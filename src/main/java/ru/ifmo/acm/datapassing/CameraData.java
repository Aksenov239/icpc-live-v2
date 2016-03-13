package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.mainscreen.MainScreenData;

public class CameraData implements CachedData {
    public CameraData() {
        timestamp = System.currentTimeMillis();
        cameraNumber = 0;
        cameraURL = MainScreenData.getProperties().cameraURLs[0];
    }

    @Override
    public CameraData initialize() {
        CameraData data = MainScreenData.getMainScreenData().cameraData;
        timestamp = data.timestamp;
        cameraURL = data.cameraURL;

        return this;
    }

    public void recache() {
        Data.cache.refresh(CameraData.class);
    }

    public synchronized boolean setCameraNumber(int cameraNumber) {
        if (timestamp + MainScreenData.getProperties().sleepTime < System.currentTimeMillis()) {
            this.cameraNumber = cameraNumber;
            cameraURL = MainScreenData.getProperties().cameraURLs[cameraNumber];
            timestamp = System.currentTimeMillis();
            recache();

            return true;
        }
        return false;
    }

    public synchronized String cameraStatus() {
        return timestamp + "\n" + MainScreenData.getProperties().cameraNames[cameraNumber];
    }

    public long timestamp;
    public String cameraURL;
    private int cameraNumber;
}
