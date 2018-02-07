package org.icpclive.backend.graphics;

import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import com.jogamp.opengl.util.texture.Texture;

/**
 * Created by Aksenov239 on 04.09.2016.
 */
public abstract class AbstractGraphics {
    protected int width, height;
    protected int x0, y0;
    protected int x, y;
    protected Font font;
    protected Color color;
    protected double scale = 1;

    public abstract AbstractGraphics create();

    public abstract AbstractGraphics create(int x, int y, int width, int height);

    private final int POINTS_IN_ROUND = 1;
    private final int ROUND_RADIUS = 0;

    public abstract void drawRect(int x, int y, int width, int height, Color color, double opacity, RectangleType rectangleType);

    public void setScale(double scale) {
        this.scale = scale;
    }

    public abstract void clear(int x, int y, int width, int height);

    public enum RectangleType {
        SOLID_ROUNDED,
        SOLID,
        ITALIC
    }

    public abstract void clear(int width, int height);

    public enum Alignment {
        LEFT,
        CENTER,
        RIGHT
    }
    public abstract void drawString(String text, int x, int y, Font font, Color color, double opacity);

    public void drawString(String text, int x, int y, Font font, Color color) {
        drawString(text, x, y, font, color, 1D);
    }

    public abstract void drawRectWithText(String text, int x, int y, int width, int height, Alignment alignment, Font font,
                                          PlateStyle plateStyle, double opacity, double textOpacity,
                                          double margin, boolean scale);

    public abstract void drawTextThatFits(String text, int x, int y, int width, int height, Font font, Color color,
                                          double margin);

    public abstract void drawImage(Image image, int x, int y, int width, int height);

    public abstract void drawImage(Image image, int x, int y, int width, int height, double opacity);

    public void drawTexture(Texture texture, int x, int y, int width, int height) {
        throw  new AssertionError("Drawing OpenGL Textures is not supported by this Graphics class");
    }

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

    public abstract void clip(int x, int y, int width, int height);

    public void translate(int x, int y) {
        x0 += x;
        y0 += y;
    }

    public void reset() {
        this.x0 = 0;
        this.y0 = 0;
    }

    public abstract void dispose();

}
