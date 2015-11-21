package ru.ifmo.acm.mainscreen.statuses;

import ru.ifmo.acm.events.EventsLoader;
import ru.ifmo.acm.events.PCMS.PCMSEventsLoader;
import ru.ifmo.acm.events.TeamInfo;
import ru.ifmo.acm.events.ContestInfo;
import java.util.Arrays;

public class TeamStatus {
    public final ContestInfo info;
    public final String[] teamNames;

    public TeamStatus(){
        EventsLoader loader = PCMSEventsLoader.getInstance();
        info = loader.getContestData();
        TeamInfo[] teamInfos = info.getStandings();
        teamNames = new String[teamInfos.length];
        for (int i = 0; i < teamNames.length; i++) {
            teamNames[i] = teamInfos[i].getShortName();
        }
        Arrays.sort(teamNames);
    }

    public synchronized void setInfoVisible(boolean visible, String type, String teamName) {
        infoTimestamp = System.currentTimeMillis();
        isInfoVisible = visible;
        infoType = type;
        infoTeam = info.getParticipant(teamName);
    }

    public synchronized String infoStatus() {
        return infoTimestamp + "\n" + isInfoVisible + "\n" + infoType + "\n" + infoTeam.getName();
    }

    private long infoTimestamp;
    private boolean isInfoVisible;
    private String infoType;
    private TeamInfo infoTeam;
}
