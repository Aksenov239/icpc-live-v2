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
        return this;
    }

    public void recache() {
        Data.cache.refresh(BreakingNewsData.class);
    }

    public synchronized boolean setNewsVisible(boolean visible, String type, boolean isLive, String info) {
        this.isVisible = visible;

        if (visible) {
            String[] zz = info.split(" ");
            int teamId = Integer.parseInt(zz[0]);
            int problemId = zz[1].charAt(0) - 'A';

            if (timestamp + MainScreenData.getProperties().sleepTime > System.currentTimeMillis() && isVisible) {
                return false;
            }
            TeamInfo teamInfo = MainScreenData.getProperties().contestInfo.getParticipant(teamId);
            this.teamId = teamId;
            this.problemId = problemId;
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
            //System.err.println(PCMSEventsLoader.getInstance().getContestData().getTeamsNumber());
            if (System.currentTimeMillis() > timestamp +
                    MainScreenData.getProperties().breakingNewsTimeToShow) {
                isVisible = false;
                change = true;
            }
        }
        if (change)
            recache();
    }

    public String toString() {
        return timestamp + "\n" + isVisible + "\n" + infoType + "\n" + isLive + "\n" + teamName + "\n" + (char) ('A' + problemId);
    }

    public String getStatus() {
        if (isVisible) {
            String status = "Breaking news (%s) are shown for team %d and problem %c for %d seconds";

            long time = (timestamp + MainScreenData.getProperties().breakingNewsTimeToShow - System.currentTimeMillis()) / 1000;
            String type = isLive ? "Live" : infoType;

            return String.format(status, type, teamId, (char)('A' + problemId), time);
        } else {
            return "Breaking news aren't shown";
        }

    }

    public long timestamp;
    public boolean isVisible;
    public int teamId;
    public String teamName;
    public int problemId;
    public String infoType;
    public boolean isLive;

    final private Object breakingNewsLock = new Object();
}
