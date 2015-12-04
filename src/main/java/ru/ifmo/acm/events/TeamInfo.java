package ru.ifmo.acm.events;

import java.util.Comparator;
import java.util.List;

public interface TeamInfo {
    int getId();

    int getRank();

    String getName();

    String getShortName();

    int getPenalty();

    int getSolvedProblemsNumber();

    List<RunInfo>[] getRuns();

    Comparator<TeamInfo> comparator = new Comparator<TeamInfo>() {
        @Override
        public int compare(TeamInfo o1, TeamInfo o2) {
            if (o1.getSolvedProblemsNumber() != o2.getSolvedProblemsNumber()) {
                return -Integer.compare(o1.getSolvedProblemsNumber(), o2.getSolvedProblemsNumber());
            }
            if (o1.getPenalty() != o2.getPenalty()) {
                return Integer.compare(o1.getPenalty(), o2.getPenalty());
            }
            return 0;
        }
    };

}
