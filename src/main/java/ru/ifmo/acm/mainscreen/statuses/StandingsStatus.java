package ru.ifmo.acm.mainscreen.statuses;

public class StandingsStatus {
    public synchronized void setStandingsVisible(boolean visible, int type) {
        synchronized (standingsLock) {
            standingsTimestamp = System.currentTimeMillis();
            isStandingsVisible = visible;
            standingsType = type;
        }
    }

    public synchronized String standingsStatus() {
        synchronized (standingsLock) {
            return standingsTimestamp + "\n" + isStandingsVisible + "\n" + standingsType;
        }
    }

    public long getStandingsTimestamp() {
        return standingsTimestamp;
    }

    public boolean isStandingsVisible() {
        return isStandingsVisible;
    }

    public long getStandingsType() {
        return standingsType;
    }

    private long standingsTimestamp;
    private boolean isStandingsVisible;
    private long standingsType;
    final private Object standingsLock = new Object();
}
