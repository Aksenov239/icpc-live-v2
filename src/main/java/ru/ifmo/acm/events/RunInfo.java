package ru.ifmo.acm.events;

public interface RunInfo {
    int getId();
    boolean isAccepted();
    boolean isJudged();
    String getResult();
    int getProblemNumber();
    long getTime();
    int getTeamId();
    SmallTeamInfo getTeamInfoBefore();
}
