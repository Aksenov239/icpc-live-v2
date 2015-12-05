package ru.ifmo.acm.events;

public abstract class ContestInfo {
    protected int teamNumber;
    protected final int problemNumber;
    protected long startTime = 0;
    protected final long totalTime = 0;

    protected ContestInfo(int problemNumber) {
        this.problemNumber = problemNumber;
    }

    public int getTeamsNumber() {
        return teamNumber;
    }

    public int getProblemsNumber() {
        return problemNumber;
    }

    public long getStartTime(){
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getCurrentTime() {
        return startTime == 0 ? 0 : Math.min(System.currentTimeMillis() - startTime, 5 * 60 * 60 * 100);
    }

    public abstract TeamInfo getParticipant(String name);

    public abstract TeamInfo getParticipant(int id);

    public abstract TeamInfo[] getStandings();

    public abstract long[] firstTimeSolved();
}
