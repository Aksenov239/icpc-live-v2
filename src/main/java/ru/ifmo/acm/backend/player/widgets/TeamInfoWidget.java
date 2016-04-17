package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.PCMS.PCMSTeamInfo;
import ru.ifmo.acm.events.TeamInfo;
import ru.ifmo.acm.events.WF.WFTeamInfo;

import java.awt.*;

/**
 * @author: pashka
 */
public class TeamInfoWidget extends TeamWidget {


    public TeamInfoWidget(long updateWait, int width, int height, double aspectRatio, int sleepTime) {
        super(0, 0, width, height, aspectRatio, sleepTime);

        this.updateWait = updateWait;
        teamId = -1;
    }

    private long updateWait;
    private long lastUpdate;
    private String currentInfoType;

    public void updateImpl(Data data) {
        //System.err.println(data.teamData.isTeamVisible);

        if (!data.teamData.isVisible) {
            setVisible(false);
            teamId = -1;
            stop();
        } else {
            setVisible(true);
            //System.err.println(data.teamData.teamId + " " + teamId + " " + ready.get());
            if ((data.teamData.getTeamId() != teamId || !data.teamData.infoType.equals(currentInfoType)) && ready.get()) {
                //System.err.println("Change to " + urlTemplates.get(data.teamData.infoType) + " " + data.teamData.teamId);
                TeamInfo team = Preparation.eventsLoader.getContestData().getParticipant(data.teamData.getTeamId());
                if (team == null) {
                    setVisible(false);
                    return;
                }

                change(team, data.teamData.infoType);
                teamId = data.teamData.getTeamId();
                currentInfoType = data.teamData.infoType;
            }
        }
    }

    public void paintImpl(Graphics2D g, int width, int height) {
        update();
        super.paintImpl(g, width, height);
    }

    protected int getTeamId() {
        return teamId;
    }
}
