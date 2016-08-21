package ru.ifmo.acm.backend.opengl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.glu.GLU;
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

    public WidgetManager(Properties properties) {
//        Preparation.prepareEventsLoader();
//        Preparation.prepareDataLoader();
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
        gl.glViewport(0, 0, width, height);
    }

    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glClearColor(1, 0, 1, 0);
    }

    public void dispose(GLAutoDrawable drawable) {
    }

    public void addWidget(Widget widget) {
        widgets.add(widget);
    }
}
