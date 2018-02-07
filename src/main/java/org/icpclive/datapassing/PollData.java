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
        if (MainScreenData.getMainScreenData().teamData.isVisible) {
            return MainScreenData.getMainScreenData().teamData.getOverlayError();
        }
        if (MainScreenData.getMainScreenData().statisticsData.isVisible()) {
            return MainScreenData.getMainScreenData().standingsData.getOverlayError();
        }
        if (MainScreenData.getMainScreenData().standingsData.isVisible) {
            return MainScreenData.getMainScreenData().standingsData.getOverlayError();
        }
        return null;
    }

    public void update() {
//        System.err.println("Update: " + System.currentTimeMillis() + " " + timestamp + " " + MainScreenData.getProperties().pollTimeToShow);
        synchronized (pollLock) {
            if (isVisible && System.currentTimeMillis() > timestamp + MainScreenData.getProperties().pollTimeToShow) {
                isVisible = false;
                recache();
            }
        }
    }

    public void hide() {
        synchronized (pollLock) {
            isVisible = false;
            recache();
        }
    }

    public String setPollVisible(Poll poll) {
        synchronized (pollLock) {
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
                " for " +
                (MainScreenData.getProperties().pollTimeToShow - (System.currentTimeMillis() - timestamp)) / 1000 +
                " seconds more"
                : "No poll to show";
    }

    public void recache() {
        Data.cache.refresh(PollData.class);
    }

    public Poll poll;
    public long timestamp;
    public boolean isVisible = false;

    Object pollLock = new Object();

    public static class PollDataSerializer implements JsonSerializer<PollData> {
        @Override
        public JsonElement serialize(PollData pollData, Type type, JsonSerializationContext jsonSerializationContext) {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("timestamp", pollData.timestamp);
            jsonObject.addProperty("isVisible", pollData.isVisible);
            Poll poll = pollData.poll;
            jsonObject.addProperty("PollQuestion", poll == null ? "null" : poll.getQuestion());
            if (poll != null) {
                jsonObject.addProperty("PollHashtag", poll.getHashtag());
                jsonObject.addProperty("teamOptions", poll.getTeamOptions());
                JsonArray optionsArray = new JsonArray();
                for (Poll.Option options : poll.getData()) {
                    JsonObject jsonOption = new JsonObject();
                    jsonOption.addProperty("id", options.id);
                    jsonOption.addProperty("option", options.option);
                    jsonOption.addProperty("votes", options.votes);
                    optionsArray.add(jsonOption);
                }
                jsonObject.add("PollData", optionsArray);
            }
            return jsonObject;
        }
    }

    public static class PollDataDeserializer implements JsonDeserializer<PollData> {
        @Override
        public PollData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
            PollData pollData = new PollData();

            final JsonObject jsonObject = jsonElement.getAsJsonObject();
            String question = jsonObject.get("PollQuestion").getAsString();
            if (!question.equals("null")) {
                String hashtag = jsonObject.get("PollHashtag").getAsString();
                Poll poll = new Poll(question, hashtag, false);
                poll.setTeamOptions(jsonObject.get("teamOptions").getAsBoolean());
                JsonArray pollOptions = jsonObject.get("PollData").getAsJsonArray();
                for (int i = 0; i < pollOptions.size(); i++) {
                    JsonObject jsonOption = pollOptions.get(i).getAsJsonObject();
                    Poll.Option option = new Poll.Option(jsonOption.get("id").getAsInt(),
                            jsonOption.get("option").getAsString(),
                            jsonOption.get("votes").getAsInt());
                    poll.options.put(option.option, option);
                }
                pollData.poll = poll;
            }
            pollData.timestamp = jsonObject.get("timestamp").getAsInt();
            pollData.isVisible = jsonObject.get("isVisible").getAsBoolean();
            return pollData;
        }
    }
}
