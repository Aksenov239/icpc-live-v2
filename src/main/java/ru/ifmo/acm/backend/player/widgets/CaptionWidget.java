package ru.ifmo.acm.backend.player.widgets;

import java.awt.*;

/**
 * @author: pashka
 */
public class CaptionWidget extends Widget {

    private final int MARGIN = 30;
    private final int Y = 580;
    private final int HEIGHT1 = 60;
    private final int HEIGHT2 = 30;
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
                x1 = MARGIN;
                x2 = x1 + dx;
            } else if (position == POSITION_RIGHT) {
                x1 = width - MARGIN;
                x2 = x1 - dx;
            } else {
                x1 = width / 2;
                x2 = x1;
            }
            int y = Y;
            g.setFont(FONT1);
            drawTextInRect(g, caption, x1, y, -1, HEIGHT1, position, ADDITIONAL_COLOR, Color.white, visibilityState);
            y += HEIGHT1 + 2;
            g.setFont(FONT2);
            if (description != null && description.length() != 0) {
                drawTextInRect(g, description, x2, y, -1, HEIGHT2, position, MAIN_COLOR, Color.white, visibilityState);
            }
        }
    }
}
