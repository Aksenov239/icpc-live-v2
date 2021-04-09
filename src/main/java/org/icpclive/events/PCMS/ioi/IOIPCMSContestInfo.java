package org.icpclive.events.PCMS.ioi;

import org.icpclive.datapassing.StandingsData;
import org.icpclive.events.AnalystMessage;
import org.icpclive.events.ContestInfo;
import org.icpclive.events.PCMS.PCMSContestInfo;
import org.icpclive.events.RunInfo;
import org.icpclive.events.TeamInfo;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class IOIPCMSContestInfo extends PCMSContestInfo {
    private BlockingQueue<AnalystMessage> messageQueue = new ArrayBlockingQueue<AnalystMessage>(1);

    @Override
    public TeamInfo[] getStandings(StandingsData.OptimismLevel optimismLevel) {
        return getStandings();
    }

    IOIPCMSContestInfo(int problemNumber) {
        super(problemNumber);
    }

    public void calculateRanks() {
        standings.get(0).rank = 1;
        for (int i = 1; i < standings.size(); i++) {
            if (IOIPCMSTeamInfo.comparator.compare((IOIPCMSTeamInfo) standings.get(i), (IOIPCMSTeamInfo) standings.get(i - 1)) == 0) {
                standings.get(i).rank = standings.get(i - 1).rank;
            } else {
                standings.get(i).rank = i + 1;
            }
        }
    }
}