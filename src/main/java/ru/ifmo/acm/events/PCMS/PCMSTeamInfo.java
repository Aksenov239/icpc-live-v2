package ru.ifmo.acm.events.PCMS;

import ru.ifmo.acm.events.TeamInfo;

import java.util.ArrayList;
import java.util.Arrays;

public class PCMSTeamInfo implements TeamInfo {
    public PCMSTeamInfo(int problemsNumber) {
        problemRuns = new ArrayList[problemsNumber];
        Arrays.setAll(problemRuns, i -> new ArrayList<>());
        this.rank = 1;
    }

    public PCMSTeamInfo(String id, String name, String shortName, int problemsNumber) {
        this(problemsNumber);
        this.id = id;
        this.name = name;
        this.shortName = shortName;
    }

    public PCMSTeamInfo(String name, int problemsNumber) {
        this(null, name, null, problemsNumber);
    }

    public void addRun(PCMSRunInfo run, int problemId) {
        if (run != null) {
            problemRuns[problemId].add(run);
        }
    }

    public int getRunsNumber(int problemId) {
        return problemRuns[problemId].size();
    }

    public long getLastSubmitTime(int problemId) {
        int runsNumber = getRunsNumber(problemId);
        return runsNumber == 0 ? -1 : problemRuns[problemId].get(runsNumber).time;
    }

    @Override
    public int getRank() {
        return rank;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPenalty() {
        return penalty;
    }

    @Override
    public int getSolvedProblemsNumber() {
        return solved;
    }

    public String id;

    public String name;
    public String shortName;

    public int rank;
    public int solved;
    public int penalty;


    protected ArrayList<PCMSRunInfo>[] problemRuns;
}
