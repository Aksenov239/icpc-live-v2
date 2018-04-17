package org.icpclive.datapassing;

import org.icpclive.webadmin.mainscreen.MainScreenData;
import org.icpclive.webadmin.mainscreen.MainScreenProperties;
import org.icpclive.webadmin.mainscreen.statistics.WordStatistics;

/**
 * Created by Meepo on 4/16/2017.
 */
public class WordStatisticsData extends CachedData {
    @Override
    public WordStatisticsData initialize() {
        WordStatisticsData wordStatisticsData = MainScreenData.getMainScreenData().wordStatisticsData;
        isVisible = wordStatisticsData.isVisible;
        word = wordStatisticsData.word;
        return this;
    }

    public String getOverlayError() {
        return "You have to wait while word statistics is shown";
    }

    public String checkOverlays() {
        if (MainScreenData.getMainScreenData().factData.isVisible) {
            return MainScreenData.getMainScreenData().factData.getOverlayError();
        }
        return null;
    }

    public synchronized String setWordVisible(WordStatistics word) {
        String check = checkOverlays();
        if (check != null) {
            return check;
        }

        if (isVisible) {
            return "The word statistics is currently visible";
        }
        this.word = word;
        timestamp = System.currentTimeMillis();
        isVisible = true;
        recache();
        return null;
    }

    public void hide() {
        isVisible = false;
        recache();
    }

    public synchronized void update() {
        if (!isVisible) {
            return;
        }
        MainScreenProperties properties = MainScreenData.getProperties();
        long now = System.currentTimeMillis();
        if (now - timestamp >= properties.wordTimeToShow) {
            isVisible = false;
            recache();
        }
        recache();
    }

    public String toString() {
        return isVisible ? "Showing statistics of word " + word.getWord() +
                " for " + Math.max(0, timestamp +
                MainScreenData.getProperties().wordTimeToShow -
                System.currentTimeMillis()) / 1000 + " more seconds" : "Word statistics is not shown";
    }

    public void recache() {
        Data.cache.refresh(WordStatisticsData.class);
    }

    public boolean isVisible;
    public WordStatistics word;

}
