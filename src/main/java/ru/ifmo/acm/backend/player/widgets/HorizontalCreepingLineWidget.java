package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.backend.player.widgets.stylesheets.CreepingLineStylesheet;

import java.util.ArrayDeque;

/**
 * @author: pashka
 */
public class HorizontalCreepingLineWidget extends CreepingLineWidget {
    public HorizontalCreepingLineWidget(long updateWait) {
        super(updateWait);
    }

    protected static final double V = 0.15;

    ArrayDeque<Message> messagesOnScreen;

    @Override
    public void paintImpl(Graphics g, int width, int height) {
//            g2.setColor(Color.red);
//            g2.setComposite(AlphaComposite.SrcOver.derive(0.3f));
//            g2.fillRoundRect(100, 100, 100, 80, 32, 32);
        update();
        g.drawRect(0, height - HEIGHT - MARGIN, width, HEIGHT, CreepingLineStylesheet.main.background, 1);
        g.setFont(messageFont);
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
                                text, g, messageFont
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
                g.drawString(message.message, (int) message.position, height - MARGIN - 5, messageFont, CreepingLineStylesheet.main.text);
            }
        }
        while (messagesOnScreen.size() > 0 && messagesOnScreen.getFirst().position + messagesOnScreen.getFirst().width < 0) {
            Message toRemove = messagesOnScreen.removeFirst();
            inQueue.remove(toRemove.message);
        }
    }
}
