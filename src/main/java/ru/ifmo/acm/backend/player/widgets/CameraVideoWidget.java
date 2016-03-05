package ru.ifmo.acm.backend.player.widgets;

import java.awt.*;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.Data;

/**
 * @author: Aksenov239
 */
public class CameraVideoWidget extends VideoWidget {

    public CameraVideoWidget(long updateWait, int width, int height, int sleepTime) {
        super(0, 0, width, height, sleepTime, updateWait);
        setVisible(true);
    }

    protected void update(Data data) {
        if (data.cameraData.cameraUrl != null && !data.cameraData.cameraUrl.equals(URL) && !inChange.get() && ready.get()) {
            change(data.cameraData.cameraUrl);
        }
        lastUpdate = System.currentTimeMillis();
    }

    public void paintImpl(Graphics2D g, int width, int height) {
        update();

       // g.drawImage(image, x, y, null);
    }
}
