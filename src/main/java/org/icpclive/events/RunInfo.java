package org.icpclive.events;

public interface RunInfo extends Comparable<RunInfo> {
    int getId();
    boolean isAccepted();
    boolean isJudged();
    String getResult();
    int getProblemNumber();
    long getTime();
    int getTeamId();
    SmallTeamInfo getTeamInfoBefore();
    boolean isReallyUnknown();
    double getPercentage();
    long getLastUpdateTimestamp();
    double getTimestamp();

    default public int compareTo(RunInfo runInfo) {
        return Long.compare(getTime(), runInfo.getTime());
    }
}
