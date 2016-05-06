package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.backup.BackUp;
import ru.ifmo.acm.events.EventsLoader;
import ru.ifmo.acm.events.TeamInfo;
import ru.ifmo.acm.events.WF.WFContestInfo;
import ru.ifmo.acm.events.WF.WFRunInfo;
import ru.ifmo.acm.mainscreen.BreakingNews.BreakingNews;
import ru.ifmo.acm.mainscreen.MainScreenData;
import ru.ifmo.acm.mainscreen.Utils;

import java.util.ArrayList;
import java.util.List;

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
        this.infoType = data.infoType;
        return this;
    }

    public void recache() {
        Data.cache.refresh(BreakingNewsData.class);
    }

    public synchronized boolean setNewsVisible(boolean visible, String type, boolean isLive, String newsMessage, int teamId, int problemId) {
        if (visible && isVisible) {
            return false;
        }

        this.isVisible = visible;

        if (visible) {
            TeamInfo teamInfo = MainScreenData.getProperties().contestInfo.getParticipant(teamId);
            this.teamId = teamId;
            this.problemId = problemId;
            this.newsMessage = newsMessage;

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

    public static Utils.StoppedThread getUpdaterThread() {
        Utils.StoppedThread tableUpdater = new Utils.StoppedThread(new Utils.StoppedRunnable() {
            @Override
            public void run() {
                while (!stop) {
                    final BackUp<BreakingNews> backUp = MainScreenData.getProperties().backupBreakingNews;
                    List<BreakingNews> toDelete = new ArrayList<>();
                    backUp.getData().forEach(news -> {
                        if (news.getTimestamp() + MainScreenData.getProperties().breakingNewsTimeToKeepInTable > System.currentTimeMillis()) {
                            toDelete.add(news);
                        }
                    });
                    toDelete.forEach(msg -> backUp.removeItem(msg));

                    WFContestInfo contestInfo = null;
                    while (contestInfo == null) {
                        contestInfo = (WFContestInfo) EventsLoader.getInstance().getContestData();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    while (lastShowedRun <= contestInfo.getMaxRunId()) {
                        WFRunInfo run = contestInfo.getRun(lastShowedRun);
                        if (run != null) {
                            // TODO: time or timestamp??

                            backUp.addItemAt(0, new BreakingNews(run.getResult(), "" + (char) (run.getProblemNumber() + 'A'), run.getTeamId(), run.getTime()));
                        }
                        lastShowedRun++;
                    }

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return tableUpdater;
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
    public String infoType;
    public boolean isLive;
    public String newsMessage;

    private static int lastShowedRun = 0;

    final private Object breakingNewsLock = new Object();
}
