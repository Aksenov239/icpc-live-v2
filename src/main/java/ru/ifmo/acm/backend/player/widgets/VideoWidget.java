package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.player.PlayerInImage;
import uk.co.caprica.vlcj.player.MediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: pashka
 */
public abstract class VideoWidget extends Widget implements PlayerWidget {
    private PlayerInImage player;
    protected AtomicReference<BufferedImage> image;
    protected AtomicBoolean inChange;
    protected AtomicBoolean ready;
    protected AtomicBoolean stopped;
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected int sleepTime;
    protected AtomicReference<String> URL;

    public VideoWidget(int x, int y, int width, int height, int sleepTime, long updateWait) {
        super(updateWait);
        this.URL = new AtomicReference<>(null);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        player = new PlayerInImage(width, height, null, null);
        image = new AtomicReference<BufferedImage>(player.getImage());
        this.sleepTime = sleepTime;
        inChange = new AtomicBoolean();
        ready = new AtomicBoolean(true);
        stopped = new AtomicBoolean();
    }

    public void change(final String url) {
        if (url == null) {
            if (!stopped.get()) {
                URL.set(null);
                stop();
            }
            return;
        }
        ready.set(false);
        new Thread() {
            public void run() {
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
                if (!stopped.get()) {
                    player.stop();
                }
                player = player2;
                image.set(player2.getImage());
                ready.set(true);
                URL.set(url);
                stopped.set(false);
            }
        }.start();
    }

    public void stop() {
        if (player != null && !stopped.get()) {
            player.stop();
        }
        stopped.set(true);
        URL.set(null);
    }

    public boolean readyToShow() {
        return ready.get();
    }

    public MediaPlayer getPlayer() {
        return player.getPlayer();
    }

}
