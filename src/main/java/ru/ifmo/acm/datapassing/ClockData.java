package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.mainscreen.MainScreenData;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class ClockData implements CachedData {
    public ClockData() {
        isVisible = true;
    }

    public void recache() {
        Data.cache.refresh(ClockData.class);
    }

    public synchronized void setClockVisible(boolean visible) {
        timestamp = System.currentTimeMillis();
        isVisible = visible;
        recache();
    }

    public synchronized boolean isClockVisible() {
        return isVisible;
    }

    public synchronized String clockStatus() {
        return timestamp + "\n" + isVisible;
    }

    public ClockData initialize() {
        ClockData data = MainScreenData.getMainScreenData().clockData;
        this.timestamp = data.timestamp;
        this.isVisible = data.isVisible;

        return this;
    }

    private long timestamp;
    private boolean isVisible;
}
