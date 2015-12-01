package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.mainscreen.MainScreenData;
import ru.ifmo.acm.mainscreen.statuses.StandingsStatus;

import static ru.ifmo.acm.mainscreen.statuses.StandingsStatus.getTotalTime;

public class StandingsData implements CachedData {
    @Override
    public StandingsData initialize() {
        StandingsStatus status = MainScreenData.getMainScreenData().standingsStatus;
        status.initialize(this);

        return this;
    }

//    public StandingsData() {
//    }
//
//    public StandingsData(long standingsTimestamp, boolean isStandingsVisible, int standingsType) {
//        this.standingsTimestamp = standingsTimestamp;
//        this.isStandingsVisible = isStandingsVisible;
//        this.standingsType = standingsType;
//    }


    public String toString() {
        if (isStandingsVisible) {
            long time = standingsType == 0
                    ? (System.currentTimeMillis() - standingsTimestamp) / 1000
                    : (standingsTimestamp + getTotalTime(standingsType) - System.currentTimeMillis()) / 1000;
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

    public long standingsTimestamp;
    public boolean isStandingsVisible;
    public int standingsType;
}
