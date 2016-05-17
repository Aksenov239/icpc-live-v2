package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.backend.player.urls.TeamUrls;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;
import ru.ifmo.acm.events.WF.WFContestInfo;

import java.awt.*;
import java.io.IOException;
import java.util.Properties;
import ru.ifmo.acm.datapassing.CachedData;

/**
 * @author: pashka
 */
public class SplitScreenWidget extends Widget {
    final TeamWidget[] teamInfoWidgets = new TeamWidget[4];
    final String[] currentInfoType = new String[4];
    final boolean[] automatic = new boolean[4];
    private long switchTime;
    private long replayTime;
    private long sleepTime;
    private long relevanceTime;
    private long[] lastSwitch = new long[4];
    private String defaultType;
    private int currentRunId;
    private int[] interestingTeams;
    private int topPlaces;
    private TwitterBasedQueue usersQueue;
    private int mode = 0;

    public void initialization() {
        Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream("splitscreen.properties"));
        } catch (IOException e) {
            log.error("error", e);
        }

        switchTime = Integer.parseInt(properties.getProperty("switch.time"));
        relevanceTime = Integer.parseInt(properties.getProperty("relevance.time"));
        replayTime = Integer.parseInt(properties.getProperty("replay.time"));
        topPlaces = Integer.parseInt(properties.getProperty("top.places"));
        String[] showSetup = properties.getProperty("setup").split(",");
        interestingTeams = new int[showSetup.length];
        for (int i = 0; i < interestingTeams.length; i++) {
            interestingTeams[i] = Integer.parseInt(showSetup[i]) - 1;
        }
        defaultType = properties.getProperty("default.type", "screen");
        TeamInfo[] standings = Preparation.eventsLoader.getContestData().getStandings();
        for (int i = 0; i < 4; i++) {
            int teamId;
            if (standings[i].getSolvedProblemsNumber() == 0) {
                teamId = Integer.parseInt(showSetup[predefinedTeam++]) - 1;
            } else {
                teamId = standings[i].getId();
            }
            teamInfoWidgets[i].setVisible(true);
            teamInfoWidgets[i].change(
                    Preparation.eventsLoader.getContestData().getParticipant(teamId), defaultType);
            lastSwitch[i] = System.currentTimeMillis() + i * switchTime / 4;
        }

        usersQueue = TwitterBasedQueue.getInstance();
    }

    public SplitScreenWidget(long updateWait, int width, int height, double aspectRatio, int sleepTime) {
        super(updateWait);
        for (int i = 0; i < 4; i++) {
            teamInfoWidgets[i] = new TeamWidget(
                    (width / 2) * (i & 1),
                    (height / 2) * (i / 2),
                    width / 2,
                    height / 2,
                    aspectRatio,
                    sleepTime,
                    true
            );
            automatic[i] = true;
        }
        initialization();
        this.sleepTime = sleepTime;
    }

    private boolean teamInUse(int teamId) {
        for (int i = 0; i < teamInfoWidgets.length; i++) {
            if (teamInfoWidgets[i].teamId == teamId ||
                    teamInfoWidgets[i].team.getId() == teamId) {
                return true;
            }
        }
        return false;
    }

    private int currentPlace = 0;
    private int predefinedTeam = 0;

    protected void chooseNewStream(int widget) {
        WFContestInfo contestInfo = (WFContestInfo) Preparation.eventsLoader.getContestData();
        RunInfo replayRun = null;
        // TODO: when frozen always switch onto teamId screen
        while (currentRunId <= contestInfo.getMaxRunId() && replayRun == null) {
            if (contestInfo.getRun(currentRunId) != null &&
//                    contestInfo.getRun(currentRunId).timestamp * 1000 + relevanceTime > System.currentTimeMillis() &&
                    contestInfo.getRun(currentRunId).getLastUpdateTimestamp() * 1000 + relevanceTime > System.currentTimeMillis() &&
                    contestInfo.getRun(currentRunId).isAccepted()) {
                replayRun = contestInfo.getRun(currentRunId);
            }
            currentRunId++;
        }
        if (replayRun != null) {
            teamInfoWidgets[widget].change(replayRun);
            lastSwitch[widget] = System.currentTimeMillis() - switchTime + replayTime;
            return;
        }
        TeamInfo[] standings = contestInfo.getStandings();
        int teamId;
        String infoType = defaultType;
        while (true) {
            if (mode == 0) {
                TwitterBasedQueue.Request request = usersQueue.nextRequest();
                if (request != null) {
                    //log.info("MODE 0 " + request.teamId);
                    teamId = request.teamId;
                    infoType = request.type;
                    if (teamInUse(teamId)) {
                        continue;
                    }
                    break;
                }
            }
            if (standings[currentPlace].getSolvedProblemsNumber() == 0) {
                if (!teamInUse(interestingTeams[predefinedTeam])) {
                    teamId = interestingTeams[predefinedTeam];
                    predefinedTeam = (predefinedTeam + 1) % interestingTeams.length;
                    currentPlace = (currentPlace + 1) % topPlaces;
                    break;
                }
                predefinedTeam = (predefinedTeam + 1) % interestingTeams.length;
            } else {
                if (!teamInUse(standings[currentPlace].getId())) {
                    teamId = standings[currentPlace].getId();
                    currentPlace = (currentPlace + 1) % topPlaces;
                    break;
                }
                currentPlace = (currentPlace + 1) % topPlaces;
            }
        }
        //log.info("Choose " + teamId + " for " + widget + " with mode " + mode);
        teamInfoWidgets[widget].change(
                contestInfo.getParticipant(teamId),
                infoType
        );
        //log.info("There " + teamInfoWidgets[widget].teamId + " " + teamInfoWidgets[widget].team.getId());
        lastSwitch[widget] = System.currentTimeMillis();
        mode ^= 1;
    }

    @Override
    protected void updateImpl(Data data) {
        for (int i = 0; i < 4; i++) {
            if (data.splitScreenData.isAutomatic[i]) {
                if (!automatic[i]) {
                    automatic[i] = true;
                    lastSwitch[i] = System.currentTimeMillis() + switchTime - sleepTime;
                }

                if (System.currentTimeMillis() > lastSwitch[i] + switchTime) {
                    chooseNewStream(i);
                }
            } else {
                if (data.splitScreenData.getTeamId(i) == -1) {
                    if (System.currentTimeMillis() > lastSwitch[i] + switchTime) {
                        chooseNewStream(i);
                    }
                    continue;
                }
                automatic[i] = false;
                if ((data.splitScreenData.getTeamId(i) != teamInfoWidgets[i].getTeamId()
                        && !data.splitScreenData.infoStatus(i).equals(currentInfoType[i])) &&
                        teamInfoWidgets[i].readyToShow()) {
                    teamInfoWidgets[i].setTeamId(data.splitScreenData.getTeamId(i));
                    teamInfoWidgets[i].change(
                            TeamUrls.getUrl(
                                    Preparation.eventsLoader.getContestData().getParticipant(data.splitScreenData.getTeamId(i)),
                                    data.splitScreenData.controllerDatas[i].infoType
                            )
                    );
                }
            }
        }
    }

    @Override
    public void paintImpl(Graphics2D g, int width, int height) {
        update();
        for (int i = 0; i < teamInfoWidgets.length; i++) {
            teamInfoWidgets[i].paintImpl(g, width, height);
        }
    }

    @Override
    public CachedData getCorrespondingData(Data data){
        return data.splitScreenData;
    }

}
