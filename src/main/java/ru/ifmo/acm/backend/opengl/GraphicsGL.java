package ru.ifmo.acm.backend.opengl;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.backend.player.widgets.Widget;
import ru.ifmo.acm.backend.player.widgets.stylesheets.Stylesheet;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * Created by Aksenov239 on 08.10.2016.
 */
public class GraphicsGL extends Graphics {
    int xS = -1, yS = -1, widthS = -1, heightS = -1;

    private GL2 gl2;
    private GLU glu;

    public GraphicsGL(int x, int y, int width, int height, GL2 gl2, GLU glu) {
        this.gl2 = gl2;
        this.glu = glu;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public GraphicsGL(int width, int height, GL2 gl2, GLU glu) {
        this(0, 0, width, height, gl2, glu);
    }

    @Override
    public Graphics create() {
        GraphicsGL g = new GraphicsGL(x, y, width, height, gl2, glu);
        g.x0 = x;
        g.y0 = y;
        g.xS = xS;
        g.yS = yS;
        g.widthS = widthS;
        g.heightS = heightS;
        return g;
    }

    @Override
    public Graphics create(int x, int y, int width, int height) {
        GraphicsGL g = new GraphicsGL(x + x0, y + y0, width, height, gl2, glu);
        g.x0 = x + x0;
        g.y0 = y + y0;
        g.xS = x + x0;
        g.yS = y + y0;
        g.widthS = width;
        g.heightS = height;
        return g;
    }

    @Override
    public void drawString(String text, int x, int y, Font font, Color color) {

    }

    @Override
    public void drawRectWithText(String text, int x, int y, int width, int height, Position position, Font font,
                                 Color color, Color textColor, double opacity, double textOpacity, double margin,
                                 boolean italic, boolean scale) {
    }

    @Override
    public void drawTextThatFits(String text, int x, int y, int width, int height, Font font, Color color, double margin) {

    }

    @Override
    public void drawStar(int x, int y, int size) {
        x += x0;
        y += y0;
        setColor(Color.decode(Stylesheet.colors.get("star.color")));
        int[] xx = new int[10];
        int[] yy = new int[10];
        double[] d = {size, size * 2};
        for (int i = 0; i < xx.length; i++) {
            xx[i] = (int) (x + Math.sin(Math.PI * i / 5) * d[i % 2]);
            yy[i] = (int) (y + Math.cos(Math.PI * i / 5) * d[i % 2]);
        }

        gl2.glBegin(GL2.GL_TRIANGLES);
        for (int i = 0; i < xx.length; i++) {
            gl2.glVertex2i(xx[i], Widget.BASE_HEIGHT - yy[i]);
            gl2.glVertex2i(x, Widget.BASE_HEIGHT - y);
            gl2.glVertex2i(xx[(i + 1) % xx.length], Widget.BASE_HEIGHT - yy[(i + 1) % yy.length]);
        }
        gl2.glEnd();
    }

    @Override
    public void drawImage(BufferedImage image, int x, int y, int width, int height) {

    }

    @Override
    public void fillPolygon(int[] x, int[] y, Color color, double opacity) {
        setColor(color, opacity);
        gl2.glBegin(GL2.GL_POLYGON);
        for (int i = 0; i < x.length; i++) {
            gl2.glVertex2i(x[i] + x0, Widget.BASE_HEIGHT - y[i] - y0);
        }
        gl2.glEnd();
    }

    @Override
    public void fillPolygon(int[] x, int[] y, int xC, int yC, Color color, double opacity) {
        setColor(color, opacity);
        gl2.glBegin(GL2.GL_TRIANGLES);
        for (int i = 0; i < x.length; i++) {
            gl2.glVertex2i(x[i] + x0, Widget.BASE_HEIGHT - y[i] - y0);
            gl2.glVertex2i(xC + x0, Widget.BASE_HEIGHT - yC - y0);
            gl2.glVertex2i(x[(i + 1) % x.length] + x0, Widget.BASE_HEIGHT - y[(i + 1) % y.length] - y0);
        }
        gl2.glEnd();
    }

    @Override
    public Rectangle2D getStringBounds(String message, Font font) {
        return new Rectangle2D() {
            @Override
            public void setRect(double x, double y, double w, double h) {

            }

            @Override
            public int outcode(double x, double y) {
                return 0;
            }

            @Override
            public Rectangle2D createIntersection(Rectangle2D r) {
                return null;
            }

            @Override
            public Rectangle2D createUnion(Rectangle2D r) {
                return null;
            }

            @Override
            public double getX() {
                return 0;
            }

            @Override
            public double getY() {
                return 0;
            }

            @Override
            public double getWidth() {
                return 100;
            }

            @Override
            public double getHeight() {
                return 20;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }
        };
    }

    @Override
    public void clip() {
        xS = x;
        yS = y;
        widthS = width;
        heightS = height;
    }

    @Override
    public void clip(int x, int y, int width, int height) {
        xS = x + x0;
        yS = y + y0;
        widthS = width;
        heightS = height;
    }

    @Override
    public void dispose() {
    }
    @Override
    public void setColor(Color color) {
        gl2.glColor4i(color.getRed(), color.getBlue(), color.getGreen(), color.getAlpha());
    }

    @Override
    public void setColor(Color color, double opacity) {
        gl2.glColor4d(1. * color.getRed() / 256, 1. * color.getBlue() / 256, 1. * color.getGreen() / 256, opacity);
    }
}
