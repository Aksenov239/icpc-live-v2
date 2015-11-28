package ru.ifmo.acm.mainscreen.statuses;

import ru.ifmo.acm.datapassing.ClockData;
import ru.ifmo.acm.datapassing.Data;

public class ClockStatus {
    public ClockStatus() {
        isClockVisible = true;
    }

    public void recache() {
        Data.cache.refresh(ClockData.class);
    }

    public synchronized void setClockVisible(boolean visible) {
        clockTimestamp = System.currentTimeMillis();
        isClockVisible = visible;
        recache();
    }

    public synchronized boolean isClockVisible() {
        return isClockVisible;
    }

    public synchronized String clockStatus() {
        return clockTimestamp + "\n" + isClockVisible;
    }

    public synchronized void initialize(ClockData status){
        status.timestamp = clockTimestamp;
        status.isVisible = isClockVisible;
    }

    private long clockTimestamp;
    private boolean isClockVisible;
    final private Object clockLock = new Object();
}
