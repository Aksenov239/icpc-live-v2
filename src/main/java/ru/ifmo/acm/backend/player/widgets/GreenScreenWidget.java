package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.backend.player.widgets.stylesheets.Stylesheet;

import java.awt.*;

/**
 * @author: pashka
 */
public class GreenScreenWidget extends Widget {
    public GreenScreenWidget(boolean isVisible) {
        setVisible(isVisible);
    }

    @Override
    public void paintImpl(Graphics g, int width, int height) {
        if (isVisible()) {
//            g.clear(width, height);
            g.drawRect(0, 0, width, height, Color.decode(Stylesheet.styles.get("chromakey.color")), 1, Graphics.RectangleType.SOLID);
        }
    }

    public CachedData getCorrespondingData(Data data) {
        return null;
    }
}
