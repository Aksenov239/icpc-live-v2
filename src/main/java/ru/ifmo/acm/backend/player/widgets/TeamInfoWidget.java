package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.Data;

import java.awt.*;

/**
 * @author: pashka
 */
public class TeamInfoWidget extends TeamWidget {
    private int teamId;

    public TeamInfoWidget(long updateWait, int width, int height, int sleepTime) {
        super(0, 0, width, height, sleepTime);

        this.updateWait = updateWait;

        teamId = -1;
    }

    private long updateWait;
    private long lastUpdate;

    public void update() {
        if (lastUpdate + updateWait < System.currentTimeMillis()) {
            Data data = Preparation.dataLoader.getDataBackend();
            if (data == null) {
                return;
            }

            //System.err.println(data.teamData.isTeamVisible);

            if (!data.teamData.isTeamVisible) {
                setVisible(false);
                stop();
            } else {
                setVisible(true);
                if (!isVisible() || (data.teamData.teamId != teamId && !inChange.get())) {
                    //System.err.println("Change to " + urlTemplates.get(data.teamData.infoType) + " " + data.teamData.teamId);
                    int hall = data.teamData.teamId / 100;
                    int place = data.teamData.teamId % 100;
                    //change(String.format(urlTemplates.get(data.teamData.infoType), hall, place));
                    teamId = data.teamData.teamId;
                }
            }


            lastUpdate = System.currentTimeMillis();
        }
    }

    public void paint(Graphics2D g, int width, int height) {
        update();
        super.paint(g, width, height);
    }

    protected int getTeamId() {
        return teamId;
    }
}
