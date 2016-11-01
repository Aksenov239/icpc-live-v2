package ru.ifmo.acm.backend.graphics;

import ru.ifmo.acm.backend.player.widgets.stylesheets.PlateStyle;
import ru.ifmo.acm.backend.player.widgets.stylesheets.Stylesheet;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Created by Aksenov239 on 04.09.2016.
 */
public class GraphicsSWT extends Graphics {
    public Graphics2D g;

    public GraphicsSWT(Graphics2D g) {
        this.g = (Graphics2D) g.create();
    }

    @Override
    public Graphics create() {
        GraphicsSWT newG = new GraphicsSWT(g);
        newG.x0 = x0;
        newG.y0 = y0;
        return newG;
    }

    @Override
    public Graphics create(int x, int y, int width, int height) {
        GraphicsSWT g2 = new GraphicsSWT((Graphics2D) g.create(x + x0, y + y0, width, height));
        g2.width = width;
        g2.height = height;
        return g2;
    }

    @Override
    public void drawString(String text, int x, int y, Font font, Color color, double opacity) {
        g.setFont(font);
        setColor(color, opacity);
        g.drawString(text, x + x0, y + y0);
    }

    @Override
    public void drawRectWithText(String text, int x, int y, int width, int height, Alignment alignment, Font font,
                                 PlateStyle plateStyle, double opacity, double textOpacity, double margin,
                                 boolean scale) {
        Graphics2D saved = g;
        x += x0;
        y += y0;
        g = (Graphics2D) saved.create();
        g.setFont(font);
        int textWidth = g.getFontMetrics().stringWidth(text);
        double textScale = 1;

        margin = height * margin;

        if (width == -1) {
            width = (int) (textWidth + 2 * margin);
            if (alignment == Alignment.CENTER) {
                x -= width / 2;
            } else if (alignment == Alignment.RIGHT) {
                x -= width;
            }
        } else if (scale) {
            int maxTextWidth = (int) (width - 2 * margin);
            if (textWidth > maxTextWidth) {
                textScale = 1.0 * maxTextWidth / textWidth;
            }
        }

        drawRect(x - x0, y - y0, width, height, plateStyle.background, opacity, plateStyle.rectangleType);

        setColor(plateStyle.text, textOpacity);

        FontMetrics wh = g.getFontMetrics();

        float yy = (float) (y + 1.0 * (height - wh.getStringBounds(text, g).getHeight()) / 2) + wh.getAscent()
                - 0.03f * height;

        float xx;
        if (alignment == Alignment.LEFT) {
            xx = (float) (x + margin);
        } else if (alignment == Alignment.CENTER) {
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
        g = saved;
    }

    @Override
    public void drawTextThatFits(String text, int x, int y, int width, int height, Font font, Color color, double margin) {
        Graphics2D saved = g;
        x += x0;
        y += y0;
        g = (Graphics2D) saved.create();
        g.setFont(font);
        g.setColor(color);
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
        g.dispose();
        g = saved;
    }

    @Override
    public void drawStar(int x, int y, int size) {
        g.setColor(Color.decode(Stylesheet.styles.get("star.color")));
        int[] xx = new int[10];
        int[] yy = new int[10];

        double[] d = {size, size * 2};
        for (int i = 0; i < 10; i++) {
            xx[i] = x0 + (int) (x + Math.sin(Math.PI * i / 5) * d[i % 2]);
            yy[i] = y0 + (int) (y + Math.cos(Math.PI * i / 5) * d[i % 2]);
        }
        g.fillPolygon(xx, yy, xx.length);
    }

    @Override
    public void drawImage(BufferedImage image, int x, int y, int width, int height) {
        g.drawImage(image, x0 + x, y0 + y, width, height, null);
    }

    @Override
    public void fillPolygon(int[] x, int[] y, Color color, double opacity) {
        setColor(color, opacity);
        int[] xx = new int[x.length];
        int[] yy = new int[y.length];
        for (int i = 0; i < x.length; i++) {
            xx[i] = x[i] + x0;
            yy[i] = y[i] + y0;
        }
        g.fillPolygon(xx, yy, xx.length);
    }

    @Override
    public void fillPolygon(int[] x, int[] y, int xC, int yC, Color color, double opacity) {
        fillPolygon(x, y, color, opacity);
    }

    @Override
    public Rectangle2D getStringBounds(String message, Font font) {
        g.setFont(font);
        return g.getFontMetrics().getStringBounds(message, g);
    }

    @Override
    public void clip() {
        g.clipRect(0, 0, this.width, this.height);
    }

    @Override
    public void clip(int x, int y, int width, int height) {
        g.clipRect(x + x0, y + y0, width, height);
    }

    @Override
    public void setColor(Color color) {
        g.setColor(color);
    }

    @Override
    public void setColor(Color color, double opacity) {
        g.setColor(color);
        g.setComposite(AlphaComposite.SrcOver.derive((float) opacity));
    }

    @Override
    public void dispose() {
        g.dispose();
    }
}
