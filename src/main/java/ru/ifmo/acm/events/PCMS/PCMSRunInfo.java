package ru.ifmo.acm.events.PCMS;

import ru.ifmo.acm.events.RunInfo;

public class PCMSRunInfo implements RunInfo {
    PCMSRunInfo() {
        this.judged = true;
    }

    PCMSRunInfo(boolean judged, String result, int problem, long time) {
        this(judged, result, problem, time, false);
    }

    PCMSRunInfo(String result, int problem, long time, boolean firstToSolve) {
        this(true, result, problem, time, firstToSolve);
    }

    PCMSRunInfo(boolean judged, String result, int problem, long time, boolean firstToSolve) {
        this.judged = judged;
        this.result = result;
        this.problem = problem;
        this.time = time;
        this.firstToSolve = firstToSolve;
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

    protected boolean judged;
    protected String result = "";
    protected int problem;
    protected long time;

    protected boolean firstToSolve;
}
