package ru.ifmo.acm.backend.player.widgets;

import java.awt.*;


/**
 * @author: pashka
 */
public class DoublePersonWidget extends Widget {

    private final CaptionWidget leftWidget;
    private final CaptionWidget rightWidget;

    public DoublePersonWidget(long updateWait) {
        leftWidget = new CaptionWidget(POSITION_LEFT);
        rightWidget = new CaptionWidget(POSITION_RIGHT);
        this.updateWait = updateWait;
    }

    private long updateWait;
    private long lastUpdate;

    public void update() {
        if (lastUpdate + updateWait < System.currentTimeMillis()) {
            //Preparation.dataLoader.getDataBackend().personData;
            lastUpdate = System.currentTimeMillis();
        }
    }


    @Override
    public void paint(Graphics2D g, int width, int height) {
        leftWidget.paint(g, width, height);
        rightWidget.paint(g, width, height);
    }
}
