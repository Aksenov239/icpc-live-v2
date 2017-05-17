package ru.ifmo.acm.backend.player.generator;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.backend.graphics.GraphicsSWT;
import ru.ifmo.acm.backend.player.widgets.Widget;
import sun.awt.image.SunVolatileImage;
import sun.java2d.opengl.OGLRenderQueue;
import sun.java2d.opengl.WGLSurfaceData;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.lwjgl.glfw.GLFW.glfwInit;

public class ScreenGenerator {
//    private final WritableRaster raster;
    protected List<Widget> widgets = new ArrayList<>();
    protected int width;
    protected int height;
    private double scale;

    private VolatileImage image;
    private WGLSurfaceData surface;

    public ScreenGenerator(int width, int height, Properties properties, double scale) {
        this.width = width;
        this.height = height;
        this.scale = scale;

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        image = gc.createCompatibleVolatileImage(width, height, VolatileImage.TRANSLUCENT);
        surface = (WGLSurfaceData) ((SunVolatileImage)image).getDestSurface();

        System.out.println("LWJGL Version " + Version.getVersion() + " is working.");
        OGLRenderQueue.getInstance().flushAndInvokeNow(() -> {
            GLFWErrorCallback.createPrint(System.err).set();
            if (!glfwInit()) {
                System.err.println("Unable to initialize GLFW");
                return;
            }
            GL.createCapabilities();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, surface.getTextureID());
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

//    public final DataBufferInt getBuffer() {
//        draw();
//        return (DataBufferInt) raster.getDataBuffer();
//    }

    public final Image getScreen() {
        draw();
        return image;
    }

    public final void drawToBuffer(ByteBuffer buf) {
        draw();
        OGLRenderQueue.getInstance().flushAndInvokeNow(() -> {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, surface.getTextureID());
            GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        });
    }

    private void draw() {

//        Arrays.fill(((DataBufferInt)raster.getDataBuffer()).getData(), 0);

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
        Graphics g = new GraphicsSWT(g2);

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
