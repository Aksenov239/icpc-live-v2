package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.mainscreen.MainScreenData;
import ru.ifmo.acm.mainscreen.statuses.ClockStatus;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class ClockData implements CachedData {
    public long timestamp;
    public boolean isVisible;

    public ClockData initialize() {
        ClockStatus status = MainScreenData.getMainScreenData().clockStatus;
        timestamp = status.clockTimestamp;
        isVisible = status.isClockVisible;
        return this;
    }
}
