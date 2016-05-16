package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;

import java.awt.*;

/**
 * @author: Aksenov239
 */
public class CameraVideoWidget extends VideoWidget {

    public CameraVideoWidget(long updateWait, int width, int height, int sleepTime) {
        super(0, 0, width, height, sleepTime, updateWait);
        setVisible(true);
    }

    protected void updateImpl(Data data) {
        if (data.cameraData.cameraURL != null && !data.cameraData.cameraURL.equals(URL) && !inChange.get() && ready.get()) {
            change(data.cameraData.cameraURL);
        }
    }

    public void paintImpl(Graphics2D g, int width, int height) {
        update();

        // g.drawImage(image, x, y, null);
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return data.cameraData;
    }
}
