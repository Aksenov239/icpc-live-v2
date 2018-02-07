package org.icpclive.backend.player.widgets;

import org.icpclive.backend.Preparation;
import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.webadmin.mainscreen.Advertisement;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * @author: pashka
 */
public abstract class CreepingLineWidget extends Widget {

    protected double SEPARATOR = 75;
    public static final int HEIGHT = 45;
    public int MARGIN = 18;

    Queue<String> messagesQueue = new ArrayDeque<>(100);
    Set<String> inQueue = new HashSet<String>();

    Queue<String> logoQueue = new ArrayDeque<>(100);
    Set<String> inLogoQueue = new HashSet<>();

    long last;

    public CreepingLineWidget() {
        super();
        setVisible(true);
    }

    private void addMessage(String message, Queue<String> queue, Set<String> set) {
        if (!set.contains(message)) {
            set.add(message);
            queue.add(message);
        }
    }

    protected void updateImpl(Data data) {
        for (org.icpclive.webadmin.creepingline.Message message : Preparation.dataLoader.getDataBackend().creepingLineData.messages) {
            addMessage(message.getMessage(), messagesQueue, inQueue);
        }

        for (Advertisement logo : Preparation.dataLoader.getDataBackend().creepingLineData.logos) {
            addMessage(logo.getAdvertisement(), logoQueue, inLogoQueue);
        }

        lastUpdate = System.currentTimeMillis();
//        for (Message message : Preparation.dataLoader.getDataBackend().creepingLineData.messages) {
//            byte[] bytes = message.getMessage().getBytes();
//            String text = null;
//            try {
//                text = new String(bytes, "utf-8");
//            } catch (UnsupportedEncodingException e) {
//                log.error("error", e);
//            }
//            if (!inQueue.contains(text)) {
//                addMessage(text);
//            }
//        }
    }

    public CreepingLineWidget(long updateWait) {
        super(updateWait);
    }

    Font messageFont = Font.decode(MAIN_FONT + " " + 34);

    public void addMessage(String s) {
        inQueue.add(s);
        messagesQueue.add(s);
    }

    public CachedData getCorrespondingData(Data data) {
        return data.creepingLineData;
    }

    class Message {
        String message;
        double position;
        int width;
        int heigth;

        public Message() {
            message = "";
            position = 0;
        }

        public Message(String message, AbstractGraphics g, Font font) {
            this.message = message;
            Rectangle2D wh = g.getStringBounds(message, font);
            width = (int) Math.ceil(wh.getWidth());
            heigth = (int) Math.ceil(wh.getHeight());
        }

        public String toString() {
            return message + " " + position;
        }
    }

}
