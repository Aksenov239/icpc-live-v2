package ru.ifmo.acm.mainscreen.statuses;

import ru.ifmo.acm.backend.player.widgets.StandingsWidget;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.datapassing.StandingsData;
import ru.ifmo.acm.events.PCMS.PCMSEventsLoader;

public class StandingsStatus {
    private int teamNumber;
    private static long latency;

    public StandingsStatus(long latency) {
        this.latency = latency;
    }

    public long getLatency() {
        return latency;
    }

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

    public static long getTotalTime(int type) {
        return StandingsWidget.totalTime(type, PCMSEventsLoader.getInstance().getContestData().getTeamsNumber()) + latency;
    }

    public void update() {
        boolean change = false;
        synchronized (standingsLock) {
            //System.err.println(PCMSEventsLoader.getInstance().getContestData().getTeamsNumber());
            if (System.currentTimeMillis() > standingsTimestamp +
                    getTotalTime(standingsType)) {
                isStandingsVisible = false;
                change = true;
            }
        }
        if (change)
            recache();
    }

    public StandingsData standingsStatus() {
        synchronized (standingsLock) {
            // return standingsTimestamp + "\n" + isStandingsVisible + "\n" + standingsType;
            return new StandingsData().initialize();
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
    private int standingsType;
    final private Object standingsLock = new Object();
}
