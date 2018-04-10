package org.icpclive.datapassing;

import com.google.gson.*;
import org.icpclive.webadmin.mainscreen.MainScreenData;
import org.icpclive.webadmin.mainscreen.Polls.Poll;

import java.lang.reflect.Type;

/**
 * Created by Aksenov239 on 26.03.2017.
 */
public class PollData extends CachedData {
    @Override
    public PollData initialize() {
        PollData data = MainScreenData.getMainScreenData().pollData;
        this.poll = data.poll;

        return data;
    }

    public String getOverlayError() {
        return "You have to wait while poll information is shown";
    }

    public String checkOverlays() {
        MainScreenData mainScreenData = MainScreenData.getMainScreenData();
        if (mainScreenData.teamData.isVisible) {
            return mainScreenData.teamData.getOverlayError();
        }
        if (mainScreenData.statisticsData.isVisible()) {
            return mainScreenData.standingsData.getOverlayError();
        }
        if (mainScreenData.standingsData.isVisible) {
            return mainScreenData.standingsData.getOverlayError();
        }
        return null;
    }

    public void hide() {
        synchronized (this) {
            isVisible = false;
            recache();
        }
    }

    public String setPollVisible(Poll poll) {
        synchronized (this) {
            String error = checkOverlays();
            if (error != null) {
                return error;
            }
            if (isVisible) {
                return "Poll " + poll.getHashtag() + " is showed now";
            }
            timestamp = System.currentTimeMillis();
            this.poll = poll;
//            System.err.println("Set poll " + this.poll.getHashtag());
            isVisible = true;
            recache();
        }
        return null;
    }

    public String toString() {
        return isVisible ? "Show poll " + poll.getHashtag() +
                " already for " +
                (System.currentTimeMillis() - timestamp) / 1000 +
                " seconds"
                : "No poll is shown";
    }

    public void recache() {
        Data.cache.refresh(PollData.class);
    }

    public Poll poll;
    public long timestamp;
    public boolean isVisible = false;

    public static class PollDataSerializer implements JsonSerializer<PollData> {
        @Override
        public JsonElement serialize(PollData pollData, Type type, JsonSerializationContext jsonSerializationContext) {
            synchronized (pollData) {
                final JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("timestamp", pollData.timestamp);
                jsonObject.addProperty("isVisible", pollData.isVisible);
                Poll poll = pollData.poll;
                Gson gsonSerializer = new GsonBuilder()
                        .registerTypeAdapter(Poll.class, new Poll.PollSerializer())
                        .create();
                jsonObject.addProperty("poll",
                        poll == null ? "null" : gsonSerializer.toJson(poll));
                return jsonObject;
            }
        }
    }

    public static class PollDataDeserializer implements JsonDeserializer<PollData> {
        @Override
        public PollData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
            PollData pollData = new PollData();

            final JsonObject jsonObject = jsonElement.getAsJsonObject();

            String pollString = jsonObject.get("poll").getAsString();
            if (pollString.equals("null")) {
                pollData.poll = null;
            } else {
                Gson gsonDeserializer = new GsonBuilder()
                        .registerTypeAdapter(Poll.class, new Poll.PollDeserializer())
                        .create();
                pollData.poll = gsonDeserializer.fromJson(pollString, Poll.class);
            }

            pollData.timestamp = jsonObject.get("timestamp").getAsInt();
            pollData.isVisible = jsonObject.get("isVisible").getAsBoolean();
            return pollData;
        }
    }
}
