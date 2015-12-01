package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.mainscreen.MainScreenData;
import ru.ifmo.acm.mainscreen.Advertisement;
import ru.ifmo.acm.mainscreen.statuses.AdvertisementStatus;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class AdvertisementData implements CachedData {
    public long timestamp;
    public boolean isVisible;
    public Advertisement advertisement;

    public AdvertisementData initialize() {
        AdvertisementStatus status = MainScreenData.getMainScreenData().advertisementStatus;
        status.initialize(this);
        return this;
    }

//    public AdvertisementData() {
//    }
//
//    public AdvertisementData(long timestamp, boolean isVisible, Advertisement advertisement) {
//        this.timestamp = timestamp;
//        this.isVisible = isVisible;
//        this.advertisement = advertisement;
//    }

    public String toString() {
        return isVisible ? "Advertisement \"" + advertisement.getAdvertisement() + "\"" : "No advertisement now";
    }
}
