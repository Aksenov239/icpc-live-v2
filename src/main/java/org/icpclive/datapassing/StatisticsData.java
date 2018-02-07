package org.icpclive.datapassing;

import org.icpclive.webadmin.mainscreen.MainScreenData;

public class StatisticsData extends CachedData {

    public void recache() {
        Data.cache.refresh(StatisticsData.class);
    }

    public synchronized String setVisible(boolean visible) {
        delay = 0;
        if (visible) {
            String outcome = checkOverlays();
            if (outcome != null) {
                return outcome;
            }
            switchOverlaysOff();
        }
        timestamp = System.currentTimeMillis();
        isVisible = visible;
        recache();
        return null;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public StatisticsData initialize() {
        StatisticsData data = MainScreenData.getMainScreenData().statisticsData;
        this.timestamp = data.timestamp;
        this.isVisible = data.isVisible;
        this.delay = data.delay;

        return this;
    }

    public String checkOverlays() {
        if (MainScreenData.getMainScreenData().teamData.isVisible) {
            return MainScreenData.getMainScreenData().teamData.getOverlayError();
        }
        if (MainScreenData.getMainScreenData().pollData.isVisible) {
            return MainScreenData.getMainScreenData().pollData.getOverlayError();
        }
        return null;
    }

    public void switchOverlaysOff() {
        if (MainScreenData.getMainScreenData().standingsData.isVisible &&
                MainScreenData.getMainScreenData().standingsData.isBig) {
            MainScreenData.getMainScreenData().standingsData.hide();
            delay = MainScreenData.getProperties().overlayedDelay;
        }
    }

    @Override
    public void hide() {
        delay = 0;
        setVisible(false);
    }

    private boolean isVisible;
}
