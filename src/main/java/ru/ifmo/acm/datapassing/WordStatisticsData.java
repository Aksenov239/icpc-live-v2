package ru.ifmo.acm.datapassing;

import com.google.gson.*;
import ru.ifmo.acm.mainscreen.MainScreenData;
import ru.ifmo.acm.mainscreen.MainScreenProperties;
import ru.ifmo.acm.mainscreen.Words.WordStatistics;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

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

    public synchronized String setWordVisible(WordStatistics word) {
        if (isVisible) {
            return "The word statistics is currently visible";
        }
        this.word = word;
        timestamp = System.currentTimeMillis();
        isVisible = true;
        return null;
    }

    public synchronized void update() {
        if (!isVisible) {
            return;
        }
        MainScreenProperties properties = MainScreenData.getProperties();
        long now = System.currentTimeMillis();
        if (now - timestamp >= properties.wordTimeToShow) {
            isVisible = false;
            return;
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
