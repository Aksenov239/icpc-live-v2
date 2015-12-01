package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.backend.player.TickPlayer;
import ru.ifmo.acm.datapassing.Data;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author: pashka
 */
public class ClockWidget extends Widget {

    private BufferedImage clock;
    private final int x = (int) (1160 * TickPlayer.scale);
    private final int y = (int) (20 * TickPlayer.scale);
    private final int WIDTH = (int) (93 * TickPlayer.scale);
    private final int HEIGHT = (int) (31 * TickPlayer.scale);
    Font clockFont = Font.decode("ALS Schlange sans " + (int) (24 * TickPlayer.scale));
    private long start;

    private void initialization() {
        BufferedImage clock;
        try {
            clock = ImageIO.read(new File("pics/clock.png"));
        } catch (IOException e) {
            clock = null;
        }
        this.clock = clock;
//        start = EventsLoader.getContestData().startTime;//System.currentTimeMillis() - new Random().nextInt(5 * 60 * 60 * 1000);
        setVisible(true);
        setOpacityState(1);
    }

    public ClockWidget(long updateWait) {
        initialization();
        this.updateWait = updateWait;
    }

    private long updateWait = 0;
    private long lastUpdate = 0;

    private void update() {
        if (lastUpdate + updateWait < System.currentTimeMillis()) {
            Data data = Preparation.dataLoader.getDataBackend();
            if (data == null)
                return;
            if (data.clockData.isVisible) {
                setVisible(true);
            } else {
                setVisible(false);
            }
            lastUpdate = System.currentTimeMillis();
        }
    }

    @Override
    public void paint(Graphics2D g, int width, int height) {
//        g.drawImage(clock, x, y, null);
        update();
        changeOpacity();
        drawRect(g, x, y, WIDTH, HEIGHT, MAIN_COLOR, opacity);
        g.setColor(Color.WHITE);
        g.setFont(clockFont);
        long time = Preparation.eventsLoader.getContestData().getCurrentTime();
        if (time > 5 * 60 * 60) {
            time = 5 * 60 * 60;
        }
        int h = (int) (time / 3600);
        int m = (int) (time % 3600 / 60);
        int s = (int) (time % 60);
        int w1 = g.getFontMetrics().charWidth('0');
        int w2 = g.getFontMetrics().charWidth(':');
        String timeS = String.format("%d:%02d:%02d", h, m, s);
        int dx = (int) ((clock.getWidth() * TickPlayer.scale - w1 * 5 - w2 * 2) / 2 + 1);
        int dy = (int) (clock.getHeight() * TickPlayer.scale * 0.75);
        g.setComposite(AlphaComposite.SrcOver.derive((float) (textOpacity)));
        for (int i = 0; i < timeS.length(); i++) {
            char c = timeS.charAt(i);
            if (c == ':') {
                g.drawString(":", x + dx, y + dy);
                dx += w2;
            } else {
                int dd = (w1 - g.getFontMetrics().charWidth(c)) / 2;
                g.drawString("" + c, x + dx + dd, y + dy);
                dx += w1;
            }
        }
    }

}
