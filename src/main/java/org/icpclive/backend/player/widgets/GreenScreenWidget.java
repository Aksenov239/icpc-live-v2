package org.icpclive.backend.player.widgets;

import org.icpclive.backend.player.widgets.stylesheets.Stylesheet;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;

import java.awt.*;

/**
 * @author: pashka
 */
public class GreenScreenWidget extends Widget {
    public GreenScreenWidget(boolean isVisible) {
        setVisible(isVisible);
    }

    @Override
    public void paintImpl(org.icpclive.backend.graphics.Graphics g, int width, int height) {
        if (isVisible()) {
//            g.clear(width, height);
            g.drawRect(0, 0, width, height, Color.decode(Stylesheet.styles.get("chromakey.color")), 1, org.icpclive.backend.graphics.Graphics.RectangleType.SOLID);
        }
    }

    public CachedData getCorrespondingData(Data data) {
        return null;
    }
}
