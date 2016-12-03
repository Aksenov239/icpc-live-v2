package ru.ifmo.acm.backend.player.urls;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.acm.events.PCMS.PCMSTeamInfo;
import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;
import ru.ifmo.acm.events.WF.WFTeamInfo;

import java.util.HashMap;
import java.util.Properties;
import java.util.Arrays;

public class TeamUrls {
    private static final Logger log = LogManager.getLogger(TeamUrls.class);

    public static String[] types;
    public static HashMap<String, String> urlTemplates;

    private TeamUrls() {} // utility only, do not create

    static {
        Properties properties = new Properties();
        try {
            properties.load(TeamUrls.class.getClassLoader().getResourceAsStream("mainscreen.properties"));
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
            log.info("change " + hall + " " + place);
            return String.format(urlTemplates.get(infoType), hall, place);
        } else if (team instanceof WFTeamInfo) {
            log.info("change " + (team.getId() + 1) + " " + infoType);
            return String.format(urlTemplates.get(infoType), team.getId() + 1);
        }
        return null;
    }

    public static String getUrl(RunInfo run) {
        return String.format(urlTemplates.get("record"), run.getId());
    }
}
