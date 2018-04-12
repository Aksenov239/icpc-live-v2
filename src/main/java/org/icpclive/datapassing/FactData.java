package org.icpclive.datapassing;

import org.icpclive.webadmin.mainscreen.MainScreenData;

/**
 * Created by Meepo on 4/12/2018.
 */
public class FactData extends CachedData {
    @Override
    public FactData initialize() {
        FactData factData = MainScreenData.getMainScreenData().factData;
        isVisible = factData.isVisible;
        factTitle = factData.factTitle;
        factText = factData.factText;
        return this;
    }

    public String getOverlayError() {
        return "You have to wait while fact is shown";
    }
    public String checkOverlays() {
        if (MainScreenData.getMainScreenData().wordStatisticsData.isVisible) {
            return MainScreenData.getMainScreenData().wordStatisticsData.getOverlayError();
        }
        return null;
    }

    public synchronized String show(String factTitle, String factText) {
        String check = checkOverlays();
        if (check != null) {
            return check;
        }
        if (isVisible) {
            return "The fact data is already shown right now";
        }
        timestamp = System.currentTimeMillis();
        this.factTitle = factTitle;
        this.factText = factText;
        isVisible = true;
        recache();
        return null;
    }

    public synchronized void hide() {
        isVisible = false;
        recache();
    }

    public synchronized void update() {
        if (!isVisible) {
            return;
        }

        if (System.currentTimeMillis() - timestamp >
                MainScreenData.getProperties().factTimeToShow) {
            isVisible = false;
            recache();
        }
    }

    public String toString() {
        return isVisible ? "Showing fact for " +
                Math.max(0, MainScreenData.getProperties().factTimeToShow -
                                (System.currentTimeMillis() - timestamp)) / 1000 +
                " more seconds" : "Fact is not shown";
    }

    public void recache() {
        Data.cache.refresh(FactData.class);
    }

    public boolean isVisible = false;
    public String factTitle = "";
    public String factText = "";
}
