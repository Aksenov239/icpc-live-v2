package ru.ifmo.acm.backend.player.widgets;

import com.jogamp.common.net.Uri;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.av.GLMediaPlayer;
import com.jogamp.opengl.util.av.GLMediaPlayerFactory;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.backend.opengl.GraphicsGL;
import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author: Aksenov239
 */
public class VideoGLWidget extends PlayerWidget {
    private GLMediaPlayer player;
    private GLMediaPlayer nextPlayer;

    protected boolean ready;

    protected String currentUrl;
    protected String nextUrl;

    protected Texture currentTexture;
    protected Texture nextTexture;

    private GL2 gl;

    private long changeTimestamp;

    public VideoGLWidget(int x, int y, int width, int height, int sleepTime, long updateWait) {
        super(updateWait);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.sleepTime = sleepTime;
        ready = true;
    }

    private GLMediaPlayer createPlayer(String url) {
        GLMediaPlayer player = GLMediaPlayerFactory.create(GLMediaPlayer.class.getClassLoader(), "jogamp.opengl.util.av.impl.FFMPEGMediaPlayer");
        try {
            player.initStream(Uri.cast(url),
                    GLMediaPlayer.STREAM_ID_AUTO, GLMediaPlayer.STREAM_ID_AUTO, GLMediaPlayer.TEXTURE_COUNT_DEFAULT);
        } catch (java.net.URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
        return player;
    }

    public Texture loadTexture(String url) {
        if (url.endsWith(".png") || url.endsWith(".jpg")) {
            System.err.println("Load by hand!");
            try {
                Texture result = TextureIO.newTexture(new File(url), false);
                changeTimestamp = System.currentTimeMillis(); // to do the same as for normal video
                ready = false;
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void changeManually(String url) {
        if (url == null) {
            nextUrl = null;
            return;
        }
        nextPlayer = createPlayer(url);
        nextUrl = url;
        nextTexture = loadTexture(url);
    }

    public void switchManually() {
        if (nextUrl == null) {
            stopCurrent();
            return;
        }
        stopCurrent();
        player = nextPlayer;
        currentUrl = nextUrl;
        currentTexture = nextTexture;
        nextPlayer = null;
        nextUrl = null;
        nextTexture = null;
    }

    public void change(String url) {
        log.info("Change to " + url);
        if (url == null) {
            currentUrl = null;
            stop();
            return;
        }
        ready = false;
        nextPlayer = createPlayer(url);
        nextUrl = url;
        nextTexture = loadTexture(url);
    }

    public void setVolume(int volume) {
        player.setAudioVolume(1f * volume / 100);
    }

    private void stopCurrent() {
        if (player != null) {
            player.destroy(gl);
            player = null;
        }
        currentUrl = null;
        currentTexture = null;
    }

    private void stopNext() {
        if (nextPlayer != null) {
            nextPlayer.destroy(gl);
            nextPlayer = null;
        }
        nextUrl = null;
        nextTexture = null;
    }

    public void stop() {
        stopCurrent();

        stopNext();
    }

    public boolean readyToShow() {
        return ready;
    }

    public String getCurrentURL() {
        return currentUrl;
    }

    public double getAspectRatio() {
        return 1. * player.getWidth() / player.getHeight();
    }

    public void paintImpl(Graphics g, int width, int height) {
        draw(g);
    }

    private ByteBuffer currentBuffer;
    private ByteBuffer convertedBuffer;
    private TextureData currentTextureData;

    public Texture processTexture() {
        if (player == null) {
            return null;
        }
        if (player.getState() != GLMediaPlayer.State.Playing) {
            return currentTexture;
        }

        GL2GL3 gl = this.gl;

        Texture texture = player.getNextTexture(gl).getTexture();

        texture.enable(gl);
        texture.bind(gl);

        int width = texture.getWidth();
        int height = texture.getHeight();
        if (currentBuffer == null || currentBuffer.capacity() != 3 * width * height) {
            currentBuffer = ByteBuffer.allocate(3 * width * height);
        } else {
            currentBuffer.clear();
        }
        // To store previous picture, done for YUV, that is why 3
        gl.glGetTexImage(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, currentBuffer);
        byte[] original = currentBuffer.array();

        texture.disable(gl);

        int resWidth = player.getWidth();
        int resHeight = player.getHeight();
        if (convertedBuffer == null || convertedBuffer.capacity() != 3 * resWidth * resHeight) {
            convertedBuffer = ByteBuffer.allocate(3 * resWidth * resHeight);
        } else {
            convertedBuffer.clear();
        }
        // YUV420p
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

                convertedBuffer.put((byte) r);
                convertedBuffer.put((byte) g);
                convertedBuffer.put((byte) b);
            }
        }
        convertedBuffer.flip();
        if (currentTextureData == null || currentTextureData.getWidth() != resWidth ||
                currentTextureData.getHeight() != resHeight) {
            currentTextureData = new TextureData(gl.getGLProfile(), GL.GL_RGB, resWidth, resHeight, 0,
                    GL.GL_RGB, GL.GL_UNSIGNED_BYTE,
                    false, false, true, convertedBuffer, null);
        } else {
            currentTextureData.setBuffer(convertedBuffer);
        }

        if (currentTexture == null) {
            currentTexture = new Texture(gl, currentTextureData);
        } else {
            currentTexture.updateImage(gl, currentTextureData);
        }
        return currentTexture;
    }

    public void draw(Graphics g) {
        if (processTexture() == null)
            return;
        g.drawTexture(processTexture(), x, y, this.width, this.height);
    }

    public void draw(Graphics g, int x, int y, int width, int height) {
        if (processTexture() == null)
            return;
        g.drawTexture(processTexture(), x, y, width, height);
    }

    public void updateState(Graphics g, boolean manualSwitch) {
        if (gl == null) {
            gl = ((GraphicsGL) g).getGL();
        }
        if (nextPlayer == null) {
            return;
        }
        switch (nextPlayer.getState()) {
            case Initialized:
                if (nextTexture == null) { // The video is not a picture
                    try {
                        nextPlayer.initGL(gl);
                    } catch (GLMediaPlayer.StreamException e) {
                        log.info("Could not initialise the stream " + nextUrl);
                        nextPlayer.destroy(gl);
                        nextPlayer = null;
                        nextUrl = null;
                    }
                    break;
                }
            case Paused:
                if (nextTexture == null) { // The video is not a picture
                    nextPlayer.play();
                    changeTimestamp = System.currentTimeMillis();
                    ready = false;
                    break;
                }
            case Playing:
                if (manualSwitch || System.currentTimeMillis() - changeTimestamp < sleepTime) {
                    break;
                }
                switchManually();

                inChange = true;
                ready = true;
        }
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return null;
    }

}
