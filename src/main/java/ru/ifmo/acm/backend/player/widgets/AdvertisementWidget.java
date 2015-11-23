package ru.ifmo.acm.backend.player.widgets;

import java.awt.*;
import ru.ifmo.acm.backend.net.Preparation;


/**
 * @author: pashka
 */
public class AdvertisementWidget extends Widget {

    private final CaptionWidget widget;

    public AdvertisementWidget(long updateWait) {
        widget = new CaptionWidget(POSITION_CENTER);
        this.updateWait = updateWait;
    }

    private long updateWait;
    private long lastUpdate;

    public void update() {
        if (lastUpdate + updateWait < System.currentTimeMillis()) {
            setVisible(Preparation.dataLoader.getDataBackend().advertisementData.isVisible);
            lastUpdate = System.currentTimeMillis();
        }
    }


    @Override
    public void paint(Graphics2D g, int width, int height) {
        update();
        widget.paint(g, width, height);
    }
}
