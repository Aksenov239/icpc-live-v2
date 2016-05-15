package ru.ifmo.acm.events;

import ru.ifmo.acm.events.WF.WFEventsLoader;

import java.util.List;

public abstract class ContestInfo {
    public int teamNumber;
    public int problemNumber = 0;
    protected long startTime = 0;
    protected final long totalTime = 0;
    public List<ProblemInfo> problems;

    protected ContestInfo() {
    }

    protected ContestInfo(int problemNumber) {
        this.problemNumber = problemNumber;
    }

    public int getTeamsNumber() {
        return teamNumber;
    }

    public int getProblemsNumber() {
        return problemNumber;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getCurrentTime() {
        return startTime == 0 ? 0 :
                (long) Math.min(
                        ((System.currentTimeMillis() - startTime) * WFEventsLoader.SPEED),
                        WFEventsLoader.CONTEST_LENGTH
                );
    }

    public boolean isFrozen() {
        return getCurrentTime() >= 4 * 60 * 60;
    }

    public abstract TeamInfo getParticipant(String name);

    public abstract TeamInfo getParticipant(int id);

    public abstract TeamInfo getParticipantByHashTag(String hashTag);

    public abstract TeamInfo[] getStandings();

    public abstract long[] firstTimeSolved();

    public abstract RunInfo[] firstSolvedRun();

    public abstract RunInfo[] getRuns();

    public abstract RunInfo getRun(int id);
}
