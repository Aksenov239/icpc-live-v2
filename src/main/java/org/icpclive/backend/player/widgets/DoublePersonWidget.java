package org.icpclive.backend.player.widgets;

import org.icpclive.backend.Preparation;
import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.datapassing.PersonData;

/**
 * @author: pashka
 */
public class DoublePersonWidget extends Widget {

    private final CaptionWidget leftWidget;
    private final CaptionWidget rightWidget;

    public DoublePersonWidget(long updateWait, long duration) {
        super(updateWait);
        leftWidget = new CaptionWidget(AbstractGraphics.Alignment.LEFT);
        rightWidget = new CaptionWidget(AbstractGraphics.Alignment.RIGHT);
        this.duration = duration;
    }

    private long duration;
    private long lastVisibleChangeLeft = Long.MAX_VALUE / 2;
    private long lastVisibleChangeRight = Long.MAX_VALUE / 2;

    protected void updateImpl(Data data) {
        PersonData personData = Preparation.dataLoader.getDataBackend().personData;


        //log.debug(Arrays.toString(personData.isVisible));

        lastVisibleChangeLeft = personData.exclusiveTimestamp[0];

        if (lastVisibleChangeLeft + duration < System.currentTimeMillis()) {
            leftWidget.setVisible(false);
        } else {
            leftWidget.setVisible(personData.isVisible[0]);
            if (leftWidget.isVisible())
                leftWidget.setCaption(personData.labelValue[0].getName(), personData.labelValue[0].getPosition());
        }

        lastVisibleChangeRight = personData.exclusiveTimestamp[1];
        if (lastVisibleChangeRight + duration < System.currentTimeMillis()) {
            rightWidget.setVisible(false);
        } else {
            rightWidget.setVisible(personData.isVisible[1]);
            if (rightWidget.isVisible())
                rightWidget.setCaption(personData.labelValue[1].getName(), personData.labelValue[1].getPosition());
        }
    }


    @Override
    public void paintImpl(AbstractGraphics g, int width, int height) {
        update();
        leftWidget.paintImpl(g, width, height);
        rightWidget.paintImpl(g, width, height);
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return data.personData;
    }
}
