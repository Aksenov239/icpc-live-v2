package org.icpclive.events.codeforces.marathon;

import org.icpclive.events.codeforces.CFContestInfo;
import org.icpclive.events.codeforces.CFRunInfo;
import org.icpclive.events.codeforces.CFTeamInfo;
import org.icpclive.events.codeforces.api.data.*;
import org.icpclive.events.codeforces.api.results.CFStandings;

import java.util.List;

public class MarathonCFContestInfo extends CFContestInfo {
    double[] maxScores;

    public MarathonCFContestInfo() {
        super();
        FREEZE_TIME = CONTEST_LENGTH;
    }

    protected CFTeamInfo createTeamInfo(CFRanklistRow row) {
        return new MarathonCFTeamInfo(row);
    }

    protected CFRunInfo createRunInfo(CFSubmission submission) {
        return new MarathonCFRunInfo(submission);
    }

    @Override
    public void update(CFStandings standings, List<CFSubmission> submissions) {
        super.update(standings, submissions);
        maxScores = new double[problems.size()];
        for (int i = 0; i < problems.size(); i++) {
            for (CFTeamInfo team : participantsById.values()) {
                maxScores[i] = Math.max(maxScores[i], ((MarathonCFTeamInfo) team).getProblemScore(i));
            }
        }
    }

    public double getMaxProblemScore(int problemId) {
        return maxScores[problemId];
    }
}
