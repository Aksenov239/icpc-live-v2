package ru.ifmo.acm.events.PCMS;

import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;

public class PCMSTeamInfo implements TeamInfo {
    String alias;

    public PCMSTeamInfo(int problemsNumber) {
        problemRuns = new ArrayList[problemsNumber];
        Arrays.setAll(problemRuns, i -> new ArrayList<>());
        this.rank = 1;
    }

    public PCMSTeamInfo(int id, String alias, String name, String shortName, int problemsNumber) {
        this(problemsNumber);
        this.id = id;
        this.alias = alias;
        this.name = name;
        this.shortName = shortName;
    }

    public PCMSTeamInfo(String name, int problemsNumber) {
        this(-1, "", name, null, problemsNumber);
    }

    public PCMSTeamInfo(PCMSTeamInfo pcmsTeamInfo) {
        this(pcmsTeamInfo.id, pcmsTeamInfo.alias, pcmsTeamInfo.name, pcmsTeamInfo.shortName, pcmsTeamInfo.problemRuns.length);
        for (int i = 0; i < pcmsTeamInfo.problemRuns.length; i++) {
            problemRuns[i].addAll(pcmsTeamInfo.problemRuns[i]);
        }
    }

    public void addRun(PCMSRunInfo run, int problemId) {
        if (run != null) {
            problemRuns[problemId].add(run);
        }
    }

    public void addRuns(ArrayList<PCMSRunInfo> runs, int problemId) {
        problemRuns[problemId].addAll(runs);
    }

    public int getRunsNumber(int problemId) {
        return problemRuns[problemId].size();
    }

    public long getLastSubmitTime(int problemId) {
        int runsNumber = getRunsNumber(problemId);
        return runsNumber == 0 ? -1 : problemRuns[problemId].get(runsNumber).getTime();
    }

    public int getId() {
        return id;
    }

    @Override
    public int getRank() {
        return rank;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public int getPenalty() {
        return penalty;
    }

    @Override
    public int getSolvedProblemsNumber() {
        return solved;
    }

    public ArrayList<RunInfo>[] getRuns() {
        return problemRuns;
    }

    public long getLastAccepted() {
        return 0;
    }

    public int id;

    public String name;
    public String shortName;

    public int rank;
    public int solved;
    public int penalty;

    protected ArrayList<RunInfo>[] problemRuns;
}
