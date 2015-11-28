package ru.ifmo.acm.backend.player.generator;

import ru.ifmo.acm.backend.net.Preparation;
import ru.ifmo.acm.backend.player.widgets.Widget;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Properties;

/**
 * Created by aksenov on 14.04.2015.
 */
public abstract class ScreenGenerator {
    protected Widget[] widgets;
    private BufferedImage image;
    private int width;
    private int height;
    protected Properties properties;

    public ScreenGenerator(int width, int height) {
        this.width = width;
        this.height = height;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("mainscreen.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Preparation.prepareEventsLoader();
        Preparation.prepareDataLoader();
        Preparation.prepareNetwork(properties.getProperty("login", null), properties.getProperty("password", null));
    }

    public final BufferedImage getScreen() {
        Graphics2D g2 = (Graphics2D) image.getGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        for (Widget widget : widgets) {
            if (widget != null) widget.paint(g2, width, height);
        }
        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
