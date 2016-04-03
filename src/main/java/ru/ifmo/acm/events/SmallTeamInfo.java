package ru.ifmo.acm.events;

import java.util.List;

/**
 * Created by Aksenov239 on 19.03.2016.
 */
public class SmallTeamInfo implements TeamInfo {
    private int rank, solved, penalty;
    private String shortName;

    public SmallTeamInfo(TeamInfo team) {
        rank = team.getRank();
        solved = team.getSolvedProblemsNumber();
        penalty = team.getPenalty();
        shortName = team.getShortName();
    }

    public int getId() {
        return -1;
    }

    public int getRank() {
        return rank;
    }

    public String getName() {
        return null;
    }

    public String getShortName() {
        return shortName;
    }

    public int getSolvedProblemsNumber() {
        return solved;
    }

    public int getPenalty() {
        return penalty;
    }

    public long getLastAccepted() {
        return 0;
    }

    public List<RunInfo>[] getRuns() {
        return null;
    }

    public String getHashTag() { return null; }
}
