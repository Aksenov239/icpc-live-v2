package ru.ifmo.acm.mainscreen.statuses;

import com.vaadin.data.util.BeanItemContainer;
import ru.ifmo.acm.backup.BackUp;
import ru.ifmo.acm.mainscreen.Advertisement;

public class AdvertisementStatus {
    public AdvertisementStatus(String backupFilename, long timeToShow) {
        this.backupFilename = backupFilename;
        advertisements = new BackUp<>(Advertisement.class, backupFilename);
        this.timeToShow = timeToShow;
    }

    public synchronized void setAdvertisementVisible(boolean visible, Advertisement advertisement) {
        synchronized (advertisementLock) {
            advertisementTimestamp = System.currentTimeMillis();
            isAdvertisementVisible = visible;
            advertisementValue = advertisement;
        }
    }

    public synchronized void update() {
        synchronized (advertisementLock) {
            if (System.currentTimeMillis() > advertisementTimestamp + timeToShow) {
                isAdvertisementVisible = false;
            }
        }
    }

    public synchronized String advertisementStatus() {
        synchronized (advertisementLock) {
            return advertisementTimestamp + "\n" + isAdvertisementVisible + "\n" + advertisementValue;
        }
    }

    public void addAdvertisement(Advertisement advertisement) {
//        synchronized (advertisements) {
//            advertisements.addBean(advertisement);
//        }
        advertisements.addItem(advertisement);
    }

    public void removeAdvertisement(Advertisement advertisement) {
//        synchronized (advertisements) {
//            advertisements.removeItem(advertisement);
//        }
        advertisements.removeItem(advertisement);
    }

    public BeanItemContainer<Advertisement> getContainer() {
        return advertisements.getContainer();
    }

    public void setValue(Object key, String value) {
        advertisements.setProperty(key, "advertisement", value);
    }

    public long advertisementTimestamp;
    public boolean isAdvertisementVisible;
    public Advertisement advertisementValue;
    final private Object advertisementLock = new Object();

    final private BackUp<Advertisement> advertisements;
    final String backupFilename;
    final long timeToShow;
}
