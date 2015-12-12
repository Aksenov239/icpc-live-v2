package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.player.PlayerInImage;
import uk.co.caprica.vlcj.player.MediaPlayer;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: pashka
 */
public abstract class VideoWidget extends Widget implements PlayerWidget {
    protected PlayerInImage player;
    protected BufferedImage image;
    protected AtomicBoolean inChange;
    protected AtomicBoolean ready;
    protected AtomicBoolean stopped;
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected int sleepTime;
    protected String URL;

    public VideoWidget(int x, int y, int width, int height, int sleepTime) {
        this.URL = null;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        player = new PlayerInImage(width, height, null, null);
        image = player.getImage();
        this.sleepTime = sleepTime;
        inChange = new AtomicBoolean();
        ready = new AtomicBoolean(true);
        stopped = new AtomicBoolean();
    }

    public void change(final String url) {
        if (url == null) {
            if (!stopped.get()) {
                URL = null;
                stop();
            }
            return;
        }
        new Thread() {
            public void run() {
                ready.set(false);
                PlayerInImage player2 = new PlayerInImage(width, height, null, url);
                try {
                    Thread.sleep(sleepTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                JComponent component = player.getComponent();
                player.setComponent(null);
                player2.setComponent(component);
                inChange.set(true);
                player.stop();
                player = player2;
                image = player2.getImage();
                ready.set(true);
                URL = url;
                stopped.set(false);
            }
        }.start();
    }

    public void stop() {
        if (player != null && !stopped.get() && getPlayer().isPlaying()) {
            player.stop();
        }
        stopped.set(true);
        URL = null;
    }

    public boolean readyToShow() {
        return ready.get();
    }

    public MediaPlayer getPlayer() {
        return player.getPlayer();
    }
}
