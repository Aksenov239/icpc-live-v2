package ru.ifmo.acm.mainscreen.statuses;

import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.datapassing.TeamData;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.EventsLoader;
import ru.ifmo.acm.events.PCMS.PCMSTeamInfo;
import ru.ifmo.acm.events.TeamInfo;

import java.util.Arrays;

public class TeamStatus {
//    public final ContestInfo info;
    public String[] teamNames;
    private long changeTime;

    public TeamStatus(long changeTime) {
//        EventsLoader loader = EventsLoader.getInstance();
//        info = loader.getContestData();
//        TeamInfo[] teamInfos = info.getStandings();
//        while (teamInfos == null) {
//            teamInfos = info.getStandings();
//        }
//        teamNames = new String[teamInfos.length];
//        int l = 0;
//        for (int i = 0; i < teamNames.length; i++) {
//            if (((PCMSTeamInfo) teamInfos[i]).getAlias().startsWith("S")) {
//                teamNames[l++] = teamInfos[i].getShortName() + " :" + ((PCMSTeamInfo) teamInfos[i]).getAlias();
//            }
//        }
//        teamNames = Arrays.copyOf(teamNames, l);
//        Arrays.sort(teamNames);
        teamNames = new String[1];
        teamNames[0] = "Team";
        this.changeTime = changeTime;
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
            if (infoTeam != null && ((((PCMSTeamInfo) infoTeam).getAlias().equals(alias)
//             && isInfoVisible)) {
                    || infoTimestamp + changeTime > System.currentTimeMillis()) && isInfoVisible)) {
                return false;
            }
            infoTimestamp = System.currentTimeMillis();
            isInfoVisible = visible;
            infoType = type;
//            infoTeam = info.getParticipant(alias);
//            System.err.println(alias + " " + infoTeam.getId());
        } else {
            isInfoVisible = false;
            infoTimestamp = System.currentTimeMillis();
        }

        recache();
        return true;
    }

    public synchronized boolean isVisible() {
        return isInfoVisible;
    }

    public synchronized String getTeamString() {
        return infoTeam.getShortName() + " :" + ((PCMSTeamInfo) infoTeam).getAlias();
    }

    public synchronized String infoStatus() {
        return infoTimestamp + "\n" + isInfoVisible + "\n" + infoType + "\n" + (infoTeam == null ? null : infoTeam.getName());
    }

    public synchronized void initialize(TeamData data) {
        data.timestamp = infoTimestamp;
        data.isTeamVisible = isInfoVisible;
        data.infoType = infoType;
        data.teamId = infoTeam == null ? -1 : infoTeam.getId();
    }

    private long infoTimestamp;
    private boolean isInfoVisible;
    private String infoType;
    private TeamInfo infoTeam;
}
