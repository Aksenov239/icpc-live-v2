package org.icpclive.webadmin.creepingline;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vaadin.data.util.BeanItemContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.Config;
import org.icpclive.backend.Preparation;
import org.icpclive.datapassing.CreepingLineData;
import org.icpclive.datapassing.Data;
import org.icpclive.events.EventsLoader;
import org.icpclive.events.WF.WFAnalystMessage;
import org.icpclive.events.WF.json.WFEventsLoader;
import org.icpclive.webadmin.ContextListener;
import org.icpclive.webadmin.backup.BackUp;
import org.icpclive.webadmin.mainscreen.Advertisement;
import org.icpclive.webadmin.mainscreen.MainScreenData;
import org.icpclive.webadmin.mainscreen.Utils;
import org.icpclive.webadmin.utils.SynchronizedBeanItemContainer;
import twitter4j.Status;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Aksenov239 on 14.11.2015.
 */
public class MessageData {
    private static final Logger log = LogManager.getLogger(MessageData.class);

    private static MessageData messageData;
    private static Set<String> existingMessages;

    public static MessageData getMessageData() {
        if (messageData == null) {
            messageData = new MessageData();
        }
        return messageData;
    }

    private String backup;
    private String logoBackup;

    public MessageData() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/creepingline.properties"));
            backup = properties.getProperty("backup.file.name");
            logoBackup = properties.getProperty("backup.logo.file.name");
        } catch (IOException e) {
            log.error("error", e);
        }
        messageList = new BackUp<>(Message.class, backup);
        logosList = new BackUp<>(Advertisement.class, logoBackup);
        isVisible = true;
        Utils.StoppedThread messageListUpdater = new Utils.StoppedThread(new Utils.StoppedRunnable() {
            @Override
            public void run() {
                while (!stop) {
                    messageList.getData().forEach(msg -> {
                        if (msg.getEndTime() < System.currentTimeMillis()) {
                            removeMessage(msg);
                        }
                    });

                    List<Message> toDelete = new ArrayList<>();
                    messageFlow.getItemIds().forEach(msg -> {
                        if (msg.getEndTime() < System.currentTimeMillis()) {
                            toDelete.add(msg);
                        }
                    });
                    int maxMessagesInFlow = MainScreenData.getProperties().maximumFlowSize;
                    List<Message> messages = messageFlow.getItemIds();
                    for (int i = maxMessagesInFlow; i < messages.size(); i++) {
                        toDelete.add(messages.get(i));
                    }
                    toDelete.forEach(messageFlow::removeItem);

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        log.error("error", e);
                    }
                }
            }
        });
        messageListUpdater.start();
        ContextListener.addThread(messageListUpdater);
        existingMessages = new HashSet<>();
        EventsLoader eventsLoader = EventsLoader.getInstance();

        Utils.StoppedThread analytics = new Utils.StoppedThread(new Utils.StoppedRunnable() {
            @Override
            public void run() {
                Properties properties = null;
                while (true) {
                    try {
                        properties = Config.loadProperties("events");
                    } catch (IOException e) {
                        log.error("Properties cannot be loaded", e);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e1) {
                            log.error(e1);
                        }
                    }

                    if (!(eventsLoader instanceof WFEventsLoader)) {
                        return;
                    }

                    WFEventsLoader wfEventsLoader = (WFEventsLoader) eventsLoader;
                    while (eventsLoader.getContestData() == null) {
                    }

                    while (eventsLoader.getContestData().getStartTime() == 0) { }

                    String url = properties.getProperty("analytics.url", null);
                    if (url == null) {
                        log.info("There is no analytics feed");
                        return;
                    }
                    String login = properties.getProperty("analytics.login", "");
                    String password = properties.getProperty("analytics.password", "");

                    try {
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(
                                        Preparation.openAuthorizedStream(url, login, password), "UTF-8"));

                        String line;
                        while ((line = br.readLine()) != null) {
                            JsonObject je = new Gson().fromJson(line, JsonObject.class);
                            switch (je.get("type").getAsString()) {
                                case "commentary-messages":
                                    WFAnalystMessage message =
                                            wfEventsLoader.readAnalystMessage(je.get("data").getAsJsonObject());
                                    long endTime = message.getTime() + MainScreenData.getProperties().messageLifespanCreepingLine;
                                    if (message.getPriority() <= 3 &&
                                            endTime > System.currentTimeMillis()) {
                                        System.err.println("Analytics message: " + line);
                                        addMessageToFlow(new Message(message.getMessage(),
                                                message.getTime(),
                                                MainScreenData.getProperties().messageLifespanCreepingLine,
                                                false,
                                                "Analytics"));
                                    }
                                    break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        log.error(e);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e1) {
                            log.error(e1);
                        }
                    }
                }
            }
        });
        analytics.start();
        ContextListener.addThread(analytics);
    }

    public static void processTwitterMessage(Status status) {
        Message message = new Message(
                status.getText(),
                System.currentTimeMillis(),
                MainScreenData.getProperties().messageLifespanCreepingLine,
                false, "@" + status.getUser().getScreenName());
        addMessageToFlow(message);
    }

    final BackUp<Message> messageList;
    final static BeanItemContainer<Message> messageFlow =
            new SynchronizedBeanItemContainer<>(Message.class);

    public final BackUp<Advertisement> logosList;

    private boolean isVisible;

    public synchronized void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
        recache();
    }

    public synchronized boolean isVisible() {
        return isVisible;
    }

    public void recache() {
        synchronized (messageList.getContainer()) {
            synchronized (logosList.getContainer()) {
                Data.cache.refresh(CreepingLineData.class);
            }
        }
    }

    public void addLogo(Advertisement logo) {
        logosList.addItem(logo);
    }

    public void removeLogo(Advertisement logo) {
        logosList.removeItem(logo);
    }

    public List<Advertisement> getLogos() {
        return logosList.getData();
    }

    public void setLogoValue(Object key, String value) {
        logosList.setProperty(key, "advertisement", value);
    }

    public void removeMessage(Message message) {
        messageList.removeItem(message);
        recache();
    }

    public void addMessage(Message message) {
        messageList.addItem(message);
        recache();
    }

    public static void addMessageToFlow(Message message) {
        if (!existingMessages.contains(message.getMessage())) {
            messageFlow.addItemAt(0, message);
            existingMessages.add(message.getMessage());
        }
    }

    public List<Message> getMessages() {
        return messageList.getData();
    }

    public void fireListeners() {
    }
}
