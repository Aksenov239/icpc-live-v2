package org.icpclive.backend.player.generator;

import org.icpclive.backend.Preparation;
import org.icpclive.backend.player.widgets.Widget;
import org.icpclive.backend.graphics.GraphicsSWT;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ScreenGeneratorSWT implements ScreenGenerator {

//    private final WritableRaster raster;
    protected List<Widget> widgets = new ArrayList<>();
    protected int width;
    protected int height;
    private double scale;

    private BufferedImage image;

    public ScreenGeneratorSWT(int width, int height, Properties properties, double scale) {
        this.width = width;
        this.height = height;
        this.scale = scale;

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Preparation.prepareEventsLoader();
        Preparation.prepareDataLoader();
        Preparation.prepareNetwork(properties.getProperty("login", null), properties.getProperty("password", null));
    }

    public final Image getScreen() {
        draw();
        return image;
    }

    private void draw() {
//        Arrays.fill(((DataBufferInt)raster.getDataBuffer()).getData(), 0);
        draw((Graphics2D) image.getGraphics());
    }

    @Override
    public void draw(Graphics2D g2) {

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int width = this.width;
        int height = this.height;
        if (scale != 1) {
            g2.scale(scale, scale);
            width = (int) Math.round(width / scale);
            height = (int) Math.round(height / scale);
        }
        org.icpclive.backend.graphics.Graphics g = new GraphicsSWT(g2);

//        Graphics g = new FastGraphics(g2, ((DataBufferInt)raster.getDataBuffer()).getData(), this.width);
        g.setScale(scale);

//        g.clear(width, height);
        for (Widget widget : widgets) {
            if (widget != null) widget.paint(g, width, height);
        }

        g2.dispose();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void addWidget(Widget widget) {
        widgets.add(widget);
    }

    public void removeWidget(Widget widget) {
        widgets.remove(widget);
    }
}
