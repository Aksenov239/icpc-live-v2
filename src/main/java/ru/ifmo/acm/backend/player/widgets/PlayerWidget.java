package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.graphics.Graphics;

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
        return new VideoGLWidget(x, y, width, height, sleepTime, updateWait);
//        return new VideoVLCWidget(x, y, width, height, sleepTime, updateWait);
    }

    public abstract void draw(Graphics g);

    public abstract void draw(Graphics g, int x, int y, int width, int height);

    public abstract void change(String url);

    public abstract void setVolume(int volume);

    public abstract void changeManually(String url);

    public abstract void switchManually();

    public abstract boolean readyToShow();

    public abstract void stop();

    public abstract String getCurrentURL();

    public void updateState(Graphics g, boolean manualSwitch) {
    }
}
