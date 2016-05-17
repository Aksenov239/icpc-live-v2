package ru.ifmo.acm.creepingline;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.acm.ContextListener;
import ru.ifmo.acm.backup.BackUp;
import ru.ifmo.acm.datapassing.CreepingLineData;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.mainscreen.Advertisement;
import ru.ifmo.acm.mainscreen.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
    }

    final BackUp<Message> messageList;
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
