package org.icpclive.events.PCMS.ioi;

import org.icpclive.events.*;
import org.icpclive.events.PCMS.PCMSRunInfo;

public class IOIPCMSRunInfo extends PCMSRunInfo implements ScoreRunInfo {
    public IOIPCMSRunInfo() {
        this.judged = true;
    }

    public IOIPCMSRunInfo(boolean judged, String result, int problem, long time, long timestamp, int teamId, int score) {
        super(judged, result, problem, time, timestamp, teamId);
        this.score = score;
    }

    public IOIPCMSRunInfo(RunInfo run) {
        super(run);
        if (run instanceof IOIPCMSRunInfo) {
            score = ((IOIPCMSRunInfo) run).score;
            totalScore = ((IOIPCMSRunInfo) run).totalScore;
        }
        this.judged = run.isJudged();
        this.result = run.getResult();
        this.problem = run.getProblemId();
        this.time = run.getTime();
        this.lastUpdateTimestamp = run.getLastUpdateTime();
        this.teamId = run.getTeamId();
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    protected double score;
    protected double totalScore;
}
