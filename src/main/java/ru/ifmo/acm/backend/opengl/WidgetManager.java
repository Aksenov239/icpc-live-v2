package ru.ifmo.acm.backend.opengl;

import com.jogamp.common.net.Uri;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.av.GLMediaPlayer;
import com.jogamp.opengl.util.av.GLMediaPlayerFactory;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.backend.player.widgets.Widget;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Aksenov239 on 22.08.2016.
 */
public class WidgetManager implements GLEventListener {
    private List<Widget> widgets = new ArrayList<>();
    private GLU glu;
    private int width;
    private int height;

    public WidgetManager(Properties properties) {
        Preparation.prepareEventsLoader();
        Preparation.prepareDataLoader();
        Preparation.prepareNetwork(properties.getProperty("login", null), properties.getProperty("password", null));
    }

    TextRenderer renderer;
    GLMediaPlayer player;

    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

//        GraphicsGL.renderState = RenderState.createRenderState(SVertex.factory());
////        GraphicsGL.renderState.setColorStatic(1, 0, 1, 1);
//        GraphicsGL.renderState.setHintMask(RenderState.BITHINT_BLENDING_ENABLED
//                | RenderState.BITHINT_GLOBAL_DEPTH_TEST_ENABLED);
//
//        GraphicsGL.regionRenderer = RegionRenderer.create(GraphicsGL.renderState,
//                RegionRenderer.defaultBlendEnable, RegionRenderer.defaultBlendDisable);
////        GraphicsGL.regionRenderer.init(gl, Region.DEFAULT_TWO_PASS_TEXTURE_UNIT);
//        GraphicsGL.regionRenderer.init(gl, Region.MAX_QUALITY |
//                Region.COLORCHANNEL_RENDERING_BIT);
//
////        GraphicsGL.textRegionUtil = new TextRegionUtil(Region.DEFAULT_TWO_PASS_TEXTURE_UNIT);
//        GraphicsGL.textRegionUtil = new TextRegionUtil(Region.MAX_QUALITY |
//                Region.COLORCHANNEL_RENDERING_BIT);
////        GraphicsGL.textRegionUtil.setCacheLimit(0);
//
//        GraphicsGL.regionRenderer.enable(gl, true);
//        GraphicsGL.regionRenderer.reshapeOrtho(width, height, 0.1f, 1000f);
//        GraphicsGL.regionRenderer.enable(gl, false);
//        GraphicsGL.loadFonts();

        glu = new GLU();
        gl.glClearDepth(1f);
        gl.glEnable(GL.GL_DEPTH_TEST);// | GL.GL_BLEND);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
        gl.glShadeModel(GLLightingFunc.GL_SMOOTH);

        renderer = new TextRenderer(Font.decode("Open Sans 18"));

        player = GLMediaPlayerFactory.create(GLMediaPlayer.class.getClassLoader(), "jogamp.opengl.util.av.impl.FFMPEGMediaPlayer");
        try {
            player.initStream(Uri.cast("C:/work/svn/icpc-live-v2/pics/BigBuckBunny_320x180.mp4"),
                    GLMediaPlayer.STREAM_ID_AUTO, GLMediaPlayer.STREAM_ID_AUTO, GLMediaPlayer.TEXTURE_COUNT_DEFAULT);
        } catch (java.net.URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        while (player.getState() != GLMediaPlayer.State.Initialized) {}
        try {
            player.initGL(gl);
        } catch (GLMediaPlayer.StreamException e) {
            e.printStackTrace();
        }
        while (player.getState() != GLMediaPlayer.State.Paused) {
        }
        player.play();
        while (player.getState() != GLMediaPlayer.State.Playing) {
        }
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        if (height == 0) height = 1;
        this.width = width;
        this.height = height;
        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, Widget.BASE_WIDTH, 0, Widget.BASE_HEIGHT, 0, 1);
//        gl.glOrtho(0, width, 0, height, 0, 1);
//        glu.gluPerspective(45.0, 1. * width / height, 0.1, 100.0); // fovy, aspect, zNear, zFar

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

//        GraphicsGL.regionRenderer.enable(gl, true);
//        GraphicsGL.regionRenderer.reshapeOrtho(width, height, 0.1f, 1000.0f);
//        GraphicsGL.regionRenderer.enable(gl, false);
    }

    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        GraphicsGL g = new GraphicsGL(0, 0, width, height, gl, glu);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glClearColor(1, 0, 1, 0);
//        gl.glClearColor(1, 1, 1, 1);

//        gl.glPushMatrix();
//        gl.glTranslatef(100f, 0, 0);
//        gl.glScaled(2, 1, 0);
//        TextRenderer renderer = new TextRenderer(java.awt.Font.decode("Open Sans 18"));
//        renderer.begin3DRendering();
//        renderer.setColor(1, 1, 1, 1);
//        renderer.draw("TestTestTest", 0, 0);
//        renderer.end3DRendering();
//        gl.glPopMatrix();

        for (Widget widget : widgets) {
            widget.paint(g, Widget.BASE_WIDTH, Widget.BASE_HEIGHT);
        }
//        PMVMatrix pmv = GraphicsGL.renderState.getMatrix();
//        pmv.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
//        pmv.glLoadIdentity();
//        pmv.glTranslatef(0, 0, -300);
//        Font font = null;
//        try {
//            font = FontFactory.getDefault().getDefault();//GraphicsGL.fonts.get("Open Sans Light");
//        } catch (IOException e) {}
//        float pixelSize = font.getPixelSize(32, 96);
////        GraphicsGL.regionRenderer.enable(gl, true);
//        GraphicsGL.textRegionUtil.drawString3D(gl,
//                GraphicsGL.regionRenderer, font, pixelSize, "Test4", new float[] {1f, 1f, 1f, 1f}, new int[]{4});
////        TextRegionUtil.drawString3D(gl, Region.MAX_QUALITY | Region.COLORCHANNEL_RENDERING_BIT,
////                GraphicsGL.regionRenderer, font, pixelSize, "Test",
////                new float[] {1, 1, 1, 1}, new int[] {4}, new AffineTransform(), new AffineTransform());
////        GraphicsGL.regionRenderer.enable(gl, false);
    }

    public void dispose(GLAutoDrawable drawable) {
//        GraphicsGL.regionRenderer.destroy(drawable.getGL().getGL2());
    }

    public void addWidget(Widget widget) {
        widgets.add(widget);
    }
}
