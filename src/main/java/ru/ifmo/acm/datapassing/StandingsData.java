package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.mainscreen.MainScreenData;
import ru.ifmo.acm.mainscreen.statuses.StandingsStatus;

public class StandingsData implements CachedData {
    @Override
    public StandingsData initialize() {
        StandingsStatus status = MainScreenData.getMainScreenData().standingsStatus;
        status.initialize(this);

        return this;
    }

    public StandingsData(long standingsTimestamp, boolean isStandingsVisible, int standingsType) {
        this.standingsTimestamp = standingsTimestamp;
        this.isStandingsVisible = isStandingsVisible;
        this.standingsType = standingsType;
    }

    public long standingsTimestamp;
    public boolean isStandingsVisible;
    public int standingsType;
}
