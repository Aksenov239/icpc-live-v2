package ru.ifmo.acm.events.WF;

import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by aksenov on 05.05.2015.
 */
public class WFContestInfo extends ContestInfo {
    private ArrayBlockingQueue<RunInfo> runs;
    String[] languages;
    private WFTeamInfo[] teamInfos;
    public long[] timeFirstSolved;
    int teamNumber;
    int problemNumber;

    private WFTeamInfo[] standings;

    public WFContestInfo() {
        teamInfos = new WFTeamInfo[200];
        timeFirstSolved = new long[20];
        languages = new String[4];
        runs = new ArrayBlockingQueue<RunInfo>(1000000);
    }

    public void shrink(int teamNumber, int problemNumber, int languageNumber) {
        teamInfos = Arrays.copyOf(teamInfos, teamNumber);
        timeFirstSolved = Arrays.copyOf(timeFirstSolved, problemNumber);
        languages = Arrays.copyOf(languages, languageNumber);
    }

    void recalcStandings() {
        WFTeamInfo[] standings = new WFTeamInfo[teamNumber];
        int n = 0;
        Arrays.fill(timeFirstSolved, Integer.MAX_VALUE);
        for (WFTeamInfo team : teamInfos) {
            if (team == null) continue;

            team.solved = 0;
            team.penalty = 0;
            team.lastAccepted = 0;
            for (int j = 0; j < problemNumber; j++) {
                ArrayBlockingQueue<RunInfo> runs = team.getRuns()[j];
                int wrong = 0;
                for (RunInfo run : runs) {
                    WFRunInfo wfrun = (WFRunInfo) run;
                    if ("AC".equals(run.getResult())) {
                        team.solved++;
                        int time = wfrun.getTeam() / 60 / 1000;
                        team.penalty += wrong * 20 + time;
                        team.lastAccepted = Math.max(team.lastAccepted, time);
                        timeFirstSolved[j] = Math.min(timeFirstSolved[j], wfrun.getTime());
                        break;
                    } else if (wfrun.getResult().length() > 0) {
                        wrong++;
                    }
                }
            }
            standings[n++] = team;
        }

        Arrays.sort(standings, 0, n, TeamInfo.comparator);

        for (int i = 0; i < n; i++) {
            if (i > 0 && TeamInfo.comparator.compare(standings[i], standings[i - 1]) == 0) {
                standings[i].rank = standings[i - 1].rank;
            } else {
                standings[i].rank = i + 1;
            }
        }
        this.standings = standings;
    }

    public void addTeam(WFTeamInfo team) {
        teamInfos[team.getId()] = team;
    }

    public void addRun(RunInfo run){
        runs.add(run);
        teamInfos[run.getTeam()].addRun(run, run.getProblemNumber() - 1);
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public int getProblemNumber() {
        return problemNumber;
    }

    @Override
    public TeamInfo getParticipant(String name) {
        for (int i = 0; i < teamNumber; i++) {
            if (teamInfos[i + 1].getName().equals(name) || teamInfos[i + 1].getShortName().equals(name)){
                return teamInfos[i + 1];
            }
        }
        return null;
    }

    @Override
    public TeamInfo getParticipant(int id) {
        return teamInfos[id];
    }

    public TeamInfo[] getStandings() {
        return standings;
    }

    @Override
    public long[] firstTimeSolved() {
        return timeFirstSolved;
    }
}
