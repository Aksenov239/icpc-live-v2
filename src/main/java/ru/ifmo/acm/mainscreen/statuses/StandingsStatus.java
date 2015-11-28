package ru.ifmo.acm.mainscreen.statuses;

import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.datapassing.StandingsData;

public class StandingsStatus {
    public void recache() {
        Data.cache.refresh(StandingsData.class);
    }

    public void setStandingsVisible(boolean visible, int type) {
        synchronized (standingsLock) {
            standingsTimestamp = System.currentTimeMillis();
            isStandingsVisible = visible;
            standingsType = type;
        }
        recache();
    }

    public String standingsStatus() {
        synchronized (standingsLock) {
            return standingsTimestamp + "\n" + isStandingsVisible + "\n" + standingsType;
        }
    }

    public long getStandingsTimestamp() {
        synchronized (standingsLock) {
            return standingsTimestamp;
        }
    }

    public boolean isStandingsVisible() {
        synchronized (standingsLock) {
            return isStandingsVisible;
        }
    }

    public long getStandingsType() {
        synchronized (standingsLock) {
            return standingsType;
        }
    }

    public void initialize(StandingsData data) {
        synchronized (standingsLock) {
            data.standingsTimestamp = standingsTimestamp;
            data.isStandingsVisible = isStandingsVisible;
            data.standingsType = standingsType;
        }
    }

    private long standingsTimestamp;
    private boolean isStandingsVisible;
    private long standingsType;
    final private Object standingsLock = new Object();
}
