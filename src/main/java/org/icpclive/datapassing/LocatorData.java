package org.icpclive.datapassing;

import com.google.gson.*;
import net.egork.teaminfo.data.Team;
import org.icpclive.backend.player.widgets.locator.LocatorCamera;
import org.icpclive.events.ContestInfo;
import org.icpclive.events.EventsLoader;
import org.icpclive.events.TeamInfo;
import org.icpclive.webadmin.mainscreen.MainScreenData;
import org.icpclive.webadmin.mainscreen.Polls.Poll;

import java.io.File;
import java.lang.reflect.Type;
import java.util.*;

public class LocatorData extends CachedData {

    public LocatorData() {
    }

    @Override
    public LocatorData initialize() {
        LocatorData data = MainScreenData.getMainScreenData().locatorData;
        this.timestamp = data.timestamp;
        this.delay = data.delay;
        this.visible = data.visible;
        this.teamInfos = data.teamInfos;
        this.cameraID = data.cameraID;
        return this;
    }

    boolean visible;
    List<TeamInfo> teamInfos;
    int cameraID;
    String status = "Locator is not shown";

    public synchronized void setTeams(Collection<TeamInfo> teams) {
        teamInfos = new ArrayList<>(teams);
    }

    public synchronized void setCameraID(int newCameraID) {
        cameraID = newCameraID;
    }

    public synchronized String setVisible() {
        if (teamInfos == null || teamInfos.size() == 0) {
            return "You should at least one team";
        }
        String error = checkOverlays();
        if (error != null) {
            return error;
        }
        if (visible) {
            return "You should hide locator first";
        }
        timestamp = System.currentTimeMillis();
        visible = true;
        status = "Show locator";
        recache();
        return null;
    }

    public synchronized void hide() {
        visible = false;
        timestamp = System.currentTimeMillis();
        status = "Locator is not shown";
        recache();
    }

    public boolean isVisible() {
        return visible;
    }

    public synchronized List<TeamInfo> getTeams() {
        return teamInfos;
    }

    public synchronized int getCameraID() {
        return cameraID;
    }

    public synchronized String checkOverlays() {
        MainScreenData mainScreenData = MainScreenData.getMainScreenData();
        if (mainScreenData.teamData.isVisible()) {
            return mainScreenData.teamData.getOverlayError();
        }
        return null;
    }

    public synchronized void switchOverlaysOff() {
        MainScreenData mainScreenData = MainScreenData.getMainScreenData();
        boolean turnOff = false;
        if (mainScreenData.standingsData.isVisible) {
            mainScreenData.standingsData.hide();
            turnOff = true;
        }
        if (mainScreenData.statisticsData.isVisible()) {
            mainScreenData.statisticsData.hide();
            turnOff = true;
        }
        if (mainScreenData.wordStatisticsData.isVisible) {
            mainScreenData.wordStatisticsData.hide();
            turnOff = true;
        }
        if (mainScreenData.pollData.isVisible) {
            mainScreenData.pollData.hide();
            turnOff = true;
        }
        if (mainScreenData.pictureData.isVisible()) {
            mainScreenData.pictureData.hide();
            turnOff = true;
        }
        if (turnOff) {
            delay = MainScreenData.getProperties().overlayedDelay;
        } else {
            delay = 0;
        }
    }

    public String getOverlayError() {
        return "You should hide locator view first";
    }

    public synchronized String getStatus() {
        if (visible) {
            return status + " for " + (System.currentTimeMillis() - timestamp) / 1000 + " seconds";
        } else {
            return status;
        }
    }

    private synchronized void recache() {
        Data.cache.refresh(LocatorData.class);
    }

    public static class LocatorDataSerializer implements JsonSerializer<LocatorData> {
        @Override
        public JsonElement serialize(LocatorData locatorData, Type type, JsonSerializationContext jsonSerializationContext) {
            final JsonObject object = new JsonObject();
            object.addProperty("timestamp", locatorData.timestamp);
            object.addProperty("delay", locatorData.delay);
            object.addProperty("visible", locatorData.visible);
            object.addProperty("cameraID", locatorData.cameraID);
            JsonArray optionsArray = new JsonArray();
            if (locatorData.getTeams() != null) {
                for (TeamInfo teamInfo : locatorData.getTeams()) {
                    JsonObject jsonOption = new JsonObject();
                    jsonOption.addProperty("id", teamInfo.getId());
                    optionsArray.add(jsonOption);
                }
            }
            object.add("teams", optionsArray);
            return object;
        }
    }

    public static class LocatorDataDeserializer implements JsonDeserializer<LocatorData> {
        @Override
        public LocatorData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            LocatorData data = new LocatorData();

            final JsonObject jsonObject = jsonElement.getAsJsonObject();
            data.timestamp = jsonObject.get("timestamp").getAsLong();
            data.delay = jsonObject.get("delay").getAsInt();
            data.visible = jsonObject.get("visible").getAsBoolean();
            data.cameraID = jsonObject.get("cameraID").getAsInt();
            JsonArray teamsArray = jsonObject.get("teams").getAsJsonArray();
            List<TeamInfo> teams = new ArrayList<>();
            ContestInfo contestInfo = EventsLoader.getInstance().getContestData();
            for (int i = 0; i < teamsArray.size(); i++) {
                JsonObject jo = teamsArray.get(i).getAsJsonObject();
                teams.add(contestInfo.getParticipant(jo.get("id").getAsInt()));
            }
            data.setTeams(teams);
            return data;
        }
    }

}
