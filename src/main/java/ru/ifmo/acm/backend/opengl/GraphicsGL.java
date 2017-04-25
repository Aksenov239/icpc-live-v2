package ru.ifmo.acm.backend.opengl;

import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.RenderState;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.math.geom.AABBox;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.backend.player.widgets.Widget;
import ru.ifmo.acm.backend.player.widgets.stylesheets.PlateStyle;
import ru.ifmo.acm.backend.player.widgets.stylesheets.Stylesheet;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Aksenov239 on 08.10.2016.
 */
public class GraphicsGL extends Graphics {
    public static RenderState renderState;
    public static RegionRenderer regionRenderer;
    public static TextRegionUtil textRegionUtil;
    private final int[] sampleCount = new int[]{4};
    private final float dpiH = 72;
    public static HashMap<String, com.jogamp.graph.font.Font> fonts = new HashMap<>();
    public HashMap<String, Double> ascent = new HashMap<>();
    private static com.jogamp.graph.font.Font defaultFont;
    private static HashMap<String, TextRenderer> textRenderers = new HashMap<>();
    private static Graphics2D localGraphics;

    static {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        localGraphics = (Graphics2D) image.getGraphics();
    }

    public static void loadFonts() {
        try {
            defaultFont = //FontFactory.getDefault().getDefault();
//                    FontFactory.get(new File(GraphicsGL.class.getClassLoader().getResource("fonts/times.ttf").getFile()));
                    FontFactory.get(new File(GraphicsGL.class.getClassLoader().getResource("fonts/OpenSans-Regular.ttf").getFile()));
            fonts.put("Open Sans Light", FontFactory.get(new File(GraphicsGL.class.getClassLoader().getResource("fonts/OpenSans-Light.ttf").getFile())));
            fonts.put("Open Sans Regular", FontFactory.get(new File(GraphicsGL.class.getClassLoader().getResource("fonts/OpenSans-Regular.ttf").getFile())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getAscent(Font font) {
        String name = font.toString();
        if (ascent.containsKey(name)) {
            return ascent.get(name);
        }
        localGraphics.setFont(font);
        double ascent = localGraphics.getFontMetrics().getAscent();
        this.ascent.put(name, ascent);
        return ascent;
    }

    public FontMetrics getFontMetrics(com.jogamp.graph.font.Font font, int fontSize) {
        String name = font.toString() + " " + fontSize;
        localGraphics.setFont(Font.decode(name));
        return localGraphics.getFontMetrics();
    }

    public com.jogamp.graph.font.Font getFont(Font font) {
        return fonts.getOrDefault(font.getName(), defaultFont);
    }

    public TextRenderer getTextRenderer(Font font) {
        if (!textRenderers.containsKey(font.toString())) {
            textRenderers.put(font.toString(), new TextRenderer(font));
        }
        return textRenderers.get(font.toString());
    }

    public Rectangle2D getBounds(Font font, String text) {
        localGraphics.setFont(font);
        return localGraphics.getFontMetrics().getStringBounds(text, localGraphics);
    }

    int xS = -1, yS = -1, widthS = -1, heightS = -1;

    private GL2 gl2;
    private GLU glu;

    public GL2 getGL() {
        return gl2;
    }

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
        g.x0 = x0;
        g.y0 = y0;
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
    public void clear(int width, int height) {
        gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
    }

    private void drawString(String text, float x, float y, float scaleX, float scaleY,
//                            com.jogamp.graph.font.Font joglFont, int fontSize,
                            Font font,
                            Color color, double opacity) {
//        com.jogamp.graph.font.Font joglFont = getFont(font);
//        int fontSize = font.getSize();
//        final PMVMatrix pmv = regionRenderer.getMatrix();
//        pmv.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
//        pmv.glLoadIdentity();
//        pmv.glOrthof(0, Widget.BASE_WIDTH, 0, Widget.BASE_HEIGHT, -1, 1);
//        pmv.glTranslatef(x, Widget.BASE_HEIGHT - y, 0);
//        pmv.glScalef(scaleX, scaleY, 1);
//
//        float[] rgbColor = fromColor(color);
//        rgbColor[3] = 1;
//        renderState.setColorStatic(rgbColor[0], rgbColor[1], rgbColor[2], (float)opacity);
//
//        regionRenderer.enable(gl2, true);
////        textRegionUtil.clear(gl2);
//        textRegionUtil.drawString3D(gl2, regionRenderer, joglFont, joglFont.getPixelSize(fontSize, dpiH), text,
//                rgbColor, sampleCount);
////        TextRegionUtil.drawString3D(gl2, Region.MAX_QUALITY | Region.COLORCHANNEL_RENDERING_BIT,
////                regionRenderer, joglFont, joglFont.getPixelSize(fontSize, dpiH), text,
////                rgbColor, sampleCount, new AffineTransform(), new AffineTransform());
//        regionRenderer.enable(gl2, false);
        gl2.glPushMatrix();
        gl2.glTranslated(x, Widget.BASE_HEIGHT - y, 0);
        gl2.glScaled(scaleX, scaleY, 1);

        TextRenderer textRenderer = getTextRenderer(font);
        textRenderer.begin3DRendering();

        float[] rgbColor = fromColor(color);
        textRenderer.setColor(rgbColor[0], rgbColor[1], rgbColor[2], (float) opacity);
        textRenderer.draw(text, 0, 0);

        textRenderer.end3DRendering();

        gl2.glPopMatrix();
    }

    @Override
    public void drawString(String text, int x, int y, Font font, Color color, double opacity) {
//        drawString(text, x, y, 1, 1, getFont(font), font.getSize(), color, opacity);
        drawString(text, x, y, 1, 1, font, color, opacity);
    }

    @Override
    public void drawRectWithText(String text, int x, int y, int width, int height, Alignment alignment, Font font,
                                 PlateStyle plateStyle, double opacity, double textOpacity, double margin,
                                 boolean scale) {
        x += x0;
        y += y0;
//        com.jogamp.graph.font.Font joglFont = getFont(font);
//        float pixelSize = joglFont.getPixelSize(font.getSize(), dpiH);
////        FontMetrics fm = getFontMetrics(joglFont, font.getSize());
//        AABBox bounds = joglFont.getMetricBounds(text, pixelSize);
////        Rectangle2D bounds = fm.getStringBounds(text, localGraphics);
//        TextRenderer textRenderer = getTextRenderer(font);
//        Rectangle2D bounds = textRenderer.getBounds(text);
        Rectangle2D bounds = getBounds(font, text);
        double textWidth = bounds.getWidth();
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

        float yy = (float) (y + 1. * (height - bounds.getHeight()) / 2
                + getAscent(font)
//                + fm.getAscent()
                - 0.03f * height);

        float xx;
        if (alignment == Alignment.LEFT) {
            xx = (float) (x + margin);
        } else if (alignment == Alignment.CENTER) {
            xx = (float) (x + (width - textWidth * textScale) / 2);
        } else {
            xx = (float) (x + width - textWidth * textScale - margin);
        }

//        drawString(text, xx, yy, (float) textScale, 1f, joglFont, font.getSize(), plateStyle.text, textOpacity);
        drawString(text, xx, yy, (float) textScale, 1f, font, plateStyle.text, textOpacity);
    }

    @Override
    public void drawTextThatFits(String text, int x, int y, int width, int height, Font font, Color color, double margin) {
        x += x0;
        y += y0;

//        com.jogamp.graph.font.Font joglFont = getFont(font);
//        AABBox bounds = joglFont.getMetricBounds(text, joglFont.getPixelSize(font.getSize(), dpiH));
//        Rectangle2D bounds = getTextRenderer(font).getBounds(text);
        Rectangle2D bounds = getBounds(font, text);
        double textWidth = bounds.getWidth();
        double textScale = 1;

        margin = height * margin;

        int maxTextWidth = (int) (width - 2 * margin);
        if (textWidth > maxTextWidth) {
            textScale = 1. * maxTextWidth / textWidth;
        }

        float yy = (float) y + (float) getAscent(font) - 0.03f * height;
        float xx = (float) (x + margin);

//        drawString(text, xx, yy, (float) textScale, 1f, joglFont, font.getSize(), color, 1);
        drawString(text, xx, yy, (float) textScale, 1f, font, color, 1);
    }

    @Override
    public void drawStar(int x, int y, int size) {
        x += x0;
        y += y0;
        setColor(Color.decode(Stylesheet.styles.get("star.color")), 1);
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
        TextureData textureData = AWTTextureIO.newTextureData(gl2.getGLProfile(), image, false);
        Texture texture = TextureIO.newTexture(textureData);

        drawTexture(texture, x, y, width, height);
        texture.destroy(gl2);
    }

    public void drawTexture(Texture texture, int x, int y, int width, int height) {
        x += x0;
        y += y0;
        texture.enable(gl2);
        texture.bind(gl2);
//        TextureCoords coords = texture.getImageTexCoords();
        gl2.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
        if (texture.getMustFlipVertically()) {
            gl2.glBegin(GL2.GL_QUADS);
            gl2.glTexCoord2f(0, 1);
            gl2.glVertex2i(x, Widget.BASE_HEIGHT - y - height);
            gl2.glTexCoord2f(0, 0);
            gl2.glVertex2i(x, Widget.BASE_HEIGHT - y);
            gl2.glTexCoord2f(1, 0);
            gl2.glVertex2i(x + width, Widget.BASE_HEIGHT - y);
            gl2.glTexCoord2f(1, 1);
            gl2.glVertex2i(x + width, Widget.BASE_HEIGHT - y - height);
            gl2.glEnd();
        } else {
            gl2.glBegin(GL2.GL_QUADS);
            gl2.glTexCoord2f(0, 0);
            gl2.glVertex2i(x, Widget.BASE_HEIGHT - y - height);
            gl2.glTexCoord2f(0, 1);
            gl2.glVertex2i(x, Widget.BASE_HEIGHT - y);
            gl2.glTexCoord2f(1, 1);
            gl2.glVertex2i(x + width, Widget.BASE_HEIGHT - y);
            gl2.glTexCoord2f(1, 0);
            gl2.glVertex2i(x + width, Widget.BASE_HEIGHT - y - height);
            gl2.glEnd();

        }

        texture.disable(gl2);
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
        com.jogamp.graph.font.Font joglFont = getFont(font);
        AABBox bounds = joglFont.getMetricBounds(message, joglFont.getPixelSize(font.getSize(), dpiH));
        return new Rectangle((int) bounds.getWidth(), (int) bounds.getHeight());
    }

    @Override
    public void clip() {
        xS = x;
        yS = y;
        widthS = width;
        heightS = height;
        System.err.println(x + " " + y + " " + widthS + " " + heightS);
//        gl2.glScissor(xS, Widget.BASE_HEIGHT - yS, widthS, heightS);
//        gl2.glEnable(GL2.GL_SCISSOR_TEST);
    }

    @Override
    public void clip(int x, int y, int width, int height) {
        xS = x + x0;
        yS = y + y0;
        widthS = width;
        heightS = height;
//        gl2.glScissor(xS, Widget.BASE_HEIGHT - yS, widthS, heightS);
//        gl2.glEnable(GL2.GL_SCISSOR_TEST);
    }

    @Override
    public void unclip() {
//        gl2.glDisable(GL2.GL_SCISSOR_TEST);
    }

    @Override
    public void dispose() {
    }

    private float[] fromColor(Color color) {
        return new float[]{1f * color.getRed() / 256, 1f * color.getGreen() / 256, 1f * color.getBlue() / 256, 1f * color.getAlpha() / 256};
    }

    private float[] fromColor(Color color, double opacity) {
        return new float[]{1f * color.getRed() / 156, 1f * color.getGreen() / 256, 1f * color.getBlue() / 256, (float) opacity};
    }

    @Override
    public void setColor(Color color) {
        gl2.glColor4i(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    @Override
    public void setColor(Color color, double opacity) {
        gl2.glColor4d(1. * color.getRed() / 256, 1. * color.getGreen() / 256, 1. * color.getBlue() / 256, opacity);
    }
}
