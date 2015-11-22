package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.mainscreen.MainScreenData;
import ru.ifmo.acm.mainscreen.statuses.StandingsStatus;

public class StandingsData implements CachedData {
    @Override
    public StandingsData initialize() {
        StandingsStatus status = MainScreenData.getMainScreenData().standingsStatus;
        standingsTimestamp = status.getStandingsTimestamp();
        isStandingsVisible = status.isStandingsVisible();
        standingsType = status.getStandingsType();

        return this;
    }

    public long standingsTimestamp;
    public boolean isStandingsVisible;
    public long standingsType;
}
