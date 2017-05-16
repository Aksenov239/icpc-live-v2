package ru.ifmo.acm.datapassing;

import com.google.gson.*;
import ru.ifmo.acm.mainscreen.MainScreenData;
import ru.ifmo.acm.mainscreen.MainScreenProperties;

import java.lang.reflect.Type;
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
        isVisible = memesData.isVisible;
        return memesData;
    }

    public synchronized String setMemesVisible() {
        if (isVisible) {
            return "The memes statistics is currently visible";
        }
        MainScreenProperties properties = MainScreenData.getProperties();
        currentMemeId = 0;
        currentMeme = properties.memes.get(0);
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
                System.currentTimeMillis()) / 1000 + " more seconds" : "Memes statistics is not shown";
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

    public boolean isVisible;
    public String currentMeme;
    public int count;
    private int currentMemeId;
    public static AtomicInteger[] memesCount;

    public static class MemesDataSerializer implements JsonSerializer<MemesData> {
        @Override
        public JsonElement serialize(MemesData memesData, Type type, JsonSerializationContext jsonSerializationContext) {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("isVisible", memesData.isVisible);
            jsonObject.addProperty("currentMeme", memesData.currentMeme);
            jsonObject.addProperty("count", memesData.count);
            return jsonObject;
        }
    }

    public static class MemesDataDeserializer implements JsonDeserializer<MemesData> {
        @Override
        public MemesData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
            MemesData memesData = new MemesData();
            final JsonObject jsonObject = jsonElement.getAsJsonObject();
            memesData.isVisible = jsonObject.get("isVisible").getAsBoolean();
            memesData.currentMeme = jsonObject.get("currentMeme") == null ? null :
                    jsonObject.get("currentMeme").getAsString();
            memesData.count = jsonObject.get("count").getAsInt();
            return memesData;
        }
    }
}
