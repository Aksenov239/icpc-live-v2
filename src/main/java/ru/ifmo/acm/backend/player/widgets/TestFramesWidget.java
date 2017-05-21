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

    public TestFramesWidget() {
        super(100);
    }

    @Override
    protected void updateImpl(Data data) {
        setVisible(data.frameRateData.isVisible);
    }

    long[] times = new long[25];
    int frame;
    String message = "";

    @Override
    public void paintImpl(Graphics g, int width, int height) {
        update();
        if (!isVisible()) {
            return;
        }
        frame = (frame + 1) % times.length;
        if (frame == 0) {
            long dt = times[times.length - 1] - times[0];
            message = (dt > 0 ? String.format("%.2f", (times.length - 1) * 1000.0 / dt) + " fps" : "");
            System.out.println(message);
//            System.exit(0);
        }
        times[frame] = System.currentTimeMillis();
        g.drawString(message, 50, 50, font, Color.WHITE);
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return data.frameRateData;
    }

}
