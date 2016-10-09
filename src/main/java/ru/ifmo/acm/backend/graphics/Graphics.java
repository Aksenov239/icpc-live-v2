package ru.ifmo.acm.backend.graphics;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Created by Aksenov239 on 04.09.2016.
 */
public abstract class Graphics {
    protected int x, y, width, height;
    protected int x0, y0;
    protected Font font;
    protected Color color;

    public abstract Graphics create();

    public abstract Graphics create(int x, int y, int width, int height);

    private final int POINTS_IN_ROUND = 3;
    private final int ROUND_RADIUS = 4;

    public void drawRect(int x, int y, int width, int height, Color color, double opacity, boolean italic) {
        int hh = (int) (height * opacity);
        y += (height - hh) / 2;
        height = hh;

        int[] xx = new int[POINTS_IN_ROUND * 4];
        int[] yy = new int[POINTS_IN_ROUND * 4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < POINTS_IN_ROUND; j++) {
                int t = i * POINTS_IN_ROUND + j;
                double a = Math.PI / 2 * j / (POINTS_IN_ROUND - 1);
                int dx = new int[]{0, 1, 0, -1}[i];
                int dy = new int[]{1, 0, -1, 0}[i];
                double baseX = (i == 0 || i == 3 ? x + ROUND_RADIUS : x + width - ROUND_RADIUS);
                double baseY = (i == 2 || i == 3 ? y + ROUND_RADIUS : y + height - ROUND_RADIUS);

                double tx = baseX + ROUND_RADIUS * (dx * Math.sin(a) - dy * Math.cos(a));
                double ty = baseY + ROUND_RADIUS * (dx * Math.cos(a) + dy * Math.sin(a));
                if (italic) tx -= (ty - (y + height / 2)) * 0.2;
                xx[t] = (int) Math.round(tx);
                yy[t] = (int) Math.round(ty);
            }
        }
        fillPolygon(xx, yy, x, y, color, opacity);
    }

    public void drawRect(int x, int y, int width, int height, Color color, double opacity) {
        drawRect(x, y, width, height, color, opacity, false);
    }

    public enum Position {
        POSITION_LEFT,
        POSITION_CENTER,
        POSITION_RIGHT
    }

    public abstract void drawString(String text, int x, int y, Font font, Color color);

    public abstract void drawRectWithText(String text, int x, int y, int width, int height, Position position, Font font,
                                          Color color, Color textColor, double opacity, double textOpacity,
                                          double margin, boolean italic, boolean scale);

    public abstract void drawTextThatFits(String text, int x, int y, int width, int height, Font font, Color color,
                                          double margin);

    public abstract void drawStar(int x, int y, int size);

    public abstract void drawImage(BufferedImage image, int x, int y, int width, int height);

    public abstract void fillPolygon(int[] x, int[] y, Color color, double opacity);

    public void fillPolygon(int[] x, int[] y, Color color) {
        fillPolygon(x, y, color, 1);
    }

    public abstract void fillPolygon(int[] x, int[] y, int xC, int yC, Color color, double opacity);

    public void setFont(Font font) {
        this.font = font;
    }

    public abstract void setColor(Color color);

    public abstract void setColor(Color color, double opacity);

    public abstract Rectangle2D getStringBounds(String text, Font font);

    public abstract void clip();

    public abstract void clip(int x, int y, int width, int height);

    public void translate(int x, int y) {
        x0 += x;
        y0 += y;
    }

    public void reset() {
        this.x = 0;
        this.y = 0;
    }

    public abstract void dispose();

}
