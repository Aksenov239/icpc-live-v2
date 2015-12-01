package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.mainscreen.MainScreenData;
import ru.ifmo.acm.mainscreen.statuses.StandingsStatus;
import ru.ifmo.acm.mainscreen.statuses.TeamStatus;

public class TeamData implements CachedData {
    @Override
    public TeamData initialize() {
        TeamStatus status = MainScreenData.getMainScreenData().teamStatus;
        status.initialize(this);

        return this;
    }

    public long timestamp;
    public boolean isTeamVisible;
    public int teamId;
    public String infoType;
}
