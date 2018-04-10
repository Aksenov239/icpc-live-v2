package org.icpclive.datapassing;

import org.icpclive.backend.player.widgets.BigStandingsWidget;
import org.icpclive.backend.player.widgets.StandingsWidget;
import org.icpclive.events.EventsLoader;
import org.icpclive.webadmin.mainscreen.MainScreenData;

public class StandingsData extends CachedData {
    @Override
    public StandingsData initialize() {
        StandingsData data = MainScreenData.getMainScreenData().standingsData;
        this.timestamp = data.timestamp;
        this.isVisible = data.isVisible;
        this.standingsType = data.standingsType;
        this.optimismLevel = data.optimismLevel;
        this.isBig = data.isBig;
        this.delay = data.delay;
        this.region = data.region;
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
                (isBig() ? " big standings" : " compact standings") +
                " of " + region + " region are shown";
    }

    public void recache() {
        Data.cache.refresh(StandingsData.class);
    }

    public String checkOverlays() {
        if (MainScreenData.getMainScreenData().teamData.isVisible) {
            return MainScreenData.getMainScreenData().teamData.getOverlayError();
        }
        return null;
    }

    public void switchOverlaysOff() {
        if (isBig) {
            MainScreenData mainScreenData = MainScreenData.getMainScreenData();
            if (mainScreenData.statisticsData.isVisible()) {
                delay = MainScreenData.getProperties().overlayedDelay;
                mainScreenData.statisticsData.hide();
            }
            if (mainScreenData.pollData.isVisible) {
                delay = MainScreenData.getProperties().overlayedDelay;
                mainScreenData.pollData.hide();
            }
        }
    }

    public void hide() {
        delay = 0;
        synchronized (standingsLock) {
            isVisible = false;
            timestamp = System.currentTimeMillis();
        }
        recache();
    }

    public String getOverlayError() {
        return "You need to hide standings first!";
    }

    public String setStandingsVisible(boolean visible, StandingsType type, boolean isBig, String region, OptimismLevel level) {
        delay = 0;
        if (visible) {
            String outcome = checkOverlays();
            this.isBig = isBig;
            if (outcome != null) {
                return outcome;
            }
            switchOverlaysOff();
        }
        synchronized (standingsLock) {
            timestamp = System.currentTimeMillis();
            isVisible = visible;
            standingsType = type;
            if (isBig) {
                this.region = region;
            } else {
                this.region = ALL_REGIONS;
            }
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

    public boolean isVisible;
    public StandingsType standingsType = StandingsType.HIDE;
    public boolean isBig;
    public OptimismLevel optimismLevel = OptimismLevel.NORMAL;
    public String region = ALL_REGIONS;

    final private Object standingsLock = new Object();

    public static final String ALL_REGIONS = "all";

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
