package ru.ifmo.acm.creepingline;

import com.vaadin.data.util.BeanItemContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.acm.ContextListener;
import ru.ifmo.acm.backup.BackUp;

import java.io.IOException;
import java.util.*;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.datapassing.CreepingLineData;
import ru.ifmo.acm.mainscreen.Utils;
import ru.ifmo.acm.utils.SynchronizedBeanItemContainer;

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

    public MessageData() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/creepingline.properties"));
            backup = properties.getProperty("backup.file.name");
        } catch (IOException e) {
            log.error("error", e);
        }
//        messageList = new ArrayList<>();
//        messageList = new BeanItemContainer<>(Message.class);
        messageList = new BackUp<Message>(Message.class, backup);
//        reload();
//
//        new Timer().scheduleAtFixedRate(
//                new TimerTask() {
//                    public void run() {
//                        backup();
//                    }
//                },
//                0L,
//                60000L);

//                }, 0L, 2000L);
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
    }

    //final List<Message> messageList;
    // final BeanItemContainer<Message> messageList;
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

    private void recache() {
        Data.cache.refresh(CreepingLineData.class);
    }

    public void removeMessage(Message message) {
//        synchronized (messageList) {
//            messageList.removeItem(message);
//        }
        messageList.removeItem(message);
        recache();
    }

    public void addMessage(Message message) {
        // messageList.addBean(message);
        messageList.addItem(message);
        recache();
    }

    public void addMessageToFlow(Message message) {
        // messageList.addBean(message);
        synchronized (messageFlow) {
            messageFlow.addItemAt(0, message);
            int toRemove = 5;
            if (messageFlow.size() > 2 * toRemove) {
                while (messageFlow.size() > toRemove) {
                    messageFlow.removeItem(messageFlow.getIdByIndex(toRemove));
                }
            }
        }
        recache();
    }

    public List<Message> getMessages() {
//        synchronized (messageList) {
//            return messageList.getItemIds();
//        }
        return messageList.getData();
    }

    public void tick() {
//        synchronized (messageList) {
//            List<Message> toDelete = new ArrayList<Message>();
//            for (Message message : messageList.getItemIds()) {
//                if (message.getEndTime() < System.currentTimeMillis()) {
//                    toDelete.add(message);
//                }
//            }
//
//            for (Message message : toDelete) {
//                messageList.removeItem(message);
//            }
//            fireListeners();
//        }
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
