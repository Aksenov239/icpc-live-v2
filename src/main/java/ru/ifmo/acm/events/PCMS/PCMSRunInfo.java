package events.PCMS;

import events.RunInfo;

public class PCMSRunInfo implements RunInfo {
    PCMSRunInfo() {
        this.judged = true;
    }

    PCMSRunInfo(String result, int problem, int team, long time, boolean firstToSolve) {
        this();
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
