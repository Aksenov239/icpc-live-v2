package org.icpclive.events.WF.old;

import org.icpclive.events.RunInfo;
import org.icpclive.events.SmallTeamInfo;
import org.icpclive.events.TeamInfo;

/**
 * Created by aksenov on 16.04.2015.
 */
public class WFRunInfo implements RunInfo {
    public int id;
    public boolean judged;
    public String result = "";
    public String language;
    public int problem;
    public int teamId;
    public long time;
    private long lastUpdateTime;
    private int passed;
    private int total;
    private SmallTeamInfo teamInfoBefore;
    public TeamInfo team;
    public boolean reallyUnknown;

    public WFRunInfo() {
//        lastUpdateTime = System.currentTimeMillis();
    }

    public WFRunInfo(WFRunInfo another) {
        this.id = another.id;
        this.judged = another.judged;
        this.result = another.result;
        this.language = another.language;
        this.problem = another.problem;
        this.teamId = another.teamId;
        this.time = another.time;
        this.passed = another.getPassedTestsNumber();
        this.total = another.getTotalTestsNumber();
        this.lastUpdateTime = another.getLastUpdateTime();
    }

    @Override
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public void setTeamInfoBefore(SmallTeamInfo teamInfoBefore) {
        this.teamInfoBefore = teamInfoBefore;
    }

    public void add(WFTestCaseInfo test) {
        if (total == 0) {
            total = test.total;
        }
        passed = test.id;
        lastUpdateTime = Math.max(lastUpdateTime, test.time);
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
        return time;
    }

    public int getTeamId() {
        return teamId;
    }

    @Override
    public SmallTeamInfo getTeamInfoBefore() {
        return teamInfoBefore;
    }

    public boolean isReallyUnknown() {
        return reallyUnknown;
    }

    public double getPercentage() {
        return 1.0 * this.getPassedTestsNumber() / this.getTotalTestsNumber();
    }

    @Override
    public String toString() {
        String teamName = "" + teamId;
        if (team != null) teamName = team.getShortName();
        return teamName + " " + (char) ('A' + problem) + " " + result;
    }
}
