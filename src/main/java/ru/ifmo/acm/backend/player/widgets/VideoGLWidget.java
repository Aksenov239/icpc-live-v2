package ru.ifmo.acm.backend.player.widgets;

import com.jogamp.common.net.Uri;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.av.GLMediaPlayer;
import com.jogamp.opengl.util.av.GLMediaPlayerFactory;
import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.backend.opengl.GraphicsGL;
import ru.ifmo.acm.backend.player.PlayerInImage;
import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;

import javax.swing.*;

/**
 * @author: Aksenov239
 */
public class VideoGLWidget extends PlayerWidget {
    private GLMediaPlayer player;
    private GLMediaPlayer nextPlayer;

    protected boolean ready;

    protected String currentUrl;
    protected String nextUrl;

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

    public void changeManually(String url) {
        if (url == null) {
            nextUrl = null;
            return;
        }
        nextPlayer = createPlayer(url);
        nextUrl = url;
    }

    public void switchManually() {
        if (nextUrl == null) {
            stopCurrent();
            return;
        }
        stopCurrent();
        player = nextPlayer;
        currentUrl = nextUrl;
        nextPlayer = null;
        nextUrl = null;
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
    }

    private void stopNext() {
        if (nextPlayer != null) {
            nextPlayer.destroy(gl);
            nextPlayer = null;
        }
        nextUrl = null;
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

    public void paintImpl(Graphics g, int width, int height) {
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawTexture(player.getNextTexture(gl).getTexture(), x, y, this.width, this.height);
    }

    public void draw(Graphics g, int x, int y, int width, int height) {
        g.drawTexture(player.getNextTexture(gl).getTexture(), x, y, width, height);
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
                try {
                    nextPlayer.initGL(gl);
                } catch (GLMediaPlayer.StreamException e) {
                    log.info("Could not initialise the stream " + nextUrl);
                    nextPlayer.destroy(gl);
                    nextPlayer = null;
                    nextUrl = null;
                }
                break;
            case Paused:
                nextPlayer.play();
                changeTimestamp = System.currentTimeMillis();
                ready = false;
                break;
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
