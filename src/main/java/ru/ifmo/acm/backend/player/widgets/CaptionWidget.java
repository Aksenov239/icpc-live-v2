package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.player.TickPlayer;

import java.awt.*;

/**
 * @author: pashka
 */
public class CaptionWidget extends Widget {

    private final int MARGIN = (int) (30 * TickPlayer.scale);
    private final int Y = (int) (580 * TickPlayer.scale);
    private final int HEIGHT1 = (int) (60 * TickPlayer.scale);
    private final int HEIGHT2 = (int) (30 * TickPlayer.scale);
    private final Font FONT1 = Font.decode("Open Sans " + (int)(40 * TickPlayer.scale));
    private final Font FONT2 = Font.decode("Open Sans " + (int)(20 * TickPlayer.scale));

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
    public void paint(Graphics2D g, int width, int height) {
        changeOpacity();
        if (opacity > 0) {
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
            drawTextInRect(g, caption, x1, y, -1, HEIGHT1, position, ADDITIONAL_COLOR, Color.white, opacityState);
            y += HEIGHT1 + 2;
            g.setFont(FONT2);
            if (description != null && description.length() != 0) {
                drawTextInRect(g, description, x2, y, -1, HEIGHT2, position, MAIN_COLOR, Color.white, opacityState);
            }
        }
    }
}
