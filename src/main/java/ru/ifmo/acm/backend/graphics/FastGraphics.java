package ru.ifmo.acm.backend.graphics;

import ru.ifmo.acm.backend.player.widgets.stylesheets.PlateStyle;
import ru.ifmo.acm.backend.player.widgets.stylesheets.Stylesheet;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * @author pashka
 */
public class FastGraphics extends Graphics {

    private Graphics2D g;
    private int[] buffer;
    private int pitch;

    public FastGraphics(Graphics2D g, int[] buffer, int pitch) {
        this.g = g;
        this.buffer = buffer;
        this.pitch = pitch;
    }

    public FastGraphics(Graphics2D g, int x0, int y0, int[] buffer, int pitch) {
        this(g, buffer, pitch);
        this.x0 = x0;
        this.y0 = y0;
    }

    @Override
    public Graphics create() {
        Graphics g2 = new FastGraphics((Graphics2D) g.create(), x0, y0, buffer, pitch);
        g2.setScale(scale);
        return g2;
    }

    @Override
    public Graphics create(int x, int y, int width, int height) {
        Graphics2D gg = (Graphics2D) g.create();
        FastGraphics g2 = new FastGraphics(gg, x + x0, y + y0, buffer, pitch);
        gg.clipRect(x + x0, y + y0, width, height);
        g2.width = width;
        g2.height = height;
        g2.setScale(scale);
        return g2;
    }

    @Override
    public void clear(int width, int height) {
        g.setBackground(new Color(0, 0, 0, 0));
        g.clearRect(0, 0, width, height);
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

        drawRect(x, y, width, height, plateStyle.background, opacity, plateStyle.rectangleType);

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

    static Rectangle clip = new Rectangle();

    @Override
    public void drawRect(int x, int y, int width, int height, Color color, double opacity, RectangleType rectangleType) {
//        setColor(color, opacity * .9);
//        g.fillRect(x + x0, y + y0, width, height);
        int c = color.getRGB() & 0xffffff | ((int)(opacity * 230) << 24);
        x += x0;
        y += y0;

        clip.x = -1000000;
        clip.y = -1000000;
        clip.width = 2000000;
        clip.height = 2000000;
        g.getClipBounds(clip);
        if (clip.x > x) {
            width -= clip.x - x;
            x = clip.x;
        }
        if (clip.y > y) {
            height -= clip.y - y;
            y = clip.y;
        }
        if (x + width > clip.x + clip.width) {
            width = clip.x + clip.width - x;
        }
        if (y + height > clip.y + clip.height) {
            height = clip.y + clip.height - y;
        }
        if (width <= 0 || height <= 0) return;

        if (scale != 1) {
            width = (int)((x + width) * scale) - (int)(x * scale);
            height = (int)((y + height) * scale) - (int)(y * scale);
            x *= scale;
            y *= scale;
        }

        for (int i= 0; i < height; i++) {
            int q = (y + i) * pitch + x;
            Arrays.fill(buffer, q, q + width, c);
        }
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
    public void clip(int x, int y, int width, int height)
    {
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
    public void reset() {
        super.reset();
        g.setClip(null);
    }

    @Override
    public void dispose() {
        g.dispose();
    }
}
