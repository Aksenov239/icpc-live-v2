package org.icpclive.backend.player.widgets;

import org.icpclive.backend.Preparation;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.events.TeamInfo;
import org.icpclive.backend.graphics.Graphics;

/**
 * @author: pashka
 */
public class TeamInfoWidget extends TeamWidget {

    public TeamInfoWidget(int baseX, int baseY, long updateWait, int width, int height, double aspectRatio, int sleepTime) {
        super(baseX, baseY, width, height, aspectRatio, sleepTime);

        this.updateWait = updateWait;
        teamId = -1;
    }

    private long updateWait;
    private long lastUpdate;
    private String currentInfoType;

    public void updateImpl(Data data) {
        //log.info(data.teamData.isTeamVisible);

        if (!data.teamData.isVisible || "".equals(data.teamData.infoType)) {
            setVisible(false);
            teamId = -1;
            mainVideo.stop();
        } else {
            setVisible(true);
            //log.info(data.teamData.teamId + " " + teamId + " " + ready.get());
            if ((data.teamData.getTeamId() != teamId || !data.teamData.infoType.equals(currentInfoType)) && mainVideo.readyToShow()) {
                //log.info("Change to " + urlTemplates.get(data.teamData.infoType) + " " + data.teamData.teamId);
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

    public void paintImpl(Graphics g, int width, int height) {
        update();
        super.paintImpl(g, width, height);
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return data.teamData;
    }

    protected int getTeamId() {
        return teamId;
    }
}
