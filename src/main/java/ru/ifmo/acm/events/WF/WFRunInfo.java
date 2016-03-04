package ru.ifmo.acm.events.WF;

import ru.ifmo.acm.events.RunInfo;

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
        return (long) Math.floor(timestamp);
    }

    @Override
    public int getTeam() {
        return team;
    }
}
