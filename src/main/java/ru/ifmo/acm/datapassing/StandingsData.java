package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.backend.player.widgets.StandingsWidget;
import ru.ifmo.acm.events.PCMS.PCMSEventsLoader;
import ru.ifmo.acm.mainscreen.MainScreenData;

public class StandingsData implements CachedData {
    @Override
    public StandingsData initialize() {
        StandingsData data = MainScreenData.getMainScreenData().standingsData;
        this.timestamp = data.timestamp;
        this.isVisible = data.isVisible;
        this.standingsType = data.standingsType;

        return this;
    }

    public String toString() {
        if (isVisible) {
            long time = standingsType == 0
                    ? (System.currentTimeMillis() - timestamp) / 1000
                    : (timestamp + getTotalTime(standingsType) - System.currentTimeMillis()) / 1000;
            return String.format(labelStatuses[standingsType], time);
        }
        return labelStatuses[3];
    }

    private final static String[] labelStatuses = new String[]{
            "Top 1 page is shown for %d seconds",
            "Top 2 pages are remaining for %d seconds",
            "All pages are remaining for %d seconds",
            "Standings aren't shown"
    };

    public long getLatency() {
        return latency;
    }

    public void recache() {
        Data.cache.refresh(StandingsData.class);
    }

    public void setStandingsVisible(boolean visible, int type) {
        synchronized (standingsLock) {
            timestamp = System.currentTimeMillis();
            isVisible = visible;
            standingsType = type;
        }

        recache();
    }

    public static long getTotalTime(int type) {
        return StandingsWidget.totalTime(type, PCMSEventsLoader.getInstance().getContestData().getTeamsNumber()) + latency;
    }

    public void update() {
        boolean change = false;
        synchronized (standingsLock) {
            //System.err.println(PCMSEventsLoader.getInstance().getContestData().getTeamsNumber());
            if (System.currentTimeMillis() > timestamp +
                    getTotalTime(standingsType)) {
                isVisible = false;
                change = true;
            }
        }
        if (change)
            recache();
    }

    public long getStandingsTimestamp() {
        synchronized (standingsLock) {
            return timestamp;
        }
    }

    public boolean isStandingsVisible() {
        synchronized (standingsLock) {
            return isVisible;
        }
    }

    public long getStandingsType() {
        synchronized (standingsLock) {
            return standingsType;
        }
    }

    public long timestamp;
    public boolean isVisible;
    public int standingsType;

    public static long latency;

    final private Object standingsLock = new Object();
}
