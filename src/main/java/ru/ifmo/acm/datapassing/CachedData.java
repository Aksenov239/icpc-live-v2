package ru.ifmo.acm.datapassing;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public abstract class CachedData {
    abstract CachedData initialize();

    public String checkOverlays() {
        return null;
    }

    public void switchOverlaysOff() {
    }

    public void hide() {
    }

    public String getOverlayError() {
        return "";
    }

    public long delay = 0;
    public long timestamp = 0;
}
