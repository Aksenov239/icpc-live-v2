package ru.ifmo.acm.datapassing;

import com.google.gson.*;
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
        this.teamInfo = data.teamInfo;

        return this;
    }

    public void recache() {
        Data.cache.refresh(TeamData.class);
    }

    public synchronized boolean setInfoVisible(boolean visible, String type, String teamName) {
        if (visible) {
            String alias = null;
            if (teamName != null) {
                alias = teamName.split("[:.]")[1].trim();
            }
            if (teamInfo != null && ((
                    (teamInfo instanceof PCMSTeamInfo && ((PCMSTeamInfo) teamInfo).getAlias().equals(alias)) ||
                            (teamInfo instanceof WFTeamInfo && (teamInfo.getName().equals(alias) || teamInfo.getShortName().equals(alias)))
//             && isInfoVisible)) {
                            || timestamp + MainScreenData.getProperties().sleepTime > System.currentTimeMillis()) && isVisible)) {
                return false;
            }
            timestamp = System.currentTimeMillis();
            isVisible = true;
            infoType = type;
            teamInfo = MainScreenData.getProperties().contestInfo.getParticipant(alias);
            System.err.println(alias + " " + teamInfo.getId());
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
        return teamInfo.getShortName() + " :" + ((PCMSTeamInfo) teamInfo).getAlias();
    }

    public synchronized String infoStatus() {
        return timestamp + "\n" + isVisible + "\n" + infoType + "\n" + (teamInfo == null ? null : teamInfo.getName());
    }

    public synchronized int getTeamId() {
        return teamInfo.getId();
    }

    public long timestamp;
    public boolean isVisible;
    public String infoType;

    private TeamInfo teamInfo;

    public static class TeamDataSerializer implements JsonSerializer<TeamData> {

        @Override
        public JsonElement serialize(TeamData teamData, Type type, JsonSerializationContext jsonSerializationContext) {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("timestamp", teamData.timestamp);
            jsonObject.addProperty("isVisible", teamData.isVisible);
            jsonObject.addProperty("infoType", teamData.infoType);

            // System.err.println("Hello from TeamDataSerializer!");
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
            teamData.infoType = (jsonObject.get("infoType") == null) ? null: jsonObject.get("infoType").getAsString();
            teamData.teamInfo = null;

            //System.err.println("Hello from TeamDataDeserializer!");

            return teamData;
        }
    }
}
