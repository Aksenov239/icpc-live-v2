package ru.ifmo.acm.mainscreen.BreakingNews;

import ru.ifmo.acm.events.WF.WFRunInfo;

public class BreakingNews {
    public String outcome;
    public String problem;
    public int team;
    public long timestamp;
    public int runId;

    public BreakingNews(String outcome, String problem, int team, long timestamp, int runId) {
        this.outcome = outcome;
        this.problem = problem;
        this.team = team;
        this.timestamp = timestamp;
        this.runId = runId;
    }

    public void update(WFRunInfo run) {
        setOutcome(run.getResult());
        setTimestamp(run.getTime());
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setRunId(int runId) {
        this.runId = runId;
    }

    public String getOutcome() {
        return outcome;
    }

    public String getProblem() {
        return problem;
    }

    public int getTeam() {
        return team;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getRunId() {
        return runId;
    }

    public BreakingNews clone() {
        return new BreakingNews(outcome, problem, team, timestamp, runId);
    }
}
