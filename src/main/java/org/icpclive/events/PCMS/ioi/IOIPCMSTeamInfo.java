package org.icpclive.events.PCMS.ioi;

import org.icpclive.events.PCMS.PCMSTeamInfo;
import org.icpclive.events.RunInfo;
import org.icpclive.events.ScoreRunInfo;
import org.icpclive.events.ScoreTeamInfo;

import java.util.*;

public class IOIPCMSTeamInfo extends PCMSTeamInfo implements ScoreTeamInfo {
    public IOIPCMSTeamInfo(int problemsNumber) {
        super(problemsNumber);
    }

    public IOIPCMSTeamInfo(int id, String alias, String hallId, String name, String shortName, String hashTag,
                           HashSet<String> groups, int problemsNumber, int delay) {
        super(id, alias, hallId, name, shortName, hashTag, groups, problemsNumber, delay);
    }

    public IOIPCMSTeamInfo(PCMSTeamInfo pcmsTeamInfo) {
        super(pcmsTeamInfo);

        if (pcmsTeamInfo instanceof IOIPCMSTeamInfo) {
            score = ((IOIPCMSTeamInfo) pcmsTeamInfo).score;
        }
    }

    @Override
    public IOIPCMSTeamInfo copy() {
        return new IOIPCMSTeamInfo(this.id, this.alias, this.hallId, this.name, this.shortName, this.hashTag,
                this.groups, problemRuns.length, this.delay);
    }

    static Comparator<IOIPCMSTeamInfo> comparator = new Comparator<IOIPCMSTeamInfo>() {
        @Override
        public int compare(IOIPCMSTeamInfo o1, IOIPCMSTeamInfo o2) {
            if (o1.getScore() != o2.getScore()) {
                return -Long.compare(Math.round(o1.getScore()), Math.round(o2.getScore()));
            }
            return 0;//o1.getName().compareTo(o2.getName());
        }
    };

    public String getShortProblemState(int problem) {
        List<? extends RunInfo> runs = getRuns()[problem];
        synchronized (runs) {
            if (runs.isEmpty()) return "";
            String finalStatus = runs.get(runs.size() - 1).getResult();
            if (finalStatus.isEmpty()) {
                return "?";
            }
            return String.valueOf(Math.round(getProblemScore(problem)));
        }
    }

//    public String getScoreUpTo(IOIPCMSRunInfo run) {
//        List<? extends RunInfo> runs = getRuns()[run.getProblemId()];
//        synchronized (runs) {
//            if (runs.size() == 0) return "";
//            int maxBefore = 0;
//
//            for (RunInfo pRun: runs) {
//                if (pRun == run) {
//                    break;
//                }
//                IOIPCMSRunInfo ioiRun = (IOIPCMSRunInfo) pRun;
//                maxBefore = Math.max(maxBefore, ioiRun.getTotalScore());
//            }
//
//            int diff = Math.max(run.getTotalScore() - maxBefore, 0);
//            if (diff == 0) {
//                return "=";
//            } else {
//                return "+" + diff;
//            }
//        }
//    }

    public double getScore() {
        return score;
    }

    @Override
    public double getProblemScore(int problemId) {
        List<? extends RunInfo> runs = getRuns()[problemId];
        synchronized (runs) {
            double maxScore = 0.0;
            for (RunInfo e : runs) {
                maxScore = Math.max(maxScore, ((ScoreRunInfo) e).getTotalScore());
            }
            return maxScore;
        }
    }

    public int score;
}
