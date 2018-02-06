package org.icpclive.datapassing;

import org.icpclive.mainscreen.MainScreenData;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class ClockData extends CachedData {
    public ClockData() {
        isVisible = true;
    }

    public void recache() {
        Data.cache.refresh(ClockData.class);
    }

    public synchronized void setClockVisible(boolean visible) {
        timestamp = System.currentTimeMillis();
        isVisible = visible;
        recache();
    }

    public boolean isClockVisible() {
        return isVisible;
    }

    public ClockData initialize() {
        ClockData data = MainScreenData.getMainScreenData().clockData;
        this.timestamp = data.timestamp;
        this.isVisible = data.isVisible;
        this.delay = data.delay;

        return this;
    }

    private boolean isVisible;
}
