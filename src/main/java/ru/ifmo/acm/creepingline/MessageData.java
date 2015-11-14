package ru.ifmo.acm.creepingline;

import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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
            properties.load(getClass().getResourceAsStream("creepingline.properties"));
            backup = properties.getProperty("backup.file.name");
        } catch (IOException e) {
            e.printStackTrace();
        }
        messageList = new ArrayList<>();
        reload();
    }

    final List<Message> messageList;

    public void reload() {
        synchronized (messageList) {
            messageList.clear();
            Scanner sc = new Scanner(getClass().getResourceAsStream(backup));
            while (sc.hasNextLine()) {
                long start = Long.parseLong(sc.nextLine());
                long end = Long.parseLong(sc.nextLine());
                String msg = sc.nextLine();
                boolean isAd = Boolean.parseBoolean(sc.nextLine());
                messageList.add(new Message(msg, start, end - start, isAd));
            }
            sc.close();
        }
    }

    public void backup(){
        try {
            String path = getClass().getResource(backup).getPath();

            String tmpFile = path + ".tmp";

            PrintWriter out = new PrintWriter(path + ".tmp");
            synchronized (messageList) {
                for (Message message : messageList) {
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

    public void removeMessage(Message message){
        synchronized (messageList) {
            messageList.remove(message);
        }
    }

    public void addMessage(Message message){
        messageList.add(message.clone());
    }
}
