package ru.ifmo.acm.backend.graphics;

import ru.ifmo.acm.backend.player.widgets.stylesheets.Stylesheet;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static java.lang.Math.round;

/**
 * Created by Aksenov239 on 04.09.2016.
 */
public class GraphicsSWT extends Graphics {
    Graphics2D g;

    public GraphicsSWT(Graphics2D g) {
        this.g = (Graphics2D) g.create();
    }

    @Override
    public Graphics create() {
        return new GraphicsSWT(g);
    }

    @Override
    public Graphics create(int x, int y, int width, int height) {
        return new GraphicsSWT((Graphics2D) g.create(x, y, width, height));
    }

    private final int POINTS_IN_ROUND = 3;
    private final int ROUND_RADIUS = 4;

    @Override
    public void drawRect(int x, int y, int width, int height, Color color, double opacity, boolean italic) {
        g.setComposite(AlphaComposite.SrcOver.derive((float) opacity));
        g.setColor(color);

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
                xx[t] = (int) round(tx);
                yy[t] = (int) round(ty);
            }
        }
        g.fill(new Polygon(xx, yy, xx.length));
    }
    @Override
    public void drawString(String text, int x, int y, Font font, Color color) {
        g.setFont(font);
        g.setColor(color);
        g.drawString(text, x, y);
    }
    @Override
    public void drawRectWithText(String text, int x, int y, int width, int height, Position position, Font font,
                                 Color color, Color textColor, double opacity, double textOpacity, double margin,
                                 boolean italic, boolean scale) {
        Graphics2D g = (Graphics2D) this.g.create();
        int textWidth = g.getFontMetrics().stringWidth(text);
        double textScale = 1;

        margin = height * margin;

        if (width == -1) {
            width = (int) (textWidth + 2 * margin);
            if (position == Position.POSITION_CENTER) {
                x -= width / 2;
            } else if (position == Position.POSITION_RIGHT) {
                x -= width;
            }
        } else if (scale) {
            int maxTextWidth = (int) (width - 2 * margin);
            if (textWidth > maxTextWidth) {
                textScale = 1.0 * maxTextWidth / textWidth;
            }
        }

        drawRect(x, y, width, height, color, opacity, italic);

        g.setComposite(AlphaComposite.SrcOver.derive((float) (textOpacity)));
        g.setColor(textColor);

        FontMetrics wh = g.getFontMetrics();
        float yy = (float) (y + 1.0 * (height - wh.getStringBounds(text, g).getHeight()) / 2) + wh.getAscent()
                - 0.03f * height;
        float xx;
        if (position == Position.POSITION_LEFT) {
            xx = (float) (x + margin);
        } else if (position == Position.POSITION_CENTER) {
            xx = (float) (x + (width - textWidth * textScale) / 2);
        } else {
            xx = (float) (x + width - textWidth * textScale - margin);
        }
        AffineTransform transform = g.getTransform();
        transform.concatenate(AffineTransform.getTranslateInstance(xx, yy));
        transform.concatenate(AffineTransform.getScaleInstance(textScale, 1));
        g.setTransform(transform);
        g.drawString(text, 0, 0);
        g.dispose();
    }

    @Override
    public void drawTextThatFits(String text, int x, int y, int width, int height, Font font, Color color, double opacity, double margin) {
        FontMetrics wh = g.getFontMetrics();
        int textWidth = g.getFontMetrics().stringWidth(text);
        double textScale = 1;

        margin = height * margin;

        int maxTextWidth = (int) (width - 2 * margin);
        if (textWidth > maxTextWidth) {
            textScale = 1. * maxTextWidth / textWidth;
        }

        float yy = (float) y + wh.getAscent() - 0.03f * height;
        float xx = (float) (x + margin);

        AffineTransform transform = g.getTransform();
        transform.concatenate(AffineTransform.getTranslateInstance(xx, yy));
        transform.concatenate(AffineTransform.getScaleInstance(textScale, 1));
        g.setTransform(transform);
        g.drawString(text, 0, 0);
    }

    @Override
    public void drawStar(int x, int y, int size, Color color) {
        g.setColor(Color.decode(Stylesheet.colors.get("star.color")));
        int[] xx = new int[10];
        int[] yy = new int[10];

        double[] d = {size, size * 2};
        for (int i = 0; i < 10; i++) {
            xx[i] = (int) (x + Math.sin(Math.PI * i / 5) * d[i % 2]);
            yy[i] = (int) (y + Math.cos(Math.PI * i / 5) * d[i % 2]);
        }
        g.fillPolygon(xx, yy, 10);
    }

    @Override
    public void drawImage(BufferedImage image, int x, int y, int width, int height) {
        g.drawImage(image, x, y, width, height, null);
    }

    @Override
    public Rectangle2D getStringBounds(String message, Font font) {
        g.setFont(font);
        return g.getFontMetrics().getStringBounds(message, g);
    }

    @Override
    public void init() {
    }

    @Override
    public void dispose() {
        g.dispose();
    }
}
