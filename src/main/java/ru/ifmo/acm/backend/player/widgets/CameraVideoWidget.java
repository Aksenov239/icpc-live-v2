package ru.ifmo.acm.backend.player.widgets;

import java.awt.*;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.Data;

/**
 * @author: Aksenov239
 */
public abstract class CameraVideoWidget extends VideoWidget {

    public CameraVideoWidget(long updateWait, int x, int y, int width, int height, int sleepTime) {
        super(x, y, width, height, sleepTime);
        this.updateWait = updateWait;
        setVisible(true);
    }

    private long updateWait;
    private long lastUpdate;

    public void update() {
        if (lastUpdate + updateWait < System.currentTimeMillis()) {
            Data data = Preparation.dataLoader.getDataBackend();
            if (data == null) {
                return;
            }

            if (!data.cameraData.cameraUrl.equals(URL) && !inChange.get()) {
                change(data.cameraData.cameraUrl);
            }
            lastUpdate = System.currentTimeMillis();
        }
    }

    public void paint(Graphics2D g, int width, int height) {
        update();

        g.drawImage(image, x, y, null);
    }
}
