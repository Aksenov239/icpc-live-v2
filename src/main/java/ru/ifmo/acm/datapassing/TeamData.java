package ru.ifmo.acm.datapassing;

import com.google.gson.*;
import ru.ifmo.acm.events.EventsLoader;
import ru.ifmo.acm.events.PCMS.PCMSTeamInfo;
import ru.ifmo.acm.events.TeamInfo;
import ru.ifmo.acm.events.WF.WFTeamInfo;
import ru.ifmo.acm.mainscreen.MainScreenData;

import java.lang.reflect.Type;

public class TeamData implements CachedData {
    public TeamData() {
    }

    @Override
    public TeamData initialize() {
        TeamData data = MainScreenData.getMainScreenData().teamData;
        this.timestamp = data.timestamp;
        this.isVisible = data.isVisible;
        this.infoType = data.infoType;
        this.teamId = data.teamId;

        return this;
    }

    public void recache() {
        Data.cache.refresh(TeamData.class);
    }

    public synchronized boolean setInfoVisible(boolean visible, String type, String teamValue) {
        if (visible) {
            String alias = null;
            if (teamValue != null) {
                alias = teamValue.split("[:.]")[1].trim();
            }
            System.err.println("Trying to find " + alias);
            TeamInfo teamInfo = MainScreenData.getProperties().contestInfo.getParticipant(alias);
            if (teamInfo == null) {
                return false;
            }
            if (((teamInfo.getId() == teamId && infoType.equals(type))
                    || timestamp + MainScreenData.getProperties().sleepTime > System.currentTimeMillis()) && isVisible) {
                return false;
            }
            timestamp = System.currentTimeMillis();
            isVisible = true;
            infoType = type;
            currentTeamValue = teamValue;
            teamName = teamInfo.getName();
            teamId = teamInfo.getId();

            System.err.println(alias + " " + teamId);
        } else {
            isVisible = false;
            timestamp = System.currentTimeMillis();
        }

        recache();
        return true;
    }

    public synchronized boolean isVisible() {
        return isVisible;
    }

    public synchronized String getTeamString() {
        return currentTeamValue;
    }

    public synchronized String infoStatus() {
        return timestamp + "\n" + isVisible + "\n" + infoType + "\n" + teamName;
    }

    public synchronized int getTeamId() {
        return teamId;
    }

    public long timestamp;
    public boolean isVisible;
    public String infoType = "";
    private String currentTeamValue;
    private String teamName;
    private int teamId = -1;

    public static class TeamDataSerializer implements JsonSerializer<TeamData> {

        @Override
        public JsonElement serialize(TeamData teamData, Type type, JsonSerializationContext jsonSerializationContext) {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("timestamp", teamData.timestamp);
            jsonObject.addProperty("isVisible", teamData.isVisible);
            jsonObject.addProperty("infoType", teamData.infoType);
            jsonObject.addProperty("teamId", teamData.teamId);

            return jsonObject;
        }
    }

    public static class TeamDataDeserializer implements JsonDeserializer<TeamData> {

        @Override
        public TeamData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            TeamData teamData = new TeamData();

            final JsonObject jsonObject = jsonElement.getAsJsonObject();

            teamData.timestamp = jsonObject.get("timestamp").getAsLong();
            teamData.isVisible = jsonObject.get("isVisible").getAsBoolean();
            teamData.infoType = jsonObject.get("infoType").getAsString();
            teamData.teamId = jsonObject.get("teamId").getAsInt();

            //System.err.println("Hello from TeamDataDeserializer!");

            return teamData;
        }
    }
}
