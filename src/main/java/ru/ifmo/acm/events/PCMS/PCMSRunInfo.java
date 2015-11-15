package ru.ifmo.acm.events.PCMS;

import ru.ifmo.acm.events.RunInfo;

public class PCMSRunInfo implements RunInfo {
    PCMSRunInfo() {
        this.judged = true;
    }

    PCMSRunInfo(String result, int problem, long time, boolean firstToSolve) {
        this.judged = true;
        this.result = result;
        this.problem = problem;
        this.time = time;
        this.firstToSolve = firstToSolve;
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

    protected boolean judged;
    protected String result = "";
    protected int problem;
    protected long time;

    protected boolean firstToSolve;
}
