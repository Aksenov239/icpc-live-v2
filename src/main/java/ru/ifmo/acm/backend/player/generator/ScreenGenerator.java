package ru.ifmo.acm.backend.player.generator;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.backend.graphics.FastGraphics;
import ru.ifmo.acm.backend.graphics.GraphicsSWT;
import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.backend.player.widgets.Widget;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by aksenov on 14.04.2015.
 */
public class ScreenGenerator {
    private final WritableRaster raster;
    protected List<Widget> widgets = new ArrayList<>();
    private BufferedImage image;
    protected int width;
    protected int height;
    private double scale;

    public ScreenGenerator(int width, int height, Properties properties, double scale) {
        this.width = width;
        this.height = height;
        this.scale = scale;

        ColorModel colorModel = ColorModel.getRGBdefault();

        raster = colorModel.createCompatibleWritableRaster(width, height);
        image = new BufferedImage(colorModel, raster, false, null);

        Preparation.prepareEventsLoader();
        Preparation.prepareDataLoader();
        Preparation.prepareNetwork(properties.getProperty("login", null), properties.getProperty("password", null));
    }

    public final DataBufferInt getBuffer() {
        draw();
        return (DataBufferInt) raster.getDataBuffer();
    }

    public final BufferedImage getScreen() {
        draw();
        return image;
    }

    private void draw() {

        Arrays.fill(((DataBufferInt)raster.getDataBuffer()).getData(), 0);

        Graphics2D g2 = (Graphics2D) image.getGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        int width = this.width;
        int height = this.height;
        if (scale != 1) {
            g2.scale(scale, scale);
            width = (int) Math.round(width / scale);
            height = (int) Math.round(height / scale);
        }
//        Graphics g = new GraphicsSWT(g2);
        Graphics g = new FastGraphics(g2, ((DataBufferInt)raster.getDataBuffer()).getData(), this.width);
        g.setScale(scale);
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
