package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;

/**
 * @author: pashka
 */
public class AdvertisementWidget extends CaptionWidget {

    public AdvertisementWidget(long updateWait, long duration) {
        super(Graphics.Position.POSITION_CENTER);
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
    public void paintImpl(Graphics g, int width, int height) {
        update();
        super.paintImpl(g, width, height);
    }

    @Override
    public CachedData getCorrespondingData(Data data) {
        return data.advertisementData;
    }
}
