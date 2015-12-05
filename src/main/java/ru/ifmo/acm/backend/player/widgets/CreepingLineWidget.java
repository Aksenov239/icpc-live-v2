package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.backend.player.TickPlayer;
import ru.ifmo.acm.datapassing.Data;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;


/**
 * @author: pashka
 */
public class CreepingLineWidget extends Widget {

    private static final double V = 0.1;
    private double SEPARATOR = 50 * TickPlayer.scale;
    public int HEIGHT = (int) (32 * TickPlayer.scale);

    Queue<String> messagesQueue = new ArrayDeque<String>(100);
    ArrayDeque<Message> messagesOnScreen = new ArrayDeque<Message>();
    Set<String> inQueue = new HashSet<String>();

    long last;

    private long lastUpdate;
    private long updateWait;

    private void update() {
        if (lastUpdate + updateWait < System.currentTimeMillis()) {
            Data data = Preparation.dataLoader.getDataBackend();
            if (data == null) {
                return;
            }
            for (ru.ifmo.acm.creepingline.Message message : Preparation.dataLoader.getDataBackend().creepingLineData.messages) {
                String text = message.getMessage();
                if (!inQueue.contains(text)) {
                    inQueue.add(text);
                    messagesQueue.add(text);
                }
            }
            lastUpdate = System.currentTimeMillis();
        }
    }

    public CreepingLineWidget(long updateWait) {
        this.updateWait = updateWait;
    }

    Font messageFont = Font.decode("Open Sans " + (int) (20 * TickPlayer.scale));

    @Override
    public void paint(Graphics2D g, int width, int height) {
//            g2.setColor(Color.red);
//            g2.setComposite(AlphaComposite.SrcOver.derive(0.3f));
//            g2.fillRoundRect(100, 100, 100, 80, 32, 32);
        update();
        g.setComposite(AlphaComposite.SrcOver.derive((float) (textOpacity)));
        g.setColor(MAIN_COLOR);
        g.fillRect(0, height - HEIGHT, width, HEIGHT);
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
            message.position -= 10;//V * dt;
            if (message.position + message.width >= 0) {
                g.drawString(message.message, (float) message.position, height - (int) (9 * TickPlayer.scale));
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
