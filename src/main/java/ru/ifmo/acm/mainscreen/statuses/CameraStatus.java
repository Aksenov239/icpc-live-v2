package ru.ifmo.acm.mainscreen.statuses;

import ru.ifmo.acm.datapassing.CameraData;
import ru.ifmo.acm.datapassing.Data;

import java.io.IOException;
import java.util.Properties;

public class CameraStatus {
    private long changeTime;

    public CameraStatus(long changeTime) {
        this.changeTime = changeTime;
        cameraTimestamp = System.currentTimeMillis();
        cameraNumber = 0;
        cameraURL = cameraURLs[0];
    }

    private static String[] cameraURLs;
    public static String[] cameraNames;

    static {
        Properties properties = new Properties();
        try {
            properties.load(CameraStatus.class.getClassLoader().getResourceAsStream("mainscreen.properties"));
            int cameraNumber = Integer.parseInt(properties.getProperty("camera.number", "0"));
            cameraURLs = new String[cameraNumber];
            cameraNames = new String[cameraNumber];
            for (int i = 0; i < cameraNumber; i++) {
                cameraURLs[i] = properties.getProperty("camera.url." + (i + 1));
                cameraNames[i] = properties.getProperty("camera.name." + (i + 1));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recache() {
        Data.cache.refresh(CameraData.class);
    }

    public synchronized boolean setCameraNumber(int cameraNumber) {
        if (cameraTimestamp + changeTime < System.currentTimeMillis()) {
            this.cameraNumber = cameraNumber;
            cameraURL = cameraURLs[cameraNumber];
            cameraTimestamp = System.currentTimeMillis();
            recache();
            return true;
        }
        return false;
    }

    public synchronized String cameraStatus() {
        return cameraTimestamp + "\n" + cameraNames[cameraNumber];
    }

    public synchronized void initialize(CameraData status) {
        status.timestamp = cameraTimestamp;
        status.cameraUrl = cameraURL;
    }

    private long cameraTimestamp;
    private String cameraURL;
    private int cameraNumber = 0;
}
