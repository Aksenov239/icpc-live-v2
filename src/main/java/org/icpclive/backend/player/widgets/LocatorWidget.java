package org.icpclive.backend.player.widgets;

import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.graphics.GraphicsSWT;
import org.icpclive.backend.player.PlayerInImage;
import org.icpclive.backend.player.urls.TeamUrls;
import org.icpclive.backend.player.widgets.stylesheets.BigStandingsStylesheet;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.backend.player.widgets.stylesheets.QueueStylesheet;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.datapassing.LocatorData;
import org.icpclive.datapassing.PvPData;
import org.icpclive.events.ContestInfo;
import org.icpclive.events.EventsLoader;
import org.icpclive.events.RunInfo;
import org.icpclive.events.TeamInfo;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

public class LocatorWidget extends Widget {

    private LocatorData data;

    public LocatorWidget(long updateWait) {
        super(updateWait);
    }

    @Override
    public void updateImpl(Data data) {
        this.data = data.locatorData;
        setVisible(data.locatorData.isVisible());
    }

    @Override
    public void paintImpl(AbstractGraphics g, int width, int height) {
        super.paintImpl(g, width, height);
        update();
        if (!isVisible() && visibilityState == 0) {
            return;
        }
//        System.out.println("!!!");

        double opacity = getOpacity(visibilityState) * 0.5;
        if (opacity == 0) return;
        graphics.setFillColor(Color.black, opacity);
        Graphics2D g2 = ((GraphicsSWT) graphics).g;
        g2.setColor(new Color(0, 0, 0, (int) (opacity * 255)));

        Polygon polygon = new Polygon(new int[]{0, width, width, 0}, new int[]{0, 0, height, height}, 4);
        Area area = new Area(polygon);

//        for (TeamInfo team : data.getTeams()) {
//            if (team == null) continue;
//        }
//
        int[] x = new int[data.getTeams().size()];
        int[] y = new int[data.getTeams().size()];
        int r = 100;

        List<TeamInfo> teams = data.getTeams();
        for (int i = 0; i < teams.size(); i++) {
            TeamInfo teamInfo = teams.get(i);
            x[i] = 500 + 600 * i;
            y[i] = 500 + 100 * i;
            Ellipse2D ellipse = new Ellipse2D.Double(x[i] - r, y[i] - r, 2 * r, 2 * r);
            area.subtract(new Area(ellipse));
        }
        g2.fill(area);

        int teamPaneHeight = 41;

        for (int i = 0; i < teams.size(); i++) {
            int xx = x[i] - getTeamPaneWidth(teamPaneHeight) / 2;
            xx = Math.min(xx, width - getTeamPaneWidth(teamPaneHeight) - 30);
            xx = Math.max(xx, 30);
            drawTeamPane(graphics, teams.get(i),
                    xx,
                    100,
                    teamPaneHeight,
                    visibilityState);
            g2.setColor(Color.WHITE);
            int d = y[i] - 100 - teamPaneHeight - r;
            g2.fillRect(x[i] - 1, 100 + teamPaneHeight + (int)(d * (1 - textOpacity) / 10), 3, (int) (d * textOpacity));
        }



    }

    @Override
    public CachedData getCorrespondingData(Data data) {
        return data.pvpData;
    }

}
