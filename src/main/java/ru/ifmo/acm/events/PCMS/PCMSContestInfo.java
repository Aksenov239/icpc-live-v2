package events.PCMS;

import events.ContestInfo;
import events.TeamInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PCMSContestInfo extends ContestInfo {
    @Override
    public TeamInfo[] getStandings() {
        return standings.stream().toArray(TeamInfo[]::new);
    }

    PCMSContestInfo(int teamNumber, int problemNumber) {
        super(teamNumber, problemNumber);
        standings = new ArrayList<>();
        ranks = new HashMap<>();
        currentTime = 0;
    }

    PCMSContestInfo() {
        super(100, 11); // TODO
        standings = new ArrayList<>();
        ranks = new HashMap<>();
        currentTime = 0;
    }

    void addTeamStandings(PCMSTeamInfo teamInfo) {
        standings.add(teamInfo);
        ranks.put(teamInfo.name, standings.size() - 1);
    }

    PCMSTeamInfo getParticipant(Integer teamRank) {
        return teamRank == null ? new PCMSTeamInfo(problemNumber) : standings.get(teamRank);
    }

    PCMSTeamInfo getParticipant(String name) {
        Integer teamRank = getParticipantRankByName(name);
        return getParticipant(teamRank);
    }

    Integer getParticipantRankByName(String participantName) {
        return ranks.get(participantName);
    }


    protected ArrayList<PCMSTeamInfo> standings;
    protected int totalRuns;
    protected int acceptedRuns;

    private Map<String, Integer> ranks;
}
