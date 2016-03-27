package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.Data;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;


/**
 * @author: pashka
 */
public class CreepingLineWidget extends Widget {

    private static final double V = 0.15;
    private double SEPARATOR = 75;
    public int HEIGHT = 45;
    public int MARGIN = 18;

    Queue<String> messagesQueue = new ArrayDeque<String>(100);
    ArrayDeque<Message> messagesOnScreen = new ArrayDeque<Message>();
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
                text = new String(bytes, "Windows-1251");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (!inQueue.contains(text)) {
                inQueue.add(text);
                messagesQueue.add(text);
            }
        }
        lastUpdate = System.currentTimeMillis();
    }

    public CreepingLineWidget(long updateWait) {
        super(updateWait);
    }

    Font messageFont = Font.decode("Open Sans " + 34);

    @Override
    public void paintImpl(Graphics2D g, int width, int height) {
//            g2.setColor(Color.red);
//            g2.setComposite(AlphaComposite.SrcOver.derive(0.3f));
//            g2.fillRoundRect(100, 100, 100, 80, 32, 32);
        update();
        g.setComposite(AlphaComposite.SrcOver.derive(1f));
        g.setColor(MAIN_COLOR);
        g.fillRect(0, height - HEIGHT - MARGIN, width, HEIGHT);
        g.setComposite(AlphaComposite.SrcOver.derive((float) (1)));
        g.setFont(messageFont);
        g.setColor(Color.white);
        long time = System.currentTimeMillis();
        int dt = (int) (time - last);
        last = time;

        if (messagesQueue.size() > 0) {
            if (messagesOnScreen.size() == 0 ||
                    messagesOnScreen.getLast().position + messagesOnScreen.getLast().width + SEPARATOR < width) {
                Message message = null;
                while (messagesQueue.size() > 0) {
                    String text = messagesQueue.poll();
                    if (inQueue.contains(text)) {
                        message = new Message(
                                text, g
                        );
                        break;
                    }
                }
                if (message != null) {
                    message.position = width;
                    messagesOnScreen.addLast(message);
                }
            }
        }
        for (Message message : messagesOnScreen) {
            message.position -= V * dt;
            if (message.position + message.width >= 0) {
                g.drawString(message.message, (float) message.position, height - MARGIN - 5);
            }
        }
        while (messagesOnScreen.size() > 0 && messagesOnScreen.getFirst().position + messagesOnScreen.getFirst().width < 0) {
            Message toRemove = messagesOnScreen.removeFirst();
            inQueue.remove(toRemove.message);
        }
    }

    public void addMessage(String s) {
        inQueue.add(s);
        messagesQueue.add(s);
    }

    class Message {
        String message;
        double position;
        int width;

        public Message(String message, Graphics2D g) {
            this.message = message;
            width = g.getFontMetrics().stringWidth(message);
        }
    }

}
