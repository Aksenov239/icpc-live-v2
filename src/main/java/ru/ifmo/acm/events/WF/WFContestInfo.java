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
    ArrayBlockingQueue<RunInfo> runs;
    String[] languages = new String[4];
    WFTeamInfo[] teamInfos = new WFTeamInfo[200];
    public double[] timeFirstSolved = new double[20];
    int teamNumber;
    int problemNumber;

    WFTeamInfo[] standings;
    public long startTime;

    void recalcStandings() {
        standings = new WFTeamInfo[teamNumber];
        int n = 0;
        Arrays.fill(timeFirstSolved, 1e100);
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
                    if (run.getResult().equals("AC")) {
                        team.solved++;
                        int time = (int) wfrun.time / 60;
                        team.penalty += wrong * 20 + time;
                        team.lastAccepted = Math.max(team.lastAccepted, time);
                        timeFirstSolved[j] = Math.min(timeFirstSolved[j], wfrun.time);
                        break;
                    } else if (wfrun.result.length() > 0) {
                        wrong++;
                    }
                }
            }
            standings[n++] = team;
        }

        Comparator<WFTeamInfo> comparator = new Comparator<WFTeamInfo>() {
            @Override
            public int compare(WFTeamInfo o1, WFTeamInfo o2) {
                if (o1.solved != o2.solved) {
                    return -Integer.compare(o1.solved, o2.solved);
                }
                if (o1.penalty != o2.penalty) {
                    return Integer.compare(o1.penalty, o2.penalty);
                }
                return Integer.compare(o1.lastAccepted, o2.lastAccepted);
            }
        };
        Arrays.sort(standings, 0, n, comparator);

        for (int i = 0; i < n; i++) {
            if (i > 0 && comparator.compare(standings[i], standings[i - 1]) == 0) {
                standings[i].rank = standings[i - 1].rank;
            } else {
                standings[i].rank = i + 1;
            }
        }
    }

    public TeamInfo getTeamInfo(int teamId) {
        return teamInfos[teamId];
    }

    public int getPosition(int teamId) {
        if (standings == null) {
            return 1;
        }
        for (int i = 0; i < standings.length; i++) {
            if (standings[i].id == teamId) {
                return i;
            }
        }
        return -1;
    }

    public int getId(int position) {
        return standings == null ? teamInfos[position + 1].id : standings[position].id;
    }

    public int getIdByName(String name) {
        for (int i = 0; i < teamNumber; i++) {
            if (teamInfos[i + 1].name.equals(name) || teamInfos[i + 1].shortName.equals(name)) {
                return i + 1;
            }
        }
        return -1;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public int getProblemNumber() {
        return problemNumber;
    }

    @Override
    public TeamInfo getParticipant(String name) {
        return null;
    }

    @Override
    public TeamInfo getParticipant(int id) {
        return null;
    }

    public TeamInfo[] getStandings() {
        return standings;
    }

    @Override
    public long[] firstTimeSolved() {
        return new long[0];
    }
}
