package org.icpclive.backend.player.widgets;

import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;

/**
 * @author: Aksenov239
 */
public class CameraVideoWidget extends Widget {

    PlayerWidget video;

    public CameraVideoWidget(long updateWait, int width, int height, int sleepTime) {
        video = PlayerWidget.getPlayerWidget(0, 0, width, height, sleepTime, 0);
        setVisible(true);
    }

    protected void updateImpl(Data data) {
        if (data.cameraData.cameraURL != null && !data.cameraData.cameraURL.equals(video.getCurrentURL()) && !video.inChange && video.readyToShow()) {
            video.change(data.cameraData.cameraURL);
        }
    }

    public void paintImpl(AbstractGraphics g, int width, int height) {
        update();

        // g.drawImage(image, x, y, null);
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return data.cameraData;
    }
}
