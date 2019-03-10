package org.icpclive.backend.player.widgets;

import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.datapassing.PvPData;
import org.icpclive.events.EventsLoader;

public class PvPWidget extends Widget {
    public PvPWidget() {

    }

    @Override
    public void updateImpl(Data data) {
        PvPData pvpData = data.pvpData;
        System.err.println(pvpData.timestamp + " " + pvpData.delay + " " +
                pvpData.isVisible());
        System.err.println(EventsLoader.getInstance().getContestData().getParticipant(pvpData.shownTeamId[0]));
        System.err.println(EventsLoader.getInstance().getContestData().getParticipant(pvpData.shownTeamId[1]));
    }

    @Override
    public void paintImpl(AbstractGraphics g, int width, int height) {
        update();
    }

    @Override
    public CachedData getCorrespondingData(Data data) {
        return data.pvpData;
    }
}
