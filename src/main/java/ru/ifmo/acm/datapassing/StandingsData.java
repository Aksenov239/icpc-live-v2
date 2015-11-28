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

    public long standingsTimestamp;
    public boolean isStandingsVisible;
    public long standingsType;
}
