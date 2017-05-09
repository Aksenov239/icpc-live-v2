package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.backend.player.widgets.stylesheets.ClockStylesheet;
import ru.ifmo.acm.backend.player.widgets.stylesheets.PlateStyle;
import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.ContestInfo;

import java.awt.*;

/**
 * @author: pashka
 */
public class TestFramesWidget extends Widget {

    Font font = Font.decode("Open Sans 30");
    PlateStyle style = new PlateStyle(Color.WHITE, Color.BLACK, Graphics.RectangleType.SOLID, Graphics.Alignment.CENTER);

    public TestFramesWidget() {
        super(100);
    }

    @Override
    protected void updateImpl(Data data) {
        super.updateImpl(data);
    }

    long[] times = new long[20];
    int frame;

    @Override
    public void paintImpl(Graphics g, int width, int height) {
        times[frame] = System.currentTimeMillis();
        int newFrame = (frame + 1) % times.length;
        long dt = times[frame] - times[newFrame];
        frame = newFrame;
        String s = (dt > 0 ? String.format("%.2f", (times.length - 1) * 1000.0 / dt) + " fps" : "");
        g.drawString(s, 50, 50, font, Color.WHITE);
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return null;
    }

}
