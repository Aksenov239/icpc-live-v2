package ru.ifmo.acm.events;

/**
 * Created by Aksenov239 on 19.03.2016.
 */
public class SmallTeamInfo {
    private int rank, solved, penalty;

    public SmallTeamInfo(int rank, int solved, int penalty) {
        this.rank = rank;
        this.solved = solved;
        this.penalty = penalty;
    }

    public int getRank() {
        return rank;
    }

    public int getSolve() {
        return solved;
    }

    public int getPenalty() {
        return penalty;
    }
}
