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
        widget = new CaptionWidget(POSITION_CENTER);
        this.updateWait = updateWait;
        this.duration = duration;
        setVisible(false);
    }

    private long updateWait;
    private long lastUpdate;
    private long duration;
    private long lastVisibleChange = Long.MAX_VALUE / 2;

    public void update() {
        if (lastUpdate + updateWait < System.currentTimeMillis()) {
            Data data = Preparation.dataLoader.getDataBackend();
            if (data == null)
                return;
            //System.err.println(data.advertisementData.isVisible);
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
    }


    @Override
    public void paint(Graphics2D g, int width, int height) {
        update();
        widget.paint(g, width, height);
    }
}
