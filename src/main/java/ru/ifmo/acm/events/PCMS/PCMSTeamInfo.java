package ru.ifmo.acm.events.PCMS;

import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;

import java.util.ArrayList;
import java.util.Arrays;

public class PCMSTeamInfo implements TeamInfo {
    String alias;

    public PCMSTeamInfo(int problemsNumber) {
        problemRuns = new ArrayList[problemsNumber];
        Arrays.setAll(problemRuns, i -> new ArrayList<>());
        this.rank = 1;
    }

    public PCMSTeamInfo(int id, String alias, String name, String shortName, String hashTag,
                        String region, int problemsNumber) {
        this(problemsNumber);

        this.id = id;
        this.alias = alias;
        this.name = name;
        this.shortName = shortName;
        this.region = region;
    }

    public PCMSTeamInfo(String name, int problemsNumber) {
        this(-1, "", name, null, null, null, problemsNumber);
    }

    public PCMSTeamInfo(PCMSTeamInfo pcmsTeamInfo) {
        this(pcmsTeamInfo.id, pcmsTeamInfo.alias, pcmsTeamInfo.name,
                pcmsTeamInfo.shortName, pcmsTeamInfo.hashTag, pcmsTeamInfo.region, pcmsTeamInfo.problemRuns.length);

        for (int i = 0; i < pcmsTeamInfo.problemRuns.length; i++) {
            problemRuns[i].addAll(pcmsTeamInfo.problemRuns[i]);
        }
    }

    @Override
    public void addRun(RunInfo run, int problemId) {
        if (run != null) {
            problemRuns[problemId].add(run);
        }
    }

    public int mergeRuns(ArrayList<PCMSRunInfo> runs, int problemId, int lastRunId) {
        for (int i = 0; i < runs.size(); i++) {
            if (i >= problemRuns[problemId].size()) {
                runs.get(i).id = lastRunId++;
                problemRuns[problemId].add(runs.get(i));
            }
        }
        return lastRunId;
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
    public String getRegion() {
        return region;
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
        return lastAccepted;
    }

    public String getHashTag() {
        return hashTag;
    }

    @Override
    public PCMSTeamInfo copy() {
        return new PCMSTeamInfo(this.id, this.alias, this.name, this.shortName, this.hashTag,
                this.region, problemRuns.length);
    }

    public String toString() {
        return shortName;
    }

    public int id;

    public String name;
    public String shortName;
    public String region;
    public String hashTag;

    public int rank;
    public int solved;
    public int penalty;
    public long lastAccepted;

    protected ArrayList<RunInfo>[] problemRuns;
}
