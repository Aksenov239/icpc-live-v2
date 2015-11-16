package events.PCMS;

import events.TeamInfo;

import java.util.ArrayList;
import java.util.Arrays;

public class PCMSTeamInfo implements TeamInfo {
    public PCMSTeamInfo(int problemsNumber) {
        problemRuns = new ArrayList[problemsNumber];
        Arrays.setAll(problemRuns, i -> new ArrayList<PCMSRunInfo>());
        this.rank = 1;
    }

    public PCMSTeamInfo(String name, int problemsNumber) {
        this(problemsNumber);
        this.name = name;
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

    public void setRuns(ArrayList<PCMSRunInfo>[] runs) {
        problemRuns = runs;
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

    public int rank;
    public String name;
    public int solved;
    public int penalty;

    protected ArrayList<PCMSRunInfo>[] problemRuns;
}
