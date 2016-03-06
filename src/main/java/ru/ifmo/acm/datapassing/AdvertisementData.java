package ru.ifmo.acm.datapassing;

import com.vaadin.data.util.BeanItemContainer;
import ru.ifmo.acm.mainscreen.Advertisement;
import ru.ifmo.acm.mainscreen.MainScreenData;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class AdvertisementData implements CachedData {
    public AdvertisementData initialize() {
        AdvertisementData data = MainScreenData.getMainScreenData().advertisementData;
        synchronized (advertisementLock) {
            this.timestamp = data.timestamp;
            this.isVisible = data.isVisible;
            this.advertisement = data.advertisement == null ? new Advertisement("") : new Advertisement(data.advertisement.getAdvertisement());
        }
        return this;
    }

    public void recache() {
        Data.cache.refresh(AdvertisementData.class);
    }

    public void update() {
        boolean change = false;
        synchronized (advertisementLock) {
            if (System.currentTimeMillis() > timestamp + MainScreenProperties.getInstance().timeAdvertisement) {
                //System.err.println("Big idle for advert");
                isVisible = false;
                change = true;
            }
        }
        if (change) {
            recache();
        }
    }

    public String toString() {
        return isVisible ? "Advertisement \"" + advertisement.getAdvertisement() + "\"" : "No advertisement now";
    }

    public void setValue(Object key, String value) {
        MainScreenProperties.getInstance().backupAdvertisements.setProperty(key, "advertisement", value);
    }

    public BeanItemContainer<Advertisement> getContainer() {
        return MainScreenProperties.getInstance().backupAdvertisements.getContainer();
    }

    public void removeAdvertisement(Advertisement advertisement) {
        MainScreenProperties.getInstance().backupAdvertisements.removeItem(advertisement);
    }

    public void addAdvertisement(Advertisement advertisement) {
        MainScreenProperties.getInstance().backupAdvertisements.addItem(advertisement);
    }

    public void setAdvertisementVisible(boolean visible, Advertisement advertisement) {
        synchronized (advertisementLock) {
            timestamp = System.currentTimeMillis();
            isVisible = visible;
            this.advertisement = advertisement;
        }
        recache();
    }

    public long timestamp;
    public boolean isVisible;
    public Advertisement advertisement;

    final private Object advertisementLock = new Object();
}
