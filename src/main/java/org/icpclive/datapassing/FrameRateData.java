package org.icpclive.datapassing;

import org.icpclive.webadmin.mainscreen.MainScreenData;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class FrameRateData extends CachedData {
    public FrameRateData() {
        isVisible = false;
    }

    public void recache() {
        Data.cache.refresh(FrameRateData.class);
    }

    public synchronized void setFrameRateVisible(boolean visible) {
        timestamp = System.currentTimeMillis();
        isVisible = visible;
        recache();
    }

    public String getStatus() {
        if (isVisible) {
            return "Frame rate is shown";
        } else {
            return "Frame rate is not shown";
        }
    }

    public FrameRateData initialize() {
        FrameRateData data = MainScreenData.getMainScreenData().frameRateData;
        this.timestamp = data.timestamp;
        this.isVisible = data.isVisible;
        this.delay = data.delay;

        return this;
    }

    public boolean isVisible;
}
