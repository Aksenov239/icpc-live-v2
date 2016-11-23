package testing;

import com.jogamp.common.net.Uri;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.av.GLMediaPlayer;
import com.jogamp.opengl.util.av.GLMediaPlayerFactory;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URISyntaxException;

/**
 * Created by Aksenov239 on 23.11.2016.
 */
public class PlayerGL implements GLEventListener {

    /* If FFMPEG is in libav folder */
//    static {
//        String directory = ".../libav/x64/";
//
//        System.load(directory + "avutil-55.dll");
//        System.load(directory + "postproc-54.dll");
//        System.load(directory + "swscale-4.dll");
//        System.load(directory + "swresample-2.dll");
//        System.load(directory + "avcodec-57.dll");
//        System.load(directory + "avformat-57.dll");
//        System.load(directory + "avfilter-6.dll");
//        System.load(directory + "avdevice-57.dll");
//    }

    GLMediaPlayer player;

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        player = GLMediaPlayerFactory.create(GLMediaPlayer.class.getClassLoader(), "jogamp.opengl.util.av.impl.FFMPEGMediaPlayer");
        try {
            player.initStream(Uri.cast("pics/BigBuckBunny_320x180.mp4"),
                    GLMediaPlayer.STREAM_ID_AUTO, GLMediaPlayer.STREAM_ID_AUTO, 4);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        while (player.getState() != GLMediaPlayer.State.Initialized) {
        }
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

    @Override
    public void dispose(GLAutoDrawable drawable) {
        player.destroy(drawable.getGL().getGL2());
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_TEST);

        Texture texture = player.getNextTexture(gl).getTexture();

        texture.enable(gl);
        texture.bind(gl);

        TextureCoords coords = texture.getImageTexCoords();

        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2d(coords.left(), coords.bottom());
        gl.glVertex2d(0, 0);
        gl.glTexCoord2d(coords.right(), coords.bottom());
        gl.glVertex2d(1, 0);
        gl.glTexCoord2d(coords.right(), coords.top());
        gl.glVertex2d(1, 1);
        gl.glTexCoord2d(0, coords.top());
        gl.glVertex2d(0, 1);
        gl.glEnd();

        texture.disable(gl);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, 1, 0, 1, 0, 1);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public static void main(String[] args) {
        GLCanvas canvas = new GLCanvas();
        canvas.addGLEventListener(new PlayerGL());
        canvas.setPreferredSize(new Dimension(1280, 720));
        final FPSAnimator animator = new FPSAnimator(canvas, 10, true);
        final JFrame frame = new JFrame();
        frame.getContentPane().add(canvas);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                new Thread() {
                    public void run() {
                        animator.stop();
                        System.exit(0);
                    }
                }.start();
            }
        });

        animator.start();

        frame.setTitle("Player");
        frame.pack();
        frame.setVisible(true);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
