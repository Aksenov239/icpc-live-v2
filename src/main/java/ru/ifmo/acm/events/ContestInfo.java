package events;

public abstract class ContestInfo {
    protected final int teamNumber;
    protected final int problemNumber;
    protected long currentTime;

    protected ContestInfo(int teamNumber, int problemNumber) {
        this.teamNumber = teamNumber;
        this.problemNumber = problemNumber;
    }

    public int getTeamsNumber() {
        return teamNumber;
    }

    public int getProblemsNumber() {
        return problemNumber;
    }

    public void setCurrentTime(long time) {
        this.currentTime = time;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public abstract TeamInfo[] getStandings();
}
