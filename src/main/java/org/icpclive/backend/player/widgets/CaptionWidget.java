package org.icpclive.backend.player.widgets;

import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.backend.player.widgets.stylesheets.CaptionStylesheet;

import java.awt.*;

/**
 * @author: pashka
 */
public class CaptionWidget extends Widget {

    private static final int SPACE = 2;
    private final int X_LEFT = 30;
    private final int X_RIGHT = 1890;
    private final int HEIGHT1 = 80;
    private final int HEIGHT2 = 50;

    private final int Y = 994 - HEIGHT2 - HEIGHT1 - SPACE;

    private final Font FONT1 = Font.decode(MAIN_FONT + " " + 40);
    private final Font FONT2 = Font.decode(MAIN_FONT + " " + 28);

    private String caption;
    private String description;
    private final AbstractGraphics.Alignment alignment;

    public CaptionWidget(AbstractGraphics.Alignment alignment) {
        this.alignment = alignment;
    }

    public void setCaption(String caption, String description) {
        this.caption = caption;
        this.description = description;
    }

    @Override
    public void paintImpl(AbstractGraphics g, int width, int height) {
        updateVisibilityState();
        if (visibilityState > 0) {
            int x1;
            int x2;
            int dx = 0;//(int) ((HEIGHT1 - HEIGHT2) * Widget.MARGIN);
            if (alignment == AbstractGraphics.Alignment.LEFT) {
                x1 = X_LEFT;
                x2 = x1 + dx;
                QueueWidget.Y_SHIFT = 3;
            } else if (alignment == AbstractGraphics.Alignment.RIGHT) {
                x1 = X_RIGHT;
                x2 = x1 - dx;
            } else {
                x1 = width / 2;
                x2 = x1;
            }
            int y = Y;
            drawTextInRect(g, caption, x1, y, -1, HEIGHT1, alignment, FONT1,
                    CaptionStylesheet.main, visibilityState, WidgetAnimation.UNFOLD_ANIMATED);
            y += HEIGHT1 + SPACE;
            if (description != null && description.length() != 0) {
                drawTextInRect(g, description, x2, y, -1, HEIGHT2, alignment, FONT2,
                        CaptionStylesheet.description, visibilityState, WidgetAnimation.UNFOLD_ANIMATED);
            }
        }
    }

    @Override
    public CachedData getCorrespondingData(Data data) {
        return null;
    }
}
