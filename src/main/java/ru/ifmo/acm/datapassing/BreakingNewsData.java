package ru.ifmo.acm.datapassing;

import com.vaadin.data.util.BeanItemContainer;
import ru.ifmo.acm.events.TeamInfo;
import ru.ifmo.acm.mainscreen.Advertisement;
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

    public synchronized boolean update(boolean visible, String type, boolean isLive, String info) {
        String[] zz = info.split(" ");
        int teamId = Integer.parseInt(zz[0]);
        int problemId = Integer.parseInt(zz[1]) - 'A';
        if (visible) {
            if (timestamp + MainScreenData.getProperties().sleepTime > System.currentTimeMillis() && isVisible) {
                return false;
            }
            TeamInfo teamInfo = MainScreenData.getProperties().contestInfo.getParticipant(teamId);
            isVisible = true;
            this.teamId = teamId;
            this.problemId = problemId;
            teamName = teamInfo.getName();
            infoType = type;
            this.isLive = isLive;
        } else {
            isVisible = false;
            timestamp = System.currentTimeMillis();
        }
        recache();
        return true;
    }

    public String toString() {
        return timestamp + "\n" + isVisible + "\n" + infoType + "\n" + isLive + "\n" + teamName + "\n" + (char) ('A' + problemId);
    }

    public long timestamp;
    public boolean isVisible;
    public int teamId;
    public String teamName;
    public int problemId;
    public String infoType;
    public boolean isLive;

    final private Object advertisementLock = new Object();
}
