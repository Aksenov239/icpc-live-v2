package org.icpclive.datapassing;

import org.icpclive.mainscreen.MainScreenData;
import org.icpclive.events.TeamInfo;

public class SplitScreenData extends CachedData {
    public SplitScreenData() {
        for (int i = 0; i < 4; i++) {
            controllerDatas[i] = new TeamData();
            isAutomatic[i] = true;
        }
    }

    @Override
    public SplitScreenData initialize() {
        SplitScreenData data = MainScreenData.getMainScreenData().splitScreenData;
        this.controllerDatas = data.controllerDatas;
        this.isAutomatic = data.isAutomatic;

        return this;
    }

    public void recache() {
        Data.cache.refresh(SplitScreenData.class);
    }

    public synchronized boolean setInfoVisible(int controllerId, boolean visible, String type, TeamInfo teamInfo) {
        return controllerDatas[controllerId].setInfoManual(visible, type, teamInfo);
    }

    public synchronized boolean isVisible(int controllerId) {
        return controllerDatas[controllerId].isVisible();
    }

    public synchronized String getTeamString(int controllerId) {
        return controllerDatas[controllerId].getTeamString();
    }

    public synchronized String infoStatus(int controllerId) {
        return controllerDatas[controllerId].infoStatus();
    }

    public synchronized int getTeamId(int contollerId) {
        return controllerDatas[contollerId].getTeamId();
    }

    public TeamData[] controllerDatas = new TeamData[4];
    public boolean[] isAutomatic = new boolean[4];
    public long[] timestamps = new long[4];
}
