package ru.ifmo.acm.backend.opengl;

import com.jogamp.graph.curve.Region;
import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.RenderState;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.font.Font;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.graph.geom.SVertex;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.PMVMatrix;
import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.backend.graphics.GraphicsSWT;
import ru.ifmo.acm.backend.player.widgets.Widget;

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
//        Preparation.prepareNetwork(properties.getProperty("login", null), properties.getProperty("password", null));
    }

    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        GraphicsGL.renderState = RenderState.createRenderState(SVertex.factory());
//        GraphicsGL.renderState.setColorStatic(1, 1, 1, 1);
//        GraphicsGL.renderState.setHintMask(RenderState.BITHINT_BLENDING_ENABLED);

        GraphicsGL.regionRenderer = RegionRenderer.create(GraphicsGL.renderState, RegionRenderer.defaultBlendEnable, RegionRenderer.defaultBlendDisable);
        GraphicsGL.regionRenderer.init(gl, Region.DEFAULT_TWO_PASS_TEXTURE_UNIT);

        GraphicsGL.textRegionUtil = new TextRegionUtil(Region.DEFAULT_TWO_PASS_TEXTURE_UNIT);

        GraphicsGL.regionRenderer.enable(gl, true);
        GraphicsGL.regionRenderer.reshapeOrtho(width, height, 0.1f, 1000f);
        GraphicsGL.regionRenderer.enable(gl, false);
        GraphicsGL.loadFonts();

        glu = new GLU();
        gl.glClearDepth(1f);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        if (height == 0) height = 1;
        this.width = width;
        this.height = height;
        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, Widget.BASE_WIDTH, 0, Widget.BASE_HEIGHT, -1, 1);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        GraphicsGL.regionRenderer.enable(gl, true);
        GraphicsGL.regionRenderer.reshapeOrtho(width, height, 0.1f, 1000.0f);
        GraphicsGL.regionRenderer.enable(gl, false);
    }

    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        GraphicsGL g = new GraphicsGL(0, 0, width, height, gl, glu);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glClearColor(1, 0, 1, 0);

        for (Widget widget : widgets) {
            widget.paint(g, width, height);
        }
//        PMVMatrix pmv = GraphicsGL.renderState.getMatrix();
//        pmv.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
//        pmv.glLoadIdentity();
//        pmv.glTranslatef(0, 0, -300);
//        Font font = GraphicsGL.fonts.get("Open Sans Light");
//        float pixelSize = font.getPixelSize(32, 96);
//        GraphicsGL.textRegionUtil.drawString3D(drawable.getGL().getGL2(),
//                GraphicsGL.regionRenderer, font, pixelSize, "Test", null, new int[]{4});
    }

    public void dispose(GLAutoDrawable drawable) {
        GraphicsGL.regionRenderer.destroy(drawable.getGL().getGL2());
    }

    public void addWidget(Widget widget) {
        widgets.add(widget);
    }
}
