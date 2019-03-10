package org.icpclive.datapassing;

import com.google.gson.*;
import org.icpclive.events.TeamInfo;
import org.icpclive.webadmin.mainscreen.MainScreenData;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PvPData extends CachedData {
    public PvPData() {
        shownTeamId = new int[2];
        teamInfos = new TeamInfo[2];
    }

    @Override
    public PvPData initialize() {
        PvPData pvpData = MainScreenData.getMainScreenData().pvpData;
        this.timestamp = pvpData.timestamp;
        this.delay = pvpData.delay;
        this.visible = pvpData.visible;
        this.shownTeamId = Arrays.copyOf(pvpData.shownTeamId, 2);

        return this;
    }

    boolean visible;
    public int[] shownTeamId;
    TeamInfo[] teamInfos;
    String status = "PvP is not shown";

    public synchronized Set<TeamInfo> setTeams(Set<TeamInfo> teams) {
        int idFree = -1;
        for (int i = 0; i < teamInfos.length; i++) {
            if (!teams.contains(teamInfos[i])) {
                teamInfos[i] = null;
            }
            if (teamInfos[i] == null && idFree == -1) {
                idFree = i;
            }
        }
        for (TeamInfo team : teams) {
            boolean appears = false;
            for (int i = 0; i < teamInfos.length; i++) {
                if (team == teamInfos[i]) {
                    appears = true;
                }
            }
            if (!appears) {
                if (idFree == -1) {
                    break;
                } else {
                    teamInfos[idFree] = team;
                }
            }
        }
        HashSet<TeamInfo> returnValue = new HashSet<>();
        for (TeamInfo teamInfo : teamInfos) {
            if (teamInfo != null) {
                returnValue.add(teamInfo);
            }
        }
        return returnValue;
    }

    public synchronized String setVisible() {
        if (teamInfos[0] == null || teamInfos[1] == null) {
            return "You should choose two teams";
        }
        String error = checkOverlays();
        if (error != null) {
            return error;
        }
        if (visible) {
            return "You should hide pvp first";
        }
        timestamp = System.currentTimeMillis();
        visible = true;
        for (int i = 0; i < teamInfos.length; i++) {
            shownTeamId[i] = teamInfos[i].getId();
        }
        status = "Show " + teamInfos[0].getName() + " vs " + teamInfos[1].getName();
        recache();
        return null;
    }

    public synchronized void hide() {
        visible = false;
        timestamp = System.currentTimeMillis();
        status = "PvP is not shown";
        recache();
    }

    public boolean isVisible() {
        return visible;
    }

    public synchronized String getTeam(int id) {
        if (id == 0) {
            return teamInfos[0] == null ? "First team" : teamInfos[0].getName();
        } else {
            return teamInfos[1] == null ? "Second team" : teamInfos[1].getName();
        }
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
        return "You should hide pvp view first";
    }

    public synchronized String getStatus() {
        if (visible) {
            return status + " for " + (System.currentTimeMillis() - timestamp) / 1000 + " seconds";
        } else {
            return status;
        }
    }

    private synchronized void recache() {
        Data.cache.refresh(PvPData.class);
    }

    public static class PvPDataSerializer implements JsonSerializer<PvPData> {
        @Override
        public JsonElement serialize(PvPData pvpData, Type type, JsonSerializationContext jsonSerializationContext) {
            final JsonObject object = new JsonObject();
            object.addProperty("timestamp", pvpData.timestamp);
            object.addProperty("delay", pvpData.delay);
            object.addProperty("visible", pvpData.visible);
            object.addProperty("first_team", pvpData.shownTeamId[0]);
            object.addProperty("second_team", pvpData.shownTeamId[1]);
            return object;
        }
    }

    public static class PvPDataDeserializer implements JsonDeserializer<PvPData> {
        @Override
        public PvPData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            PvPData pvpData = new PvPData();

            final JsonObject jsonObject = jsonElement.getAsJsonObject();
            pvpData.timestamp = jsonObject.get("timestamp").getAsLong();
            pvpData.delay = jsonObject.get("delay").getAsInt();
            pvpData.visible = jsonObject.get("visible").getAsBoolean();
            pvpData.shownTeamId[0] = jsonObject.get("first_team").getAsInt();
            pvpData.shownTeamId[1] = jsonObject.get("second_team").getAsInt();

            return pvpData;
        }
    }
}
