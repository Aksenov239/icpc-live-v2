package ru.ifmo.acm.events;

import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import ru.ifmo.acm.datapassing.StandingsData;

public abstract class ContestInfo {
    public int teamNumber;
    public int problemNumber = 0;
    protected long startTime = 0;
    protected final long totalTime = 0;
    public static List<ProblemInfo> problems;
    public long lastTime;
    private static String[] hashtags;

    public enum Status {
        BEFORE,
        RUNNING,
        PAUSED,
        OVER
    }

    public Status status = Status.BEFORE;

    public static int CONTEST_LENGTH = 5 * 60 * 60 * 1000;
    public static int FREEZE_TIME = 4 * 60 * 60 * 1000;
    public static final TreeSet<String> REGIONS = new TreeSet<>();

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

    public void setStatus(Status status) {
        lastTime = getCurrentTime();
        this.status = status;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getCurrentTime() {
        switch (status) {
            case BEFORE:
                return 0;
            case PAUSED:
                return lastTime;
            case RUNNING:
                return startTime == 0 ? 0 :
                    (long) Math.min(
                            ((System.currentTimeMillis() - startTime) * EventsLoader.EMULATION_SPEED),
                            ContestInfo.CONTEST_LENGTH
                    );
            case OVER:
                return ContestInfo.CONTEST_LENGTH;
            default:
                return 0;
        }
    }

    public boolean isFrozen() {
        return getCurrentTime() >= ContestInfo.FREEZE_TIME;
    }

    public abstract TeamInfo getParticipant(String name);

    public abstract TeamInfo getParticipant(int id);

    public abstract TeamInfo getParticipantByHashTag(String hashTag);

    public abstract TeamInfo[] getStandings();

    public abstract TeamInfo[] getStandings(StandingsData.OptimismLevel optimismLevel);

    public TeamInfo[] getStandings(String region, StandingsData.OptimismLevel optimismLevel) {
        if (StandingsData.ALL_REGIONS.equals(region)) {
            return getStandings(optimismLevel);
        }
        TeamInfo[] infos = getStandings(optimismLevel);
//        System.err.println(infos.length);
//        for (TeamInfo team : infos) {
//            System.err.println(team.getId() + " " + team.getRegion() + " " + region);
//        }
        return Stream.of(infos).filter(x -> region.equals(x.getRegion())).toArray(TeamInfo[]::new);
    }

    public String[] getHashTags() {
        if (hashtags != null) {
            return hashtags;
        }
        ArrayList<String> hashtags = new ArrayList<>();
        TeamInfo[] infos = getStandings();
        for (TeamInfo teamInfo : infos) {
            hashtags.add(teamInfo.getHashTag());
        }
        return hashtags.toArray(new String[0]);
    }

    public abstract long[] firstTimeSolved();

    public abstract RunInfo[] firstSolvedRun();

    public abstract RunInfo[] getRuns();

    public abstract RunInfo getRun(int id);

    public abstract BlockingQueue<AnalystMessage> getAnalystMessages();

    public abstract int getLastRunId();
}
