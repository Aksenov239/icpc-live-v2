package ru.ifmo.acm.mainscreen.statuses;

public class TeamStatus {
    public synchronized void setInfoVisible(boolean visible, String type, int teamId) {
        infoTimestamp = System.currentTimeMillis();
        isInfoVisible = visible;
        infoType = type;
        infoTeamNumber = teamId;
    }

    public String infoStatus() {
        return infoTimestamp + "\n" + isInfoVisible + "\n" + infoType + "\n" + infoTeamNumber;
    }

    private long infoTimestamp;
    private boolean isInfoVisible;
    private String infoType;
    private int infoTeamNumber;
    final private Object info = new Object();
}
