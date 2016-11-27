package testing;

import com.jogamp.common.net.Uri;
import com.jogamp.common.util.InterruptSource;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.av.GLMediaPlayer;
import com.jogamp.opengl.util.av.GLMediaPlayerFactory;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureSequence;
import testing.jogl.MovieSimple;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

/**
 * Created by Aksenov239 on 23.11.2016.
 */
public class PlayerGL implements GLEventListener {

    /* If FFMPEG is in libav folder */
//    static {
//        String directory = "C:/work/svn/icpc-live-v2/libav/x64/";
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


    static class MyGLMediaEventListener implements GLMediaPlayer.GLMediaEventListener {
        void destroyWindow(final com.jogamp.newt.Window window) {
            new InterruptSource.Thread() {
                public void run() {
                    window.destroy();
                }
            }.start();
        }

        @Override
        public void newFrameAvailable(final GLMediaPlayer ts, final TextureSequence.TextureFrame newFrame, final long when) {
        }

        @Override
        public void attributesChanged(final GLMediaPlayer mp, final int event_mask, final long when) {
            System.err.println("MovieSimple AttributesChanges: events_mask 0x" + Integer.toHexString(event_mask) + ", when " + when);
            System.err.println("MovieSimple State: " + mp);
            final GLWindow window = (GLWindow) mp.getAttachedObject(MovieSimple.WINDOW_KEY);
            final PlayerGL ms = (PlayerGL) mp.getAttachedObject(MovieSimple.PLAYER);
            if (0 != (GLMediaPlayer.GLMediaEventListener.EVENT_CHANGE_SIZE & event_mask)) {
                System.err.println("MovieSimple State: CHANGE_SIZE");
            }
            if (0 != (GLMediaPlayer.GLMediaEventListener.EVENT_CHANGE_INIT & event_mask)) {
                System.err.println("MovieSimple State: INIT");
                // Use GLEventListener in all cases [A+V, V, A]
                window.addGLEventListener(ms);
                final GLAnimatorControl anim = window.getAnimator();
                anim.setUpdateFPSFrames(60, null);
                anim.resetFPSCounter();
            }
            if (0 != (GLMediaPlayer.GLMediaEventListener.EVENT_CHANGE_PLAY & event_mask)) {
                window.getAnimator().resetFPSCounter();
            }

            boolean destroy = false;
            Throwable err = null;

            if (0 != (GLMediaPlayer.GLMediaEventListener.EVENT_CHANGE_EOS & event_mask)) {
                err = ms.player.getStreamException();
                if (null != err) {
                    System.err.println("MovieSimple State: EOS + Exception");
                    destroy = true;
                } else {
                    System.err.println("MovieSimple State: EOS");
                    destroy = true;
                }
            }
            if (0 != (GLMediaPlayer.GLMediaEventListener.EVENT_CHANGE_ERR & event_mask)) {
                err = ms.player.getStreamException();
                if (null != err) {
                    System.err.println("MovieSimple State: ERR + Exception");
                } else {
                    System.err.println("MovieSimple State: ERR");
                }
                destroy = true;
            }
            if (destroy) {
                if (null != err) {
                    err.printStackTrace();
                }
                destroyWindow(window);
            }
        }
    }

    public final static MyGLMediaEventListener myGLMediaEventListener = new MyGLMediaEventListener();

    public GLMediaPlayer player;
    public Texture textureToShow;

    int cnt = 0;

    public void translateToProperFormat(GL2GL3 gl, Texture texture) {
        int width = texture.getWidth();
        int height = texture.getHeight();
        ByteBuffer buffer = ByteBuffer.allocate(3 * width * height);
        // To store previous picture, done for YUV, that is why 3
        gl.glGetTexImage(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, buffer);
        byte[] original = buffer.array();

        int resWidth = player.getWidth();
        int resHeight = player.getHeight();
        byte[] convertedBuffer = new byte[3 * resWidth * resHeight];
        // YUV420p
        int p = 0;
        for (int y = 0; y < resHeight; y++) {
            for (int x = 0; x < resWidth; x++) {
                int yc = y * width + x;
                int uc = (y / 2) * width + (resWidth + x / 2);
                int vc = (y / 2 + height / 2) * width + (resWidth + x / 2);

                int yy = original[3 * yc] & 0xFF;
                int u = original[3 * uc] & 0xFF;
                int v = original[3 * vc] & 0xFF;

                if (yy < 0 || yy > 255 || u < 0 || u > 255 || v < 0 || v > 255) {
                    throw new AssertionError();
                }

                int r = (298 * (yy - 16) + 409 * (v - 128) + 128) >> 8;
                int g = (298 * (yy - 16) - 100 * (u - 128) - 208 * (v - 128) + 128) >> 8;
                int b = (298 * (yy - 16) + 516 * (u - 128) + 128) >> 8;

                r = Math.max(Math.min(r, 255), 0);
                g = Math.max(Math.min(g, 255), 0);
                b = Math.max(Math.min(b, 255), 0);

                convertedBuffer[p++] = (byte) r;
                convertedBuffer[p++] = (byte) g;
                convertedBuffer[p++] = (byte) b;
            }
        }

        TextureData data = new TextureData(gl.getGLProfile(), GL.GL_RGB, resWidth, resHeight, 0,
                GL.GL_RGB, GL.GL_UNSIGNED_BYTE
                , false, false, true, ByteBuffer.wrap(convertedBuffer), null);
        if (textureToShow == null) {
            textureToShow = new Texture(gl, data);
        } else {
            textureToShow.updateImage(gl, data);
        }
    }

    public PlayerGL() {
        player = GLMediaPlayerFactory.create(GLMediaPlayer.class.getClassLoader(), "jogamp.opengl.util.av.impl.FFMPEGMediaPlayer");
        try {
            player.initStream(
//                    Uri.cast("http://archive.org/download/BigBuckBunny_328/BigBuckBunny_512kb.mp4"),
                    Uri.cast("pics/BigBuckBunny_320x180.mp4"),
                    GLMediaPlayer.STREAM_ID_AUTO, GLMediaPlayer.STREAM_ID_AUTO, 4);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        player.attachObject(MovieSimple.PLAYER, this);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2ES2 gl = drawable.getGL().getGL2ES2();

        player.setTextureMinMagFilter(new int[]{GL.GL_NEAREST, GL.GL_LINEAR});

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

        gl.glEnable(GL.GL_DEPTH_TEST);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        player.destroy(drawable.getGL().getGL2());
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        Texture texture = player.getNextTexture(gl).getTexture();

        texture.enable(gl);
        texture.bind(gl);

        translateToProperFormat(gl, texture);

        texture.disable(gl);

//        cnt++;
//        if (cnt % 20 == 1 && textureToShow != null) {
//            try {
//                TextureIO.write(textureToShow, new File(String.format("shots/picture-%d.jpg", cnt)));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        if (cnt == 100) {
//            System.exit(0);
//        }

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        textureToShow.enable(gl);
        textureToShow.bind(gl);

        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(0, 0, 1);
        gl.glTexCoord2d(0, 1);
        gl.glVertex2d(0, 0);
        gl.glTexCoord2d(1, 1);
        gl.glVertex2d(1, 0);
        gl.glTexCoord2d(1, 0);
        gl.glVertex2d(1, 1);
        gl.glTexCoord2d(0, 0);
        gl.glVertex2d(0, 1);
        gl.glEnd();

        textureToShow.disable(gl);
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
        canvas.setPreferredSize(new Dimension(320, 180));
        final FPSAnimator animator = new FPSAnimator(canvas, 20, true);
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
