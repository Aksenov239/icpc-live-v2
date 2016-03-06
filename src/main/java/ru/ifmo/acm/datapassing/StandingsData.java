package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.backend.player.widgets.StandingsWidget;
import ru.ifmo.acm.events.EventsLoader;
import ru.ifmo.acm.mainscreen.MainScreenData;

public class StandingsData implements CachedData {
    @Override
    public StandingsData initialize() {
        StandingsData data = MainScreenData.getMainScreenData().standingsData;
        this.timestamp = data.timestamp;
        this.isVisible = data.isVisible;
        this.standingsType = data.standingsType;
        this.isBig = data.isBig;

        return this;
    }

    public String toString() {
        long time = standingsType == StandingsType.ONE_PAGE
                ? (System.currentTimeMillis() - timestamp) / 1000
                : (timestamp + getTotalTime(standingsType) - System.currentTimeMillis()) / 1000;
        return String.format(standingsType.label, time) + (isBig() ? ". Big standings are shown" : ". Compact standings are shown");
    }

    public long getLatency() {
        return latency;
    }

    public void recache() {
        Data.cache.refresh(StandingsData.class);
    }

    public void setStandingsVisible(boolean visible, StandingsType type, boolean isBig) {
        synchronized (standingsLock) {
            timestamp = System.currentTimeMillis();
            isVisible = visible;
            standingsType = type;
            this.isBig = isBig;
        }

        recache();
    }

    public static long getTotalTime(StandingsType type) {
        return StandingsWidget.totalTime(type, EventsLoader.getInstance().getContestData().getTeamsNumber()) + latency;
    }

    public void update() {
        boolean change = false;
        synchronized (standingsLock) {
            //System.err.println(PCMSEventsLoader.getInstance().getContestData().getTeamsNumber());
            if (System.currentTimeMillis() > timestamp +
                    getTotalTime(standingsType)) {
                isVisible = false;
                standingsType = StandingsType.HIDE;
                change = true;
            }
        }
        if (change)
            recache();
    }

    public long getStandingsTimestamp() {
        return timestamp;
    }

    public boolean isStandingsVisible() {
        return isVisible;
    }

    public StandingsType getStandingsType() {
        return standingsType;
    }

    public boolean isBig() {
        return isBig;
    }

    public void setBig(boolean big) {
        isBig = big;
    }

    public long timestamp;
    public boolean isVisible;
    public StandingsType standingsType = StandingsType.HIDE;
    public boolean isBig;

    public static long latency;

    final private Object standingsLock = new Object();

    public enum StandingsType {
        ONE_PAGE("Top 1 page is shown for %d seconds"),
        TWO_PAGES("Top 2 pages are remaining for %d seconds"),
        ALL_PAGES("All pages are remaining for %d seconds"),
        HIDE("Standings aren't shown");

        public final String label;

        StandingsType(String label) {
            this.label = label;
        }
    }
}
