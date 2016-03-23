package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.EventsLoader;

import java.awt.*;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author: pashka
 */
public class SplitScreenWidget extends Widget {
    final TeamWidget[] teamInfoWidgets = new TeamWidget[4];
    final boolean[] automatic = new boolean[4];
    private long switchTime;
    private long lastSwitch;
    private String defaultType;

    public void initialization() {
        Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream("splitscreen.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        switchTime = Integer.parseInt(properties.getProperty("switchtime"));
        String[] showSetup = properties.getProperty("setup").split(",");
        defaultType = properties.getProperty("default.type", "screen");
        for (int i = 0; i < 4; i++) {
            int teamId = Integer.parseInt(showSetup[i]);
            teamInfoWidgets[i].setTeamId(teamId);
            teamInfoWidgets[i].change(
                    TeamWidget.getUrl(Preparation.eventsLoader.getContestData().getParticipant(teamId), defaultType));
        }
        lastSwitch = System.currentTimeMillis() + switchTime;
    }

    public SplitScreenWidget(long updateWait, int width, int height, double aspectRatio, int sleepTime, long switchTime) {
        super(updateWait);
        for (int i = 0; i < 4; i++) {
            teamInfoWidgets[i] = new TeamWidget(
                    (width / 2) * (i & 1),
                    (height / 2) * i,
                    width / 2,
                    height / 2,
                    aspectRatio,
                    sleepTime
            );
            automatic[i] = true;
        }
        initialization();
        lastSwitch = System.currentTimeMillis();
        this.switchTime = switchTime;
    }

    @Override
    protected void updateImpl(Data data) {
        for (int i = 0; i < 4; i++) {
            automatic[i] = data.splitScreenData.isAutomatic[i];
            if (automatic[i])
                continue;
            teamInfoWidgets[i].setTeamId(data.splitScreenData.getTeamId(i));

        }

    }

    @Override
    public void paintImpl(Graphics2D g, int width, int height) {
        update();
        for (int i = 0; i < teamInfoWidgets.length; i++) {
            if (automatic[i]) {
                teamInfoWidgets[i].paintImpl(g, width, height);
            } else {
                // TODO: paint Standings
            }
        }
    }

}
