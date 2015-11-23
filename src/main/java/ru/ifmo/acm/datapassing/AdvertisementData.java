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
        timestamp = status.advertisementTimestamp;
        isVisible = status.isAdvertisementVisible;
        advertisement = status.advertisementValue;
        return this;
    }
}
