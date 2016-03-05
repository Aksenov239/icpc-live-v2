package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.Data;

import java.awt.*;

/**
 * @author: pashka
 */
public class AdvertisementWidget extends Widget {

    private final CaptionWidget widget;

    public AdvertisementWidget(long updateWait, long duration) {
        super(updateWait);
        widget = new CaptionWidget(POSITION_CENTER);
        this.duration = duration;
        setVisible(false);
    }

    private long duration;
    private long lastVisibleChange = Long.MAX_VALUE / 2;

    protected void update(Data data) {
        lastVisibleChange = data.advertisementData.timestamp;
        if (lastVisibleChange + duration < System.currentTimeMillis()) {
            widget.setVisible(false);
        } else {
            widget.setVisible(data.advertisementData.isVisible);
            if (widget.isVisible())
                widget.setCaption(data.advertisementData.advertisement.getAdvertisement(), null);
        }

        lastUpdate = System.currentTimeMillis();
    }

    @Override
    public void paint(Graphics2D g, int width, int height) {
        update();
        widget.paint(g, width, height);
    }
}
