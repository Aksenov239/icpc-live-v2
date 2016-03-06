package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.PCMS.PCMSTeamInfo;

import java.awt.*;

/**
 * @author: pashka
 */
public class TeamInfoWidget extends TeamWidget {
    private int teamId;

    public TeamInfoWidget(long updateWait, int width, int height, double aspectRatio, int sleepTime) {
        super(0, 0, width, height, aspectRatio, sleepTime);

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

            if (!data.teamData.isVisible) {
                setVisible(false);
                teamId = -1;
                stop();
            } else {
                setVisible(true);
                //System.err.println(data.teamData.teamId + " " + teamId + " " + ready.get());
                if (data.teamData.getTeamId() != teamId && ready.get()) {
                    //System.err.println("Change to " + urlTemplates.get(data.teamData.infoType) + " " + data.teamData.teamId);
                    PCMSTeamInfo team = (PCMSTeamInfo) Preparation.eventsLoader.getContestData().getParticipant(data.teamData.getTeamId());
                    if (team == null) {
                        setVisible(false);
                        return;
                    }
                    int aliasId = Integer.parseInt(team.getAlias().substring(1));
                    int hall = aliasId / 100;
                    int place = aliasId % 100;
                    System.err.println("change " + hall + " " + place);
                    change(String.format(urlTemplates.get(data.teamData.infoType), hall, place));
                    teamId = data.teamData.getTeamId();
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
