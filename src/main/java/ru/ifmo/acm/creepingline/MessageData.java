package ru.ifmo.acm.creepingline;

import com.vaadin.data.util.BeanItemContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.acm.ContextListener;
import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.backup.BackUp;
import ru.ifmo.acm.datapassing.CreepingLineData;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.AnalystMessage;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.EventsLoader;
import ru.ifmo.acm.events.WF.WFAnalystMessage;
import ru.ifmo.acm.mainscreen.Advertisement;
import ru.ifmo.acm.mainscreen.Polls.Poll;
import ru.ifmo.acm.mainscreen.Utils;
import ru.ifmo.acm.utils.SynchronizedBeanItemContainer;
import twitter4j.Status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aksenov239 on 14.11.2015.
 */
public class MessageData {
    private static final Logger log = LogManager.getLogger(MessageData.class);

    private static MessageData messageData;

    public static MessageData getMessageData() {
        if (messageData == null) {
            messageData = new MessageData();
            Utils.StoppedThread twitterThread = new Utils.StoppedThread(TwitterLoader.getInstance());
            twitterThread.start();
            ContextListener.addThread(twitterThread);
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
        Utils.StoppedThread update = new Utils.StoppedThread(new Utils.StoppedRunnable() {
            @Override
            public void run() {
                while (!stop) {
                    tick();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        log.error("error", e);
                    }
                }
            }
        });
        update.start();
        ContextListener.addThread(update);
        messageFlow = new SynchronizedBeanItemContainer<>(Message.class);
        EventsLoader eventsLoader = EventsLoader.getInstance();
        Utils.StoppedThread analytics = new Utils.StoppedThread(new Utils.StoppedRunnable() {
            @Override
            public void run() {
                while (true) {
                    final BlockingQueue<AnalystMessage> q = eventsLoader.getContestData().getAnalystMessages();
                    try {
                        AnalystMessage e = q.poll(5000, TimeUnit.MILLISECONDS);
                        if (e == null) continue;
                        if (e.getCategory() == WFAnalystMessage.WFAnalystMessageCategory.HUMAN || e.getPriority() <= 1) {
                            addMessageToFlow(new Message(e.getMessage(), e.getTime() * 1000, 0, false, "Analytics"));
                        }
                    } catch (InterruptedException e1) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            break;
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
                System.currentTimeMillis(), 0, false, "@" + status.getUser().getScreenName());
        addMessageToFlow(message);
    }

    final BackUp<Message> messageList;
    static BeanItemContainer<Message> messageFlow;

    public final BackUp<Advertisement> logosList;

    private void recache() {
        Data.cache.refresh(CreepingLineData.class);
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
        // messageList.addBean(message);
        synchronized (messageFlow) {
            messageFlow.addItemAt(0, message);
            int toRemove = 200;
            if (messageFlow.size() > 2 * toRemove) {
                while (messageFlow.size() > toRemove) {
                    messageFlow.removeItem(messageFlow.getIdByIndex(toRemove));
                }
            }
        }
//        recache();
    }

    public List<Message> getMessages() {
        return messageList.getData();
    }

    public void tick() {
        List<Message> toDelete = new ArrayList<Message>();
        messageList.getData().forEach(msg -> {
            if (msg.getEndTime() < System.currentTimeMillis()) {
                toDelete.add(msg);
            }
        });
        toDelete.forEach(msg -> messageList.removeItem(msg));
        fireListeners();
    }

    public void fireListeners() {
    }
}
