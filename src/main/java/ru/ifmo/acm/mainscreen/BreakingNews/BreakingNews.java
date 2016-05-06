package ru.ifmo.acm.mainscreen.BreakingNews;

public class BreakingNews {
    public String outcome;
    public String problem;
    public int team;
    public long timestamp;


    public BreakingNews(String outcome, String problem, int team, long timestamp) {
        this.outcome = outcome;
        this.problem = problem;
        this.team = team;
        this.timestamp = timestamp;
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

    public BreakingNews clone() {
        return new BreakingNews(outcome, problem, team, timestamp);
    }
}
