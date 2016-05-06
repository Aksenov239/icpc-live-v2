package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.events.TeamInfo;
import ru.ifmo.acm.mainscreen.MainScreenData;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class BreakingNewsData implements CachedData {
    public BreakingNewsData initialize() {
        BreakingNewsData data = MainScreenData.getMainScreenData().breakingNewsData;
        this.timestamp = data.timestamp;
        this.isVisible = data.isVisible;
        this.isLive = data.isLive;
        this.teamId = data.teamId;
        this.problemId = data.problemId;
        this.runId = data.runId;
        this.infoType = data.infoType;
        return this;
    }

    public void recache() {
        Data.cache.refresh(BreakingNewsData.class);
    }

    public synchronized boolean setNewsVisible(boolean visible, String type, boolean isLive, String newsMessage, int teamId, int problemId, int runId) {
        if (visible && isVisible) {
            return false;
        }

        this.isVisible = visible;

        if (visible) {
            TeamInfo teamInfo = MainScreenData.getProperties().contestInfo.getParticipant(teamId);
            this.teamId = teamId;
            this.problemId = problemId;
            this.newsMessage = newsMessage;
            this.runId = runId;

            teamName = teamInfo.getName();
            infoType = type;
            this.isLive = isLive;
        }

        this.timestamp = System.currentTimeMillis();
        recache();

        return true;
    }

    public void update() {
        boolean change = false;
        synchronized (breakingNewsLock) {
            if (System.currentTimeMillis() > timestamp +
                    MainScreenData.getProperties().breakingNewsTimeToShow +
                    MainScreenData.getProperties().sleepTime) {
                isVisible = false;
                change = true;
            }
        }
        if (change) {
            recache();
        }
    }

    public String toString() {
        return timestamp + "\n" + isVisible + "\n" + infoType + "\n" + isLive + "\n" + teamName + "\n" + (char) ('A' + problemId);
    }

    public String getStatus() {
        if (isVisible) {
            String status = "Breaking news (%s) are shown for team %s and problem %c for %d seconds";

            long time = (timestamp + MainScreenData.getProperties().breakingNewsTimeToShow
                    + MainScreenData.getProperties().sleepTime
                    - System.currentTimeMillis()) / 1000;
            String type = isLive ? infoType : "record";

            return String.format(status, type, teamName, (char) ('A' + problemId), time);
        } else {
            return "Breaking news aren't shown";
        }

    }

    public long timestamp;
    public boolean isVisible;
    public int teamId;
    public String teamName;
    public int problemId;
    public int runId;
    public String infoType;
    public boolean isLive;
    public String newsMessage;

    private static int lastShowedRun = 0;

    final private Object breakingNewsLock = new Object();
}
