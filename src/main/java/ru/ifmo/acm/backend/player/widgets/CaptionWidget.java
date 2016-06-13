package ru.ifmo.acm.backend.player.widgets;

import java.awt.*;

import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.backend.player.widgets.stylesheets.*;

/**
 * @author: pashka
 */
public class CaptionWidget extends Widget {

    private static final int SPACE = 2;
    private final int X_LEFT = 30;
    private final int X_RIGHT = 1890;
    private final int HEIGHT1 = 80;
    private final int HEIGHT2 = 39;

    private final int Y = 994 - HEIGHT2 - HEIGHT1 - SPACE;

    private final Font FONT1 = Font.decode("Open Sans " + 40);
    private final Font FONT2 = Font.decode("Open Sans " + 20);

    private String caption;
    private String description;
    private final int position;

    public CaptionWidget(int position) {
        this.position = position;
    }

    public void setCaption(String caption, String description) {
        this.caption = caption;
        this.description = description;
    }

    @Override
    public void paintImpl(Graphics2D g, int width, int height) {
        updateVisibilityState();
        if (visibilityState > 0) {
            int x1;
            int x2;
            int dx = 0;//(int) ((HEIGHT1 - HEIGHT2) * Widget.MARGIN);
            if (position == POSITION_LEFT) {
                x1 = X_LEFT;
                x2 = x1 + dx;
                QueueWidget.Y_SHIFT = 3;
            } else if (position == POSITION_RIGHT) {
                x1 = X_RIGHT;
                x2 = x1 - dx;
            } else {
                x1 = width / 2;
                x2 = x1;
            }
            int y = Y;
            g.setFont(FONT1);
            drawTextInRect(g, caption, x1, y, -1, HEIGHT1, position, CaptionStylesheet.main.background, CaptionStylesheet.main.text, visibilityState, WidgetAnimation.UNFOLD_ANIMATED);
            y += HEIGHT1 + SPACE;
            g.setFont(FONT2);
            if (description != null && description.length() != 0) {
                drawTextInRect(g, description, x2, y, -1, HEIGHT2, position, CaptionStylesheet.description.background, CaptionStylesheet.description.text, visibilityState, WidgetAnimation.UNFOLD_ANIMATED);
            }
        }
    }

    @Override
    public CachedData getCorrespondingData(Data data) {
        return null;
    }
}
