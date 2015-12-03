package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.mainscreen.MainScreenData;
import ru.ifmo.acm.mainscreen.statuses.CameraStatus;
import ru.ifmo.acm.mainscreen.statuses.TeamStatus;

public class CameraData implements CachedData {
    @Override
    public CameraData initialize() {
        CameraStatus status = MainScreenData.getMainScreenData().cameraStatus;
        status.initialize(this);

        return this;
    }

    public long timestamp;
    public String cameraUrl;
}
