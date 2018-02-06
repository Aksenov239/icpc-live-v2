package org.icpclive.backend.player.widgets;

import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;

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
    public void paintImpl(org.icpclive.backend.graphics.Graphics g, int width, int height) {
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
