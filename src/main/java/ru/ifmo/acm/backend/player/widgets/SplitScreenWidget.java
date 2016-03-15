package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.datapassing.Data;

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
    final boolean[] showOrStandings = new boolean[4];

    public void initialization() {
        Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream("splitscreen.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int time = Integer.parseInt(properties.getProperty("split.changetime"));
        String[] showSetup = properties.getProperty("split.setup").split(",");

        new Timer().scheduleAtFixedRate(new TimerTask() {
            public void run() {
                // TODO: Automatic update
            }
        }, 0L, time);
    }

    public SplitScreenWidget(long updateWait, int width, int height, double aspectRatio, int sleepTime) {
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
            showOrStandings[i] = true;
        }
        initialization();
    }

    @Override
    protected void updateImpl(Data data) {
        super.updateImpl(data);
        // TODO: Update using SplitScreenData.
        lastUpdate = System.currentTimeMillis();
    }

    @Override
    public void paintImpl(Graphics2D g, int width, int height) {
        update();
        for (int i = 0; i < teamInfoWidgets.length; i++) {
            if (showOrStandings[i]) {
                teamInfoWidgets[i].paintImpl(g, width, height);
            } else {
                // TODO: paint Standings
            }
        }
    }

}
