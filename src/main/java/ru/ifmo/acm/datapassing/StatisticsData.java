package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.mainscreen.MainScreenData;

public class StatisticsData implements CachedData {

    public void recache() {
        Data.cache.refresh(StatisticsData.class);
    }

    public synchronized void hide() {
        setVisible(false);
    }

    public synchronized void setVisible(boolean visible) {
        timestamp = System.currentTimeMillis();
        isVisible = visible;
        recache();
    }

    public boolean isVisible() {
        return isVisible;
    }

    public StatisticsData initialize() {
        StatisticsData data = MainScreenData.getMainScreenData().statisticsData;
        this.timestamp = data.timestamp;
        this.isVisible = data.isVisible;

        return this;
    }

    private long timestamp;
    private boolean isVisible;
}
