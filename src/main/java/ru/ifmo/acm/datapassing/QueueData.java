package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.mainscreen.MainScreenData;

public class QueueData extends CachedData {
    public QueueData() {
        isVisible = true;
    }

    public void recache() {
        Data.cache.refresh(QueueData.class);
    }

    public synchronized void setVisible(boolean visible) {
        timestamp = System.currentTimeMillis();
        isVisible = visible;
        recache();
    }

    public boolean isQueueVisible() {
        return isVisible;
    }

    public QueueData initialize() {
        QueueData data = MainScreenData.getMainScreenData().queueData;
        this.timestamp = data.timestamp;
        this.isVisible = data.isVisible;

        return this;
    }

    private boolean isVisible;
}
