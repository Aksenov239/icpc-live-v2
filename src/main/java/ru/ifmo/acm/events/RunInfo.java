package ru.ifmo.acm.events;

public interface RunInfo {
    boolean isJudged();
    String getResult();
    int getProblemNumber();
    long getTime();
}