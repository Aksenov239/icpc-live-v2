package org.icpclive.backend.player.widgets;

import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.graphics.GraphicsSWT;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.datapassing.LocatorData;
import org.icpclive.events.TeamInfo;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.List;

public class LocatorWidget extends Widget {

    public static final int TOP_Y = 50;
    private static final int BOTTOM_Y = 968;
    private static final int QUEUE_WIDTH = 550;
    private LocatorData data;
    private static final int TEAM_PANE_HEIGHT = 41;
    private static final int TEAM_PANE_WIDTH = getTeamPaneWidth(TEAM_PANE_HEIGHT);
    private static final int MARGIN = 30;

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

        Graphics2D g2 = ((GraphicsSWT) graphics).g;

        Polygon polygon = new Polygon(new int[]{0, width, width, 0}, new int[]{0, 0, height, height}, 4);
        Area area = new Area(polygon);

//        for (TeamInfo team : data.getTeams()) {
//            if (team == null) continue;
//        }
//
        int[][] c = new int[data.getTeams().size()][2];

        List<TeamInfo> teams = data.getTeams();
        for (int i = 0; i < teams.size(); i++) {
            TeamInfo teamInfo = teams.get(i);
            c[i] = getCoordinates(teamInfo);
            Ellipse2D ellipse = new Ellipse2D.Double(c[i][0] - c[i][2], c[i][1] - c[i][2], c[i][2] * 2, c[i][2] * 2);
            area.subtract(new Area(ellipse));
        }

        g2.setColor(new Color(0, 0, 0, (int) (textOpacity * 120)));
        g2.fill(area);

        int[] x = new int[c.length];
        int pos = 0;
        int minPenalty = Integer.MAX_VALUE;

        for (int m = 0; m < (1 << c.length); m++) {
            int n1 = Integer.bitCount(m);
            int n2 = c.length - n1;

            if (n1 > 3 || n2 > 2) continue;

            int[][] cTop = new int[n1][];
            int[][] cBottom = new int[n2][];
            int k1 = 0;
            int k2 = 0;
            for (int i = 0; i < c.length; i++) {
                if (((m >> i) & 1) == 1) {
                    cTop[k1++] = c[i];
                } else {
                    cBottom[k2++] = c[i];
                }
            }
            int s = 0;
            int[] xTop = placeTeamPanes(
                    cTop,
                    TEAM_PANE_WIDTH / 2 + MARGIN,
                    width - TEAM_PANE_WIDTH / 2 - MARGIN,
                    TOP_Y);
            s += penalty;
            int[] xBottom = placeTeamPanes(
                    cBottom,
                    TEAM_PANE_WIDTH / 2 + MARGIN + QUEUE_WIDTH,
                    width - TEAM_PANE_WIDTH / 2 - MARGIN,
                    BOTTOM_Y);
            s += penalty;

            if (s < minPenalty) {
                minPenalty = s;
                pos = m;
                k1 = k2 = 0;
                for (int i = 0; i < c.length; i++) {
                    if (((m >> i) & 1) == 1) {
                        x[i] = xTop[k1++];
                    } else {
                        x[i] = xBottom[k2++];
                    }
                }
            }
        }

        for (int i = 0; i < teams.size(); i++) {
            int side = (pos >> i) & 1;

            drawTeamPane(graphics, teams.get(i),
                    x[i] - TEAM_PANE_WIDTH / 2,
                    side == 1 ? TOP_Y : BOTTOM_Y,
                    TEAM_PANE_HEIGHT,
                    visibilityState);

            int x1 = x[i];
            int y1 = side == 1 ? TOP_Y + TEAM_PANE_HEIGHT + 5 : BOTTOM_Y - 5;
            int x2 = c[i][0];
            int y2 = c[i][1];
            int r = c[i][2];
            double d = Math.hypot(x1 - x2, y1 - y2);

            if (d > r * 1.1) {
                double d1 = (d - r) * (0.1 - textOpacity * 0.1) / d;
                double d2 = (d - r) * (0.1 + textOpacity * 0.9) / d;

                int xx1 = (int) (x1 + (x2 - x1) * d1);
                int yy1 = (int) (y1 + (y2 - y1) * d1);
                int xx2 = (int) (x1 + (x2 - x1) * d2);
                int yy2 = (int) (y1 + (y2 - y1) * d2);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3));
                g2.drawLine(xx1, yy1, xx2, yy2);
            }
//
//            int d = c[i][1] - 100 - TEAM_PANE_HEIGHT - c[i][2];
//            g2.fillRect(c[i][0] - 1, 100 + TEAM_PANE_HEIGHT + (int) (d * (1 - textOpacity) / 10), 3, (int) (d * textOpacity));
        }
    }

    int penalty;

    private int[] placeTeamPanes(int[][] c, int min, int max, int y) {
        int n = c.length;
        int[][] ap = new int[n][2];
        for (int i = 0; i < n; i++) {
            ap[i][0] = c[i][0];
            ap[i][1] = i;
        }
        Arrays.sort(ap, (o1, o2) -> Integer.compare(o1[0], o2[0]));
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = ap[i][0];
        int[] b = new int[n];
        for (int i = 0; i < n; i++) {
            b[i] = min + (TEAM_PANE_WIDTH + MARGIN) * i;
        }
        for (int i = n - 1; i >= 0; i--) {
            while (true) {
                int[] bb = b.clone();
                bb[i] += 10;
                for (int j = i + 1; j < n; j++) {
                    bb[j] = Math.max(bb[j], bb[j - 1] + TEAM_PANE_WIDTH + MARGIN);
                }
                if (bb[n - 1] <= max && calc(a, bb) < calc(a, b)) {
                    b = bb;
                } else {
                    break;
                }
            }
        }
        penalty = calc(a, b);
        for (int i = 0; i < n; i++) {
            penalty += (c[i][1] - y) * (c[i][1] - y);
        }
        int[] res = new int[n];
        for (int i = 0; i < n; i++) {
            res[ap[i][1]] = b[i];
        }
        return res;
    }

    private int calc(int[] a, int[] b) {
        int s = 0;
        for (int i = 0; i < a.length; i++) {
            s += (a[i] - b[i]) * (a[i] - b[i]);
        }
        return s;
    }

    Map<Integer, int[]> coordinates = new HashMap<>();

    private int[] getCoordinates(TeamInfo teamInfo) {
        int id = teamInfo.getId();
        int[] res = coordinates.get(id);
        if (res == null) {
            res = new int[3];
            res[0] = 50 + (id % 15) * 110;
            res[1] = 250 + (id / 15) * 80;
            res[2] = res[1] / 5;
            coordinates.put(id, res);
        }
        return res;
    }

    @Override
    public CachedData getCorrespondingData(Data data) {
        return data.pvpData;
    }

}
