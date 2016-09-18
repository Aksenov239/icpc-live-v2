package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.backend.graphics.Graphics;
import java.awt.*;
import ru.ifmo.acm.backend.player.widgets.stylesheets.*;

/**
 * @author: pashka
 */
public class ClockWidget extends Widget {

    private static final Color DARK_GRAY = new Color(0x404047);
    public static final int FONT_SIZE = 30;
    private final int x = 1740;
    private final int y = 30;
    private final int WIDTH = 154;
    private final int HEIGHT = 66;
    Font clockFont = Font.decode("Open Sans Light " + FONT_SIZE);
    private long start;

    private void initialization() {
//        start = EventsLoader.getContestData().startTime;//System.currentTimeMillis() - new Random().nextInt(5 * 60 * 60 * 1000);
        setVisible(true);
        setVisibilityState(1);
    }

    public int getHeight() {
        return HEIGHT;
    }

    public ClockWidget(long updateWait) {
        super(updateWait);
        initialization();
    }

    @Override
    protected void updateImpl(Data data) {
        super.updateImpl(data);
        if (data.clockData.isClockVisible()) {
            setVisible(true);
        } else {
            setVisible(false);
        }
        lastUpdate = System.currentTimeMillis();
    }

    @Override
    public void paintImpl(Graphics g, int width, int height) {
        update();
        updateVisibilityState();
        if (opacity == 0) return;
        /*g.drawRect(x, y, WIDTH, HEIGHT, ClockStylesheet.main.background, opacity);
        g.setColor(ClockStylesheet.main.text);
        g.setFont(clockFont);
        long time = Preparation.eventsLoader.getContestData().getCurrentTime() / 1000;

        int w1 = g.getFontMetrics(clockFont).charWidth('0');
        int w2 = g.getFontMetrics(clockFont).charWidth(':');
        int hh = (int) (FONT_SIZE * 0.75);
        int ww = w1 * 5 + w2 * 2;
        String timeS = getTimeString(Math.abs(time));
        int dx = (int) ((WIDTH - ww) / 2 + 1);
        int dy = (int) (HEIGHT - (HEIGHT - hh) / 2);
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
        } */
        long time = Preparation.eventsLoader.getContestData().getCurrentTime() / 1000;
        String timeS = getTimeString(Math.abs(time));
        drawTextInRect(g, timeS, x, y, WIDTH, HEIGHT, Graphics.Position.POSITION_CENTER, clockFont, ClockStylesheet.main.background, ClockStylesheet.main.text, opacity, WidgetAnimation.VERTICAL_ANIMATED);
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return data.clockData;
    }

    public static String getTimeString(long time) {
        int h = (int) (time / 3600);
        int m = (int) (time % 3600 / 60);
        int s = (int) (time % 60);
        return String.format("%d:%02d:%02d", h, m, s);
    }

}
