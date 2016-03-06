package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.events.PCMS.PCMSTeamInfo;
import ru.ifmo.acm.events.TeamInfo;
import ru.ifmo.acm.mainscreen.MainScreenData;

public class TeamData implements CachedData {
    public TeamData() {
    }

    @Override
    public TeamData initialize() {
        TeamData data = MainScreenData.getMainScreenData().teamData;
        this.timestamp = data.timestamp;
        this.isVisible = data.isVisible;
        this.infoType = data.infoType;
        this.teamInfo = data.teamInfo;

        return this;
    }

    public void recache() {
        Data.cache.refresh(TeamData.class);
    }

    public synchronized boolean setInfoVisible(boolean visible, String type, String teamName) {
        if (visible) {
            String alias = null;
            if (teamName != null) {
                alias = teamName.split(":")[1];
            }
            if (teamInfo != null && ((((PCMSTeamInfo) teamInfo).getAlias().equals(alias)
//             && isInfoVisible)) {
                    || timestamp + MainScreenProperties.getInstance().sleepTime > System.currentTimeMillis()) && isVisible)) {
                return false;
            }
            timestamp = System.currentTimeMillis();
            isVisible = true;
            infoType = type;
            teamInfo = MainScreenProperties.getInstance().contestInfo.getParticipant(alias);
            System.err.println(alias + " " + teamInfo.getId());
        } else {
            isVisible = false;
            timestamp = System.currentTimeMillis();
        }

        recache();
        return true;
    }

    public synchronized boolean isVisible() {
        return isVisible;
    }

    public synchronized String getTeamString() {
        return teamInfo.getShortName() + " :" + ((PCMSTeamInfo)teamInfo).getAlias();
    }

    public synchronized String infoStatus() {
        return timestamp + "\n" + isVisible + "\n" + infoType + "\n" + (teamInfo == null ? null : teamInfo.getName());
    }

    public synchronized int getTeamId() {
        return teamInfo.getId();
    }

    public long timestamp;
    public boolean isVisible;
    public String infoType;

    private TeamInfo teamInfo;
}
