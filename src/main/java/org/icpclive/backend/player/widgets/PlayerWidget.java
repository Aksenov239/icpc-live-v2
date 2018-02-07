package org.icpclive.backend.player.widgets;

import org.icpclive.backend.graphics.AbstractGraphics;

/**
 * Created by aksenov on 28.04.2015.
 */
public abstract class PlayerWidget extends Widget {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected int sleepTime;
    protected boolean inChange;

    public PlayerWidget(long updateWait) {
        super(updateWait);
    }

    public static PlayerWidget getPlayerWidget(int x, int y,
                                               int width, int height,
                                               int sleepTime, int updateWait) {
//        return new VideoGLWidget(x, y, width, height, sleepTime, updateWait);
        return new VideoVLCWidget(x, y, width, height, sleepTime, updateWait);
    }

    public abstract void draw(AbstractGraphics g);

    public abstract void draw(AbstractGraphics g, int x, int y, int width, int height);

    public abstract void draw(AbstractGraphics g, int x, int y, int width, int height, double opacity);

    public abstract void change(String url);

    public abstract void setVolume(int volume);

    public abstract void loadNext(String url);

    public abstract void switchToNext();

    public boolean nextIsReady() {
        throw new AssertionError("nextIsReady is not implemented");
    }

    public abstract boolean readyToShow();

    public abstract void stop();

    public abstract String getCurrentURL();

    public abstract double getAspectRatio();

    public void updateState(AbstractGraphics g, boolean manualSwitch) {
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }
}
