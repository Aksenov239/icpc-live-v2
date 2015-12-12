package ru.ifmo.acm.events.PCMS;

import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PCMSContestInfo extends ContestInfo {
    @Override
    public TeamInfo[] getStandings() {
        return standings.stream().toArray(TeamInfo[]::new);
    }

    PCMSContestInfo(int problemNumber) {
        super(problemNumber);
        standings = new ArrayList<>();
        positions = new HashMap<>();
        timeFirstSolved = new long[problemNumber];
        //currentTime = 0;
    }

    public void fillTimeFirstSolved() {
        standings.forEach(teamInfo -> {
            ArrayList<RunInfo>[] runs = teamInfo.getRuns();
            for (int i = 0; i < runs.length; i++) {
                for (RunInfo run : runs[i]) {
                    if (run.isAccepted()) {
                        timeFirstSolved[i] = Math.min(timeFirstSolved[i], run.getTime());
                    }
                }
            }
        });
    }

    public void calculateRanks() {
        standings.get(0).rank = 1;
        for (int i = 1; i < standings.size(); i++) {
            if (TeamInfo.comparator.compare(standings.get(i), standings.get(i - 1)) == 0) {
                standings.get(i).rank = standings.get(i - 1).rank;
            } else {
                standings.get(i).rank = i + 1;
            }
        }
    }

    void addTeamStandings(PCMSTeamInfo teamInfo) {
        standings.add(teamInfo);
        positions.put(teamInfo.getAlias(), standings.size() - 1);
        teamNumber = standings.size();
    }

    PCMSTeamInfo getParticipant(Integer teamRank) {
        return teamRank == null ? new PCMSTeamInfo(problemNumber) : standings.get(teamRank);
    }

    public PCMSTeamInfo getParticipant(String name) {
        Integer teamRank = getParticipantRankByName(name);
        return getParticipant(teamRank);
    }

    public PCMSTeamInfo getParticipant(int id) {
        for (PCMSTeamInfo team: standings) {
            if (team.getId() == id) {
                return team;
            }
        }
        return null;
    }

    Integer getParticipantRankByName(String participantName) {
        return positions.get(participantName);
    }

    public long[] firstTimeSolved() {
        return timeFirstSolved;
    }

    public long getTotalTime() {
        return totalTime;
    }

    protected ArrayList<PCMSTeamInfo> standings;
    protected int totalRuns;
    protected int acceptedRuns;
    protected long[] timeFirstSolved;

    //private Map<String, Integer> positions;
    public Map<String, Integer> positions;
    public boolean frozen;
}