package org.icpclive.events.codeforces.marathon;

import org.icpclive.events.RunInfo;
import org.icpclive.events.ScoreRunInfo;
import org.icpclive.events.ScoreTeamInfo;
import org.icpclive.events.codeforces.CFTeamInfo;
import org.icpclive.events.codeforces.api.data.CFRanklistRow;

import java.util.List;

public class MarathonCFTeamInfo extends CFTeamInfo implements ScoreTeamInfo {
    public MarathonCFTeamInfo(CFRanklistRow row) {
        super(row);
    }

    public double getScore() {
        return row.points;
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

    @Override
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
}
