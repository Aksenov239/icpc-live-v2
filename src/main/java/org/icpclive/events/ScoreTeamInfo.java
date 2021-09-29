package org.icpclive.events;

public interface ScoreTeamInfo extends TeamInfo {
    double getScore();
    double getProblemScore(int problemId);
}
