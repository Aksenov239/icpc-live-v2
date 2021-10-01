package org.icpclive.backend.player.widgets;

import org.icpclive.Config;
import org.icpclive.backend.Preparation;
import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.player.urls.TeamUrls;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.events.RunInfo;
import org.icpclive.events.TeamInfo;
import org.icpclive.events.WF.WFContestInfo;
import org.icpclive.events.WF.WFRunInfo;

import java.io.IOException;
import java.util.Properties;

/**
 * @author: pashka
 */
public class SplitScreenWidget extends Widget {
    final TeamWidget[] teamInfoWidgets = new TeamWidget[4];
    final String[] currentInfoType = new String[4];
    final boolean[] automatic = new boolean[4];
    private long switchTime;
    private long switchIdTime;
    private long replayTime;
    private long sleepTime;
    private long relevanceTime;
    private long[] lastSwitch = new long[4];
    private View[] lastView = new View[4];
    private String defaultType;
    private int currentRunId;
    private int[] interestingTeams;
    private int topPlaces;
    private TwitterBasedQueue usersQueue;
    private int mode = 0;

    public void initialization() {
        Properties properties = new Properties();
        try {
            properties = Config.loadProperties("splitscreen");
//            properties.load(this.getClass().getClassLoader().getResourceAsStream("splitscreen.properties"));
        } catch (IOException e) {
            log.error("error", e);
        }

        switchTime = Integer.parseInt(properties.getProperty("switch.time"));
        switchIdTime = Integer.parseInt(properties.getProperty("switch.id.time"));
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
            lastView[i] = new View(Preparation.eventsLoader.getContestData().getParticipant(teamId), defaultType);
            teamInfoWidgets[i].change(lastView[i].teamInfo, lastView[i].infoType);
            lastSwitch[i] = System.currentTimeMillis() + i * switchTime / 4;
        }

        System.err.println("Twitter based queue");
        usersQueue = TwitterBasedQueue.getInstance();
        currentRunId = 1;
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
                    (teamInfoWidgets[i].team != null &&
                            teamInfoWidgets[i].team.getId() == teamId)) {
                return true;
            }
        }
        return false;
    }

    private int currentPlace = 0;
    private int predefinedTeam = 0;
    private boolean[] allRuns = new boolean[1000000];

    protected void chooseNewStream(int widget) {
        teamInfoWidgets[widget].switchNext();

        WFContestInfo contestInfo = (WFContestInfo) Preparation.eventsLoader.getContestData();
        RunInfo replayRun = null;
        // TODO: when frozen always switch onto teamId screen
        System.err.println("Choosing new stream for widget " + widget);
        while (allRuns[currentRunId]) {
            currentRunId++;
        }
        int runId = currentRunId;
        while (runId <= contestInfo.getLastRunId() && replayRun == null) {
            WFRunInfo run = contestInfo.getRun(runId);
//            if (run != null)
//                System.err.println((long) (1000 * runId.time + " " + System.currentTimeMillis() + " " +
//                        (long) (System.currentTimeMillis() - 1000 * runId.timestamp) + " " + runId + " " +
//                        runId.isAccepted());
            if (run != null) {
                if (run.time + relevanceTime > contestInfo.getCurrentTime() &&
//                    runId.getLastUpdateTime() + relevanceTime > System.currentTimeMillis() &&
                        run.isAccepted() &&
                        !allRuns[runId]) {
                    replayRun = run;
                    allRuns[runId] = true;
                } else {
                    if (run.isJudged() || run.time >= WFContestInfo.FREEZE_TIME) {
                        allRuns[runId] = true;
                    }
                }
            }
            // TODO: because replays does not work we do not want to show the same team
            if (replayRun != null && teamInUse(replayRun.getTeamId())) {
                replayRun = null;
            }
            runId++;
        }

        if (replayRun != null) {
            log.info("Found replay " + replayRun);
            System.err.println("Found replay " + replayRun);
//            TODO: while replay do not work
//            teamInfoWidgets[widget].change(replayRun);
//            teamInfoWidgets[widget].change(
//                    replayRun,
//                    contestInfo.getParticipant(replayRun.getTeamId())
//            );
            View newView = new View(contestInfo.getParticipant(replayRun.getTeamId()), "camera");
            teamInfoWidgets[widget].change(
                    newView.teamInfo, newView.infoType
            );
            lastView[widget] = newView;
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
        View newView = new View(contestInfo.getParticipant(teamId), infoType);
        teamInfoWidgets[widget].change(
                newView.teamInfo, newView.infoType
        );
        lastView[widget] = newView;
        //log.info("There " + teamInfoWidgets[widget].teamId + " " + teamInfoWidgets[widget].team.getId());
        lastSwitch[widget] = System.currentTimeMillis();
        mode ^= 1;
    }

    private void switchIds() {
        for (int i = 0; i < 4; i++) {
            if (System.currentTimeMillis() > lastView[i].lastSwitchIdTime + switchIdTime - sleepTime) {
                lastView[i].nextId();
                teamInfoWidgets[i].change(lastView[i].teamInfo, lastView[i].infoType, lastView[i].id);
            }
        }
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
                        teamInfoWidgets[i].mainVideo.readyToShow()) {
                    View newView = new View(Preparation.eventsLoader.getContestData().getParticipant(data.splitScreenData.getTeamId(i)),
                            data.splitScreenData.controllerDatas[i].infoType);
                    teamInfoWidgets[i].setTeamId(newView.teamInfo.getId());
                    teamInfoWidgets[i].mainVideo.change(
                            TeamUrls.getUrl(
                                    newView.teamInfo,
                                    newView.infoType
                            )
                    );
                    lastView[i] = newView;
                }
            }
        }
        switchIds();
    }

    @Override
    public void paintImpl(AbstractGraphics g, int width, int height) {
        update();
        for (int i = 0; i < teamInfoWidgets.length; i++) {
            teamInfoWidgets[i].paintImpl(g, width, height);
        }
    }

    @Override
    public CachedData getCorrespondingData(Data data) {
        return data.splitScreenData;
    }

    public class View {
        TeamInfo teamInfo;
        String infoType;
        int id;
        long lastSwitchIdTime;

        public View(TeamInfo teamInfo, String infoType) {
            this.teamInfo = teamInfo;
            this.infoType = infoType;
            this.lastSwitchIdTime = System.currentTimeMillis();
        }

        public void nextId() {
            id++;
            lastSwitchIdTime = System.currentTimeMillis();
        }
    }

}
