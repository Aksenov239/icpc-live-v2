package ru.ifmo.acm.events;

public abstract class ContestInfo {
    protected int teamNumber;
    protected final int problemNumber;
    protected long currentTime;

    protected ContestInfo(int problemNumber) {
        this.problemNumber = problemNumber;
    }

    public int getTeamsNumber() {
        return teamNumber;
    }

    public int getProblemsNumber() {
        return problemNumber;
    }

    public void setCurrentTime(long time) {
        this.currentTime = time;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public abstract TeamInfo[] getStandings();
}
