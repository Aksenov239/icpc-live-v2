package ru.ifmo.acm.events.PCMS;

import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.SmallTeamInfo;

public class PCMSRunInfo implements RunInfo {
    PCMSRunInfo() {
        this.judged = true;
    }

    PCMSRunInfo(boolean judged, String result, int problem, long time, int teamId) {
        this.judged = judged;
        this.result = result;
        this.problem = problem;
        this.time = time;
        this.timestamp = 1. * System.currentTimeMillis() / 1000;
        this.lastUpdateTimestamp = System.currentTimeMillis();
        this.teamId = teamId;
    }

    PCMSRunInfo(RunInfo run) {
        this.judged = run.isJudged();
        this.result = run.getResult();
        this.problem = run.getProblemNumber();
        this.time = run.getTime();
    }

    public int getId() {
        return id;
    }

    public boolean isAccepted() {
        return "AC".equals(result);
    }

    @Override
    public boolean isJudged() {
        return judged;
    }

    @Override
    public String getResult() {
        return result;
    }

    @Override
    public int getProblemNumber() {
        return problem;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    @Override
    public double getTimestamp() {
        return timestamp;
    }

    public int getTeamId() {
        return teamId;
    }

    public boolean isReallyUnknown() {
        return reallyUnknown;
    }

    public SmallTeamInfo getTeamInfoBefore() {
        return null;
    }

    public double getPercentage() {
        return 0;
    }

    protected boolean judged;
    protected String result = "";
    protected int id;
    protected int teamId;
    protected int problem;
    protected long time;
    protected double timestamp;
    protected long lastUpdateTimestamp;
    public boolean reallyUnknown;
}
