package org.icpclive.backend.player.widgets;

import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;

/**
 * @author: pashka
 */
public class AdvertisementWidget extends CaptionWidget {

    public AdvertisementWidget(long updateWait, long duration) {
        super(PlateStyle.Alignment.CENTER);
        this.duration = duration;
        setVisible(false);
    }

    private long duration;
    private long lastVisibleChange = Long.MAX_VALUE / 2;

    @Override
    protected void updateImpl(Data data) {
        lastVisibleChange = data.advertisementData.timestamp;
        if (lastVisibleChange + duration < System.currentTimeMillis()) {
            super.setVisible(false);
        } else {
            super.setVisible(data.advertisementData.isVisible);
            if (super.isVisible())
                super.setCaption(data.advertisementData.advertisement.getAdvertisement(), null);
        }

        lastUpdate = System.currentTimeMillis();
    }

    @Override
    public void paintImpl(AbstractGraphics g, int width, int height) {
        update();
        super.paintImpl(g, width, height);
    }

    @Override
    public CachedData getCorrespondingData(Data data) {
        return data.advertisementData;
    }
}
