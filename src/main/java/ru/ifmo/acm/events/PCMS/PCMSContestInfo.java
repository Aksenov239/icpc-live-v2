package ru.ifmo.acm.events.PCMS;

import ru.ifmo.acm.events.ContestInfo;
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
        currentTime = 0;
    }

    void addTeamStandings(PCMSTeamInfo teamInfo) {
        standings.add(teamInfo);
        positions.put(teamInfo.name, standings.size() - 1);
    }

    PCMSTeamInfo getParticipant(Integer teamRank) {
        return teamRank == null ? new PCMSTeamInfo(problemNumber) : standings.get(teamRank);
    }

    public PCMSTeamInfo getParticipant(String name) {
        Integer teamRank = getParticipantRankByName(name);
        return getParticipant(teamRank);
    }

    Integer getParticipantRankByName(String participantName) {
        return positions.get(participantName);
    }


    protected ArrayList<PCMSTeamInfo> standings;
    protected int totalRuns;
    protected int acceptedRuns;

    private Map<String, Integer> positions;
}
