package ru.ifmo.acm.backend.opengl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.glu.GLU;
import ru.ifmo.acm.backend.Preparation;
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
    }

    public void dispose(GLAutoDrawable drawable) {
    }

    public void addWidget(Widget widget) {
        widgets.add(widget);
    }
}
