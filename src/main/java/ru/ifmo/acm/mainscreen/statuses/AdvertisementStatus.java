package ru.ifmo.acm.mainscreen.statuses;

import com.vaadin.data.util.BeanItemContainer;
import ru.ifmo.acm.backup.BackUp;
import ru.ifmo.acm.datapassing.AdvertisementData;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.mainscreen.Advertisement;

public class AdvertisementStatus {
    public AdvertisementStatus(String backupFilename, long timeToShow) {
        this.backupFilename = backupFilename;
        advertisements = new BackUp<>(Advertisement.class, backupFilename);
        this.timeToShow = timeToShow;
    }

    public void recache() {
        Data.cache.refresh(AdvertisementData.class);
    }

    public void setAdvertisementVisible(boolean visible, Advertisement advertisement) {
        synchronized (advertisementLock) {
            advertisementTimestamp = System.currentTimeMillis();
            isAdvertisementVisible = visible;
            advertisementValue = advertisement;
        }
        recache();
    }

    public void update() {
        boolean change = false;
        //System.err.println("Update! " + advertisementTimestamp + " " + timeToShow + " " + System.currentTimeMillis());
        synchronized (advertisementLock) {
            if (System.currentTimeMillis() > advertisementTimestamp + timeToShow) {
                //System.err.println("Big idle for advert");
                isAdvertisementVisible = false;
                change = true;
            }
        }
        if (change) {
            recache();
        }
    }

//    public String advertisementStatus() {
//        synchronized (advertisementLock) {
//            return advertisementTimestamp + "\n" + isAdvertisementVisible + "\n" + advertisementValue;
//        }
//    }

    public AdvertisementData advertisementStatus() {
        synchronized (advertisementLock) {
            return new AdvertisementData().initialize();
        }
    }

    public void addAdvertisement(Advertisement advertisement) {
//        synchronized (advertisementLock) {
//            advertisements.addItem(advertisement);
//        }
        advertisements.addItem(advertisement);
    }

    public void removeAdvertisement(Advertisement advertisement) {
        //synchronized (advertisements) {
//            advertisements.removeItem(advertisement);
//        }
        advertisements.removeItem(advertisement);
    }

    public BeanItemContainer<Advertisement> getContainer() {
        return advertisements.getContainer();
    }

    public void initialize(AdvertisementData data) {
        synchronized (advertisementLock) {
            data.timestamp = advertisementTimestamp;
            data.isVisible = isAdvertisementVisible;
            data.advertisement = advertisementValue == null ? new Advertisement("") : new Advertisement(advertisementValue.getAdvertisement());
        }
    }

    public void setValue(Object key, String value) {
        advertisements.setProperty(key, "advertisement", value);
    }

    private long advertisementTimestamp;
    private boolean isAdvertisementVisible;
    private Advertisement advertisementValue;
    final private Object advertisementLock = new Object();

    final private BackUp<Advertisement> advertisements;
    final String backupFilename;
    final long timeToShow;
}
