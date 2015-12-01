package ru.ifmo.acm.events;

import java.util.List;

public interface TeamInfo {
    int getId();

    int getRank();

    String getName();

    String getShortName();

    int getPenalty();

    int getSolvedProblemsNumber();

    List<RunInfo>[] getRuns();

}
