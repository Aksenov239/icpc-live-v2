package ru.ifmo.acm.events.WF;

import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by aksenov on 17.04.2015.
 */
public class WFTeamInfo implements TeamInfo {

    private ArrayBlockingQueue<RunInfo>[] problem_runs;

    public int id = -1;
    public int rank;
    public String name;

    public int solved;
    public int penalty;
    public int lastAccepted;
    public String region;

    public String shortName;

    public WFTeamInfo(int problems) {
        problem_runs = new ArrayBlockingQueue[problems];
        for (int i = 0; i < problems; i++) {
            problem_runs[i] = new ArrayBlockingQueue<>(100);
        }
    }

    @Override
    public int getId() {
        return id;
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
    public String getShortName() {
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

    public ArrayBlockingQueue<RunInfo>[] getRuns() {
        return problem_runs;
    }

    public int getLastAccepted() {
        return lastAccepted;
    }

    public ArrayBlockingQueue<RunInfo> getRunsByProblem(int problemId) {
        return problem_runs[problemId];
    }

    public void addRun(RunInfo run, int problemId){
        problem_runs[problemId].add(run);
    }

}
