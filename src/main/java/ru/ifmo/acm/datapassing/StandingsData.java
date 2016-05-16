package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.backend.player.widgets.BigStandingsWidget;
import ru.ifmo.acm.backend.player.widgets.StandingsWidget;
import ru.ifmo.acm.events.EventsLoader;
import ru.ifmo.acm.mainscreen.MainScreenData;
import ru.ifmo.acm.mainscreen.MainScreenProperties;

public class StandingsData implements CachedData {
    @Override
    public StandingsData initialize() {
        StandingsData data = MainScreenData.getMainScreenData().standingsData;
        this.timestamp = data.timestamp;
        this.isVisible = data.isVisible;
        this.standingsType = data.standingsType;
        this.optimismLevel = data.optimismLevel;
        this.isBig = data.isBig;

        return this;
    }

    public String toString() {
        if (standingsType == StandingsType.HIDE) {
            return standingsType.label;
        }

        long time = standingsType == StandingsType.ONE_PAGE
                ? (System.currentTimeMillis() - timestamp) / 1000
                : (timestamp + getTotalTime(isBig, standingsType) - System.currentTimeMillis()) / 1000;
        return String.format(standingsType.label, time) + ". " +
                optimismLevel.toString() +
                (isBig() ? " big standings are shown" : " compact standings are shown");
    }

    public void recache() {
        Data.cache.refresh(StandingsData.class);
    }

    public String checkOverlays() {
        if (MainScreenData.getMainScreenData().teamData.isVisible) {
            return "You need to close team view first";
        }
        return null;
    }

    public void hide() {
        isVisible = false;
        recache();
    }

    public String setStandingsVisible(boolean visible, StandingsType type, boolean isBig, OptimismLevel level) {
        String outcome = checkOverlays();
        if (outcome != null) {
            return outcome;
        }
        synchronized (standingsLock) {
            timestamp = System.currentTimeMillis();
            isVisible = visible;
            standingsType = type;
            optimismLevel = level;
            this.isBig = isBig;
        }

        recache();
        return null;
    }

    public static long getTotalTime(boolean isBig, StandingsType type) {
        return isBig ?
                BigStandingsWidget.totalTime(type, EventsLoader.getInstance().getContestData().getTeamsNumber()) + MainScreenData.getProperties().latency :
                StandingsWidget.totalTime(type, EventsLoader.getInstance().getContestData().getTeamsNumber()) + MainScreenData.getProperties().latency;
    }

    public void update() {
        boolean change = false;
        synchronized (standingsLock) {
            //System.err.println(PCMSEventsLoader.getInstance().getContestData().getTeamsNumber());
            if (System.currentTimeMillis() > timestamp +
                    getTotalTime(isBig, standingsType)) {
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
    public OptimismLevel optimismLevel = OptimismLevel.NORMAL;

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

    public enum OptimismLevel {
        NORMAL,
        OPTIMISTIC,
        PESSIMISTIC;

        public String toString() {
            switch (this) {
                case NORMAL:
                    return "Normal";
                case OPTIMISTIC:
                    return "Optimistic";
                case PESSIMISTIC:
                    return "Pessimistic";

                default:
                    throw new IllegalArgumentException();
            }
        }
    }
}
