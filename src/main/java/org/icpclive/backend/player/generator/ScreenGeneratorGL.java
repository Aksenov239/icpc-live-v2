package org.icpclive.backend.player.generator;

import org.icpclive.backend.Preparation;
import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.player.widgets.Widget;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.events.WF.json.WFEventsLoader;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.icpclive.backend.graphics.GraphicsSWT;
import sun.awt.image.SunVolatileImage;
import sun.java2d.Surface;
import sun.java2d.opengl.OGLRenderQueue;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.lwjgl.glfw.GLFW.glfwInit;

public class ScreenGeneratorGL implements ScreenGenerator {
    //    private final WritableRaster raster;
    protected List<Widget> widgets = new ArrayList<>();
    protected int width;
    protected int height;
    private double scale;

    private VolatileImage image;
    private Surface surface;


    private Image background;

    public ScreenGeneratorGL(int width, int height, Properties properties, double scale, Image background) throws IOException {
        System.setProperty("sun.java2d.opengl", "True");
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.background = background;

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        image = gc.createCompatibleVolatileImage(width, height, VolatileImage.TRANSLUCENT);
        surface = ((SunVolatileImage) image).getDestSurface();

        System.out.println("LWJGL Version " + Version.getVersion() + " is working.");
        OGLRenderQueue.getInstance().flushAndInvokeNow(() -> {
            GLFWErrorCallback.createPrint(System.err).set();
            if (!glfwInit()) {
                System.err.println("Unable to initialize GLFW");
                return;
            }
            GL.createCapabilities();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, getTextureID());
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            System.out.println("GLWF Initalized");
        });

//        ColorModel colorModel = ColorModel.getRGBdefault();
//        raster = colorModel.createCompatibleWritableRaster(width, height);
//        image = new BufferedImage(colorModel, raster, false, null);

        Preparation.prepareEventsLoader();
        Preparation.prepareDataLoader();
        Preparation.prepareNetwork(properties.getProperty("login", null), properties.getProperty("password", null));
    }

    private int getTextureID() {
        try {
            return ((Integer) surface.getClass().getMethod("getTextureID").invoke(surface));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

//    public final DataBufferInt getBuffer() {
//        draw();
//        return (DataBufferInt) raster.getDataBuffer();
//    }

    public final Image getScreen() {
        draw();
        return image;
    }

    @Override
    public void draw(Graphics2D g2) {
        draw();
        AffineTransform transform = AffineTransform.getTranslateInstance(0, height);
        transform.concatenate(AffineTransform.getScaleInstance(1, -1));
        g2.setTransform(transform);
        g2.drawImage(image.getSnapshot(), 0, 0, null);
    }

    public final void drawToBuffer(ByteBuffer buf) {
        draw();
        OGLRenderQueue.getInstance().flushAndInvokeNow(() -> {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, getTextureID());
            GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        });
    }

    private void draw() {
//        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
//        image = gc.createCompatibleVolatileImage(width, height, VolatileImage.TRANSLUCENT);
        Graphics2D g2 = (Graphics2D) image.getGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        AffineTransform transform = AffineTransform.getTranslateInstance(0, height);
        transform.concatenate(AffineTransform.getScaleInstance(1, -1));
        g2.setTransform(transform);

        int width = this.width;
        int height = this.height;
        if (scale != 1) {
            g2.scale(scale, scale);
            width = (int) Math.round(width / scale);
            height = (int) Math.round(height / scale);
        }
        AbstractGraphics g = new GraphicsSWT(g2);

        g.setScale(scale);
        if (background != null) {
            g.drawImage(background, 0, 0, width, height);
        }

        synchronized (WFEventsLoader.GLOBAL_LOCK) {
            for (Widget widget : widgets) {
                if (widget != null) widget.paint(g, width, height);
            }
        }

        g.drawRect(0, 0, 1, 1, Color.WHITE, .5, PlateStyle.RectangleType.SOLID);

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
