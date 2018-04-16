package org.icpclive.backend.player.widgets;

import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.player.PlayerInImage;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * @author: pashka
 */
public class VideoVLCWidget extends PlayerWidget {
    private PlayerInImage player;
    protected BufferedImage image;

    private PlayerInImage nextPlayer;
    private BufferedImage nextImage;

    protected boolean ready;

    protected String currentUrl;
    private String nextUrl;

    public VideoVLCWidget(int x, int y, int width, int height, int sleepTime, long updateWait) {
        super(updateWait);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        player = new PlayerInImage(width, height, null, null);
        image = player.getImage();
        this.sleepTime = sleepTime;
        ready = true;
    }

    public void loadNext(String url) {
//        SwingUtilities.invokeLater(() -> {
            if (url == null) {
                nextUrl = null;
                return;
            }
            nextPlayer = new PlayerInImage(width, height, null, url);
            nextImage = nextPlayer.getImage();
            nextUrl = url;
//        });
    }

    public void switchToNext() {
//        checkEDT();
        if (nextUrl == null) {
            stop();
            return;
        }
        JComponent component = player.getComponent();

        PlayerInImage oldPlayer = player;

        player.setComponent(null);
        nextPlayer.setComponent(component);
        inChange = true;

        player = nextPlayer;
        image = nextImage;
        currentUrl = nextUrl;

        oldPlayer.stop();
        nextPlayer = null;
        nextImage = null;
        nextUrl = null;
    }

    public void change(String url) {
        log.info("Change to " + url);
//        SwingUtilities.invokeLater(() -> {
            if (url == null) {
                currentUrl = null;
                stop();
                return;
            }
            ready = false;
            loadNext(url);
            Timer timer = new Timer(sleepTime, (a) -> {
                switchToNext();
                ready = true;
            });
            timer.setRepeats(false);
            timer.start();
//        });
    }

    // If 20 random points do not contain black the the video has been loaded
    public boolean isBlack(BufferedImage image) {
        Random rnd = new Random();
        for (int i = 0; i < 20; i++) {
            if (image.getRGB(rnd.nextInt(image.getWidth()),
                    rnd.nextInt(image.getHeight())) != 0) {
                return false;
            }
        }
        return true;
    }

    // If 20 random points do not contain black then the video has been loaded
    public boolean nextIsReady() {
        return nextImage != null && !isBlack(nextImage);
    }

    public void setVolume(int volume) {
//        checkEDT();
        player.setVolume(volume);
    }

    public void stop() {
//        SwingUtilities.invokeLater(() -> {
            if (player != null && currentUrl != null) {
                player.stop();
            }
            currentUrl = null;
            if (nextPlayer != null && nextUrl != null) {
                nextPlayer.stop();
            }
            nextUrl = null;
//        });
    }

    public boolean readyToShow() {
//        checkEDT();
        return ready;
    }

    public String getCurrentURL() {
        return currentUrl;
    }

    public double getAspectRatio() {
        if (currentUrl.startsWith("http")) {
            return 16. / 9;
        }
        return player.getPlayer().getAspectRatio().equals("16:9") ? 16. / 9 : 4. / 3;
    }

    public void paintImpl(AbstractGraphics g, int width, int height) {
        draw(g);
    }

    public void draw(AbstractGraphics g) {
        g.drawImage(player.getImage(), x, y, this.width, this.height);
    }

    public void draw(AbstractGraphics g, int x, int y, int width, int height) {
        g.drawImage(player.getImage(), x, y, width, height);
    }

    public void draw(AbstractGraphics g, int x, int y, int width, int height, double opacity) {
        g.drawImage(player.getImage(), x, y, width, height, opacity);
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return null;
    }

    private void checkEDT() {
        if (!SwingUtilities.isEventDispatchThread()) {
            IllegalStateException e = new IllegalStateException();
            log.error("Not in EDT!", e);
            throw e;
        }
    }

}
