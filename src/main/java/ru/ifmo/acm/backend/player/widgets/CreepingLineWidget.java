package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.Data;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.awt.geom.Rectangle2D;

/**
 * @author: pashka
 */
public abstract class CreepingLineWidget extends Widget {


    protected double SEPARATOR = 75;
    public int HEIGHT = 45;
    public int MARGIN = 18;

    Queue<String> messagesQueue = new ArrayDeque<String>(100);
    Set<String> inQueue = new HashSet<String>();

    long last;

    public CreepingLineWidget() {
        super();
        setVisible(true);
    }

    protected void updateImpl(Data data) {
        for (ru.ifmo.acm.creepingline.Message message : Preparation.dataLoader.getDataBackend().creepingLineData.messages) {
            byte[] bytes = message.getMessage().getBytes();
            String text = null;
            try {
                text = new String(bytes, "utf-8");
            } catch (UnsupportedEncodingException e) {
                log.error("error", e);
            }
            if (!inQueue.contains(text)) {
                addMessage(text);
            }
        }
        lastUpdate = System.currentTimeMillis();
    }

    public CreepingLineWidget(long updateWait) {
        super(updateWait);
    }

    Font messageFont = Font.decode("Open Sans " + 34);

    public void addMessage(String s) {
        inQueue.add(s);
        messagesQueue.add(s);
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

        public Message(String message, Graphics2D g) {
            this.message = message;
            Rectangle2D wh = g.getFontMetrics().getStringBounds(message, g);
            width = (int)Math.ceil(wh.getWidth());
            heigth = (int)Math.ceil(wh.getHeight());
        }
    }

}
