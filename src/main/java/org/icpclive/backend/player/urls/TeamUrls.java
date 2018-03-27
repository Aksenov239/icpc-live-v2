package org.icpclive.backend.player.urls;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.Config;
import org.icpclive.events.PCMS.PCMSTeamInfo;
import org.icpclive.events.RunInfo;
import org.icpclive.events.TeamInfo;
import org.icpclive.events.WF.WFTeamInfo;

import java.util.HashMap;
import java.util.Properties;
import java.util.Arrays;

public class TeamUrls {
    private static final Logger log = LogManager.getLogger(TeamUrls.class);

    public static String[] types;
    public static HashMap<String, String> urlTemplates;

    private TeamUrls() {} // utility only, do not create

    static {
        try {
            Properties properties = Config.loadProperties("mainscreen");
            TeamUrls.types = properties.getProperty("info.types", "screen;camera;info").split(";");
            TeamUrls.types = Arrays.copyOf(TeamUrls.types, TeamUrls.types.length + 1);
            TeamUrls.types[TeamUrls.types.length - 1] = "";
            TeamUrls.urlTemplates = new HashMap<>();
            for (int i = 0; i < TeamUrls.types.length; i++) {
                String url = properties.getProperty("info." + TeamUrls.types[i], "");
                TeamUrls.urlTemplates.put(TeamUrls.types[i], url);
            }
            if (properties.get("info.record") != null) {
                TeamUrls.urlTemplates.put("record", properties.getProperty("info.record"));
            }
        } catch (Exception e) {
            log.error("error", e);
        }
    }

    public static String getUrl(TeamInfo team, String infoType) {
        if (team instanceof PCMSTeamInfo) {
            int aliasId = Integer.parseInt(team.getAlias().substring(1));
            int hall = aliasId / 100;
            int place = aliasId % 100;
            log.info("addView " + hall + " " + place);
            return String.format(urlTemplates.get(infoType), hall, place);
        } else if (team instanceof WFTeamInfo) {
            log.info("addView " + (team.getId() + 1) + " " + infoType);
            return String.format(urlTemplates.get(infoType), team.getId() + 1);
        }
        return null;
    }

    public static String getUrl(int teamId, String contest, String infoType) {
        if (contest.equals("PCMS")) {
            int hall = teamId / 100;
            int place = teamId % 100;
            return String.format(urlTemplates.get(infoType), hall, place);
        } else if (contest.equals("WF")) {
            return String.format(urlTemplates.get(infoType), teamId);
        }
        return null;
    }

    public static String getUrl(RunInfo run) {
        return String.format(urlTemplates.get("record"), run.getId());
    }
}
