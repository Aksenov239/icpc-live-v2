package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.mainscreen.MainScreenData;

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

    public void update() {

    }

    public void recache() {
        Data.cache.refresh(MemesData.class);
    }

    private long timestamp;
    private String currentMeme;
    private int count;
    private int currentMemeId;
}
