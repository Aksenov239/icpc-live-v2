package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.net.Preparation;
import ru.ifmo.acm.datapassing.PersonData;

import java.awt.*;

/**
 * @author: pashka
 */
public class DoublePersonWidget extends Widget {

    private final CaptionWidget leftWidget;
    private final CaptionWidget rightWidget;

    public DoublePersonWidget(long updateWait, long duration) {
        leftWidget = new CaptionWidget(POSITION_LEFT);
        rightWidget = new CaptionWidget(POSITION_RIGHT);
        this.updateWait = updateWait;
        this.duration = duration;
    }

    private long updateWait;
    private long lastUpdate;
    private long duration;
    private long lastVisibleChangeLeft = Long.MAX_VALUE / 2;
    private long lastVisibleChangeRight = Long.MAX_VALUE / 2;

    public void update() {
        if (lastUpdate + updateWait < System.currentTimeMillis()) {
            if (Preparation.dataLoader.getDataBackend() == null)
                return;

            PersonData personData = Preparation.dataLoader.getDataBackend().personData;

            //System.err.println(Arrays.toString(personData.isVisible));

            lastVisibleChangeLeft = personData.timestamp[0];

            if (lastVisibleChangeLeft + duration < System.currentTimeMillis()) {
                leftWidget.setVisible(false);
            } else {
                leftWidget.setVisible(personData.isVisible[0]);
                leftWidget.setCaption(personData.label[0].getName(), personData.label[0].getPosition());
            }

            lastVisibleChangeRight = personData.timestamp[1];
            if (lastVisibleChangeRight + duration < System.currentTimeMillis()) {
                rightWidget.setVisible(false);
            } else {
                rightWidget.setVisible(personData.isVisible[1]);
                rightWidget.setCaption(personData.label[1].getName(), personData.label[1].getPosition());
            }

            lastUpdate = System.currentTimeMillis();
        }
    }


    @Override
    public void paint(Graphics2D g, int width, int height) {
        update();
        leftWidget.paint(g, width, height);
        rightWidget.paint(g, width, height);
    }
}
