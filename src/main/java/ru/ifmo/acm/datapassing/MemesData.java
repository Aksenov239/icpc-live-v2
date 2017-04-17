package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.mainscreen.MainScreenData;
import ru.ifmo.acm.mainscreen.MainScreenProperties;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Meepo on 4/16/2017.
 */
public class MemesData extends CachedData {
    @Override
    public MemesData initialize() {
        MemesData memesData = MainScreenData.getMainScreenData().memesData;
        currentMeme = memesData.currentMeme;
        count = memesData.count;
        return memesData;
    }

    public synchronized String setMemesVisible() {
        if (isVisible) {
            return "The memes statistics is currently visible";
        }
        isVisible = true;
        timestamp = System.currentTimeMillis();
        currentMemeId = -1;
        return null;
    }

    public synchronized void update() {
        if (!isVisible) {
            return;
        }
        MainScreenProperties properties = MainScreenData.getProperties();
        long now = System.currentTimeMillis();
        if (now - timestamp >= properties.oneMemeTimeToShow * properties.memes.size()) {
            isVisible = false;
            return;
        }
        int probableId = (int) ((now - timestamp) / properties.oneMemeTimeToShow);
        if (probableId != currentMemeId) {
            currentMemeId = probableId;
            currentMeme = properties.memes.get(currentMemeId);
            count = memesCount[currentMemeId].get();
        }
    }

    public String toString() {
        return isVisible ? "Showing memes statistics for " + Math.max(0, timestamp +
                MainScreenData.getProperties().oneMemeTimeToShow * MainScreenData.getProperties().memes.size() -
                System.currentTimeMillis()) + " more seconds" : "Memes statistics is not shown";
    }

    public void recache() {
        Data.cache.refresh(MemesData.class);
    }

    public static void processMessage(String message) {
        MainScreenProperties properties = MainScreenData.getProperties();
        boolean found = false;
        for (int i = 0; i < properties.memes.size(); i++) {
            String meme = properties.memes.get(i);
            for (String variations : properties.memesContent.get(meme)) {
                if (message.contains(variations)) {
                    System.err.println("Found meme: " + meme);
                    memesCount[i].incrementAndGet();
                    break;
                }
            }
        }
    }

    private boolean isVisible;
    private long timestamp;
    private String currentMeme;
    private int count;
    private int currentMemeId;
    public static AtomicInteger[] memesCount;
}
