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
import ru.ifmo.acm.mainscreen.Advertisement;
import ru.ifmo.acm.mainscreen.Utils;
import ru.ifmo.acm.utils.SynchronizedBeanItemContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

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
        ContestInfo contestInfo = EventsLoader.getInstance().getContestData();
        final BlockingQueue<AnalystMessage> q = contestInfo.getAnalystMessages();
        new Thread(() -> {
            while (true) {
                try {
                    AnalystMessage e = q.take();
                    addMessageToFlow(new Message(e.getMessage(), e.getTime() * 1000, 0, false, "ICPC Analytics"));
                } catch (InterruptedException e1) {
                    break;
                }
            }
        }).start();
    }

    final BackUp<Message> messageList;
    final BeanItemContainer<Message> messageFlow;

//    public void reload() {
//        synchronized (messageList) {
//            messageList.removeAllItems();
//            File file = new File(backup);
//            if (file.exists()) {
//                try {
//                    Scanner sc = new Scanner(file);//getClass().getResourceAsStream("/" + backup));
//                    while (sc.hasNextLine()) {
//                        long start = Long.parseLong(sc.nextLine());
//                        long end = Long.parseLong(sc.nextLine());
//                        String msg = sc.nextLine();
//                        boolean isAd = Boolean.parseBoolean(sc.nextLine());
//                        messageList.addBean(new Message(msg, start, end - start, isAd));
//                    }
//                    sc.close();
//                } catch (IOException e) {
//                    log.error("error", e);
//                }
//            }
//        }
//    }
//
//    public void backup() {
//        try {
//            String path = backup;//getClass().getResource(backup).getPath();
//
//            String tmpFile = path + ".tmp";
//
//            PrintWriter out = new PrintWriter(path + ".tmp");
//            synchronized (messageList) {
//                for (Message message : messageList.getItemIds()) {
//                    out.println(message.getCreationTime());
//                    out.println(message.getEndTime());
//                    out.println(message.getMessage());
//                    out.println(message.getIsAdvertisement());
//                }
//            }
//            out.close();
//
//            Files.move(new File(tmpFile).toPath(), new File(backup).toPath(), StandardCopyOption.REPLACE_EXISTING);
//        } catch (IOException e) {
//            log.error("error", e);
//        }
//    }
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

    public void addMessageToFlow(Message message) {
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
        recache();
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
