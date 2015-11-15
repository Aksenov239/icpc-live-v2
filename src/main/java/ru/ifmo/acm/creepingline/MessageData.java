package ru.ifmo.acm.creepingline;

import com.vaadin.data.util.BeanItemContainer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Created by Aksenov239 on 14.11.2015.
 */
public class MessageData {
    private static MessageData messageData;

    public static MessageData getMessageData() {
        if (messageData == null) {
            messageData = new MessageData();
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
            e.printStackTrace();
        }
//        messageList = new ArrayList<>();
        messageList = new BeanItemContainer<>(Message.class);
        reload();

        new Timer().scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        backup();
                    }
                },
                0L,
                60000L);

        new Timer().scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        tick();
                    }
                }, 0L, 2000L);
    }

    //final List<Message> messageList;
    final BeanItemContainer<Message> messageList;

    public void reload() {
        synchronized (messageList) {
            messageList.removeAllItems();
            File file = new File(backup);
            if (file.exists()) {
                try {
                    Scanner sc = new Scanner(file);//getClass().getResourceAsStream("/" + backup));
                    while (sc.hasNextLine()) {
                        long start = Long.parseLong(sc.nextLine());
                        long end = Long.parseLong(sc.nextLine());
                        String msg = sc.nextLine();
                        boolean isAd = Boolean.parseBoolean(sc.nextLine());
                        messageList.addBean(new Message(msg, start, end - start, isAd));
                    }
                    sc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void backup() {
        try {
            String path = backup;//getClass().getResource(backup).getPath();

            String tmpFile = path + ".tmp";

            PrintWriter out = new PrintWriter(path + ".tmp");
            synchronized (messageList) {
                for (Message message : messageList.getItemIds()) {
                    out.println(message.getCreationTime());
                    out.println(message.getEndTime());
                    out.println(message.getMessage());
                    out.println(message.getIsAdvertisement());
                }
            }
            out.close();

            Files.move(new File(tmpFile).toPath(), new File(backup).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeMessage(Message message) {
        synchronized (messageList) {
            messageList.removeItem(message);
        }
    }

    public void addMessage(Message message) {
        messageList.addBean(message);
    }

    public List<Message> getMessages() {
        synchronized (messageList) {
            return messageList.getItemIds();
        }
    }

    public void tick() {
        synchronized (messageList) {
            List<Message> toDelete = new ArrayList<Message>();
            for (Message message : messageList.getItemIds()) {
                if (message.getEndTime() < System.currentTimeMillis()) {
                    toDelete.add(message);
                }
            }

            for (Message message : toDelete) {
                messageList.removeItem(message);
            }
            fireListeners();
        }
    }

    public void fireListeners() {
    }
}
