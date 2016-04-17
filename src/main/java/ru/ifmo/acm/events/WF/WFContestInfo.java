package ru.ifmo.acm.events.WF;

import ru.ifmo.acm.datapassing.StandingsData;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;

import java.util.Arrays;
import java.util.List;

/**
 * Created by aksenov on 05.05.2015.
 */
public class WFContestInfo extends ContestInfo {
    private WFRunInfo[] runs;
    String[] languages;
    private WFTeamInfo[] teamInfos;
    public long[] timeFirstSolved;
    private int maxRunId;
    WFRunInfo[] firstSolvedRun;

    private WFTeamInfo[] standings = null;

    public WFContestInfo(int problemsNumber, int teamsNumber) {
        problemNumber = problemsNumber;
        teamNumber = teamsNumber;
        teamInfos = new WFTeamInfo[teamsNumber];
        timeFirstSolved = new long[problemsNumber];
        languages = new String[4];
        runs = new WFRunInfo[1000000];
        firstSolvedRun = new WFRunInfo[problemsNumber];
    }

    void recalcStandings() {
        WFTeamInfo[] standings = new WFTeamInfo[teamNumber];
        int n = 0;
        Arrays.fill(timeFirstSolved, Integer.MAX_VALUE);
        Arrays.fill(firstSolvedRun, null);
        for (WFTeamInfo team : teamInfos) {
            if (team == null)
                continue;

            team.solved = 0;
            team.penalty = 0;
            team.lastAccepted = 0;
            for (int j = 0; j < problemNumber; j++) {
                List<RunInfo> runs = team.getRuns()[j];
                int wrong = 0;
                for (RunInfo run : runs) {
                    WFRunInfo wfrun = (WFRunInfo) run;
                    if ("AC".equals(run.getResult())) {
                        team.solved++;
                        int time = (int) (wfrun.getTime() / 60 / 1000);
                        team.penalty += wrong * 20 + time;
                        team.lastAccepted = Math.max(team.lastAccepted, wfrun.getTime());
                        if (wfrun.getTime() < timeFirstSolved[j]) {
                            timeFirstSolved[j] = wfrun.getTime();
                            firstSolvedRun[j] = wfrun;
                        }
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

    void recalcStandings(WFTeamInfo[] standings) {
        for (WFTeamInfo team : standings) {
            team.solved = 0;
            team.penalty = 0;
            team.lastAccepted = 0;
            for (int j = 0; j < problemNumber; j++) {
                List<RunInfo> runs = team.getRuns()[j];
                int wrong = 0;
                for (RunInfo run : runs) {
                    WFRunInfo wfrun = (WFRunInfo) run;
                    if ("AC".equals(run.getResult())) {
                        team.solved++;
                        int time = (int) (wfrun.getTime() / 60 / 1000);
                        team.penalty += wrong * 20 + time;
                        team.lastAccepted = Math.max(team.lastAccepted, time);
                        break;
                    } else if (wfrun.getResult().length() > 0) {
                        wrong++;
                    }
                }
            }
        }

        Arrays.sort(standings, 0, standings.length, TeamInfo.comparator);

        for (int i = 0; i < standings.length; i++) {
            if (i > 0 && TeamInfo.comparator.compare(standings[i], standings[i - 1]) == 0) {
                standings[i].rank = standings[i - 1].rank;
            } else {
                standings[i].rank = i + 1;
            }
        }
    }


    public void addTeam(WFTeamInfo team) {
        teamInfos[team.getId()] = team;
    }

    public boolean runExists(int id) {
        return runs[id] != null;
    }

    public WFRunInfo getRun(int id) {
        return runs[id];
    }

    public void addRun(WFRunInfo run) {
//		System.err.println("add run: " + run);
        if (!runExists(run.getId())) {
            maxRunId = Math.max(maxRunId, run.getId());
            runs[run.getId()] = run;
            teamInfos[run.getTeamId()].addRun(run, run.getProblemNumber());
        }
    }

    public int getMaxRunId() {
        return maxRunId;
    }

    public void addTest(WFTestCaseInfo test) {
//		System.out.println("Adding test " + test.id + " to run " + test.run);
        if (runExists(test.run)) {
            runs[test.run].add(test);
//			System.out.println("Run " + runs[test.run] + " passed " + runs[test.run].getPassedTestsNumber() + " tests");
        }
    }

    @Override
    public TeamInfo getParticipant(String name) {
        for (int i = 0; i < teamNumber; i++) {
            if (teamInfos[i].getName().equals(name) || teamInfos[i].getShortName().equals(name)) {
                return teamInfos[i];
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

    @Override
    public RunInfo[] firstSolvedRun() {
        return firstSolvedRun;
    }

    @Override
    public RunInfo[] getRuns() {
        return runs;
    }

    public WFTeamInfo getParticipantByHashTag(String hashTag) {
        for (int i = 0; i < teamNumber; i++) {
            if (hashTag != null && hashTag.equals(teamInfos[i].getHashTag())) {
                return teamInfos[i];
            }
        }
        return null;
    }

    public TeamInfo[] getPossibleStandings(boolean isOptimistic) {
        WFTeamInfo[] possibleStandings = new WFTeamInfo[teamNumber];
        int teamIndex = 0;
        for (WFTeamInfo team : standings) {
            possibleStandings[teamIndex] = team.copy();
            for (int j = 0; j < problemNumber; j++) {
                List<RunInfo> runs = team.getRuns()[j];
                int runIndex = 0;
                for (RunInfo run : runs) {
                    WFRunInfo clonedRun = new WFRunInfo((WFRunInfo) run);

                    if (clonedRun.getResult().length() == 0) {
                        clonedRun.judged = true;
                        String expectedResult = isOptimistic ? "AC" : "WA";
                        clonedRun.result = (runIndex == runs.size() - 1) ? expectedResult : "WA";
                        clonedRun.reallyUnknown = true;
                    }
                    possibleStandings[teamIndex].addRun(clonedRun, j);
                    runIndex++;
                }
            }
            teamIndex++;
        }

        recalcStandings(possibleStandings);

        return possibleStandings;
    }

    public TeamInfo[] getStandings(StandingsData.OptimismLevel level) {
        switch (level) {
            case NORMAL:
                return getStandings();
            case OPTIMISTIC:
                return getPossibleStandings(true);
            case PESSIMISTIC:
                return getPossibleStandings(false);
        }

        return null;
    }
}
