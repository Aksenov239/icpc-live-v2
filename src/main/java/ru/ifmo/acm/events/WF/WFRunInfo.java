package ru.ifmo.acm.events.WF;

import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.SmallTeamInfo;

/**
 * Created by aksenov on 16.04.2015.
 */
public class WFRunInfo implements RunInfo {
    public int id;
    public boolean judged;
    public String result = "";
    public String language;
    public int problem;
    public int team;
    public double time;
    public double timestamp;
    private int passed;
    private int total;
    private long lastUpdateTimestamp;
    private SmallTeamInfo teamInfoBefore;

    public WFRunInfo() {
    }

    public WFRunInfo(WFRunInfo another) {
        this.id = another.id;
        this.judged = another.judged;
        this.result = another.result;
        this.language = another.language;
        this.problem = another.problem;
        this.team = another.team;
        this.time = another.time;
        this.timestamp = another.timestamp;
        this.passed = another.getPassedTestsNumber();
        this.total = another.getTotalTestsNumber();
        this.lastUpdateTimestamp = another.getLastUpdateTimestamp();
    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public void setTeamInfoBefore(SmallTeamInfo teamInfoBefore) {
        this.teamInfoBefore = teamInfoBefore;
    }

    public void add(WFTestCaseInfo test) {
        if (total == 0) {
            total = test.total;
        }
        passed = test.id;
        lastUpdateTimestamp = System.currentTimeMillis();
    }

    public int getPassedTestsNumber() {
        return passed;
    }

    public int getTotalTestsNumber() {
        return total;
    }

    public int getId() {
        return id;
    }

    @Override
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
        return (long) (time * 1000);
    }

    @Override
    public int getTeam() {
        return team;
    }

    @Override
    public SmallTeamInfo getTeamInfoBefore() {
        return teamInfoBefore;
    }
}
