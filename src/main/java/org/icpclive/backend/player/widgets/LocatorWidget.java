package org.icpclive.backend.player.widgets;

import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.graphics.GraphicsSWT;
import org.icpclive.backend.player.widgets.locator.LocatorCamera;
import org.icpclive.backend.player.widgets.locator.LocatorConfig;
import org.icpclive.backend.player.widgets.locator.LocatorPoint;
import org.icpclive.backend.player.widgets.locator.LocatorsData;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.datapassing.LocatorData;
import org.icpclive.events.TeamInfo;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class LocatorWidget extends Widget {

    public static final int TOP_Y = 50;
    private static final int BOTTOM_Y = 968;
    private static final int QUEUE_WIDTH = 550;
    public static final int BASE_RADIUS = 700;
    private LocatorData data;
    private static final int TEAM_PANE_HEIGHT = 41;
    private static final int TEAM_PANE_WIDTH = getTeamPaneWidth(TEAM_PANE_HEIGHT);
    private static final int MARGIN = 30;
    private static final int UPDATE_TIMEOUT = 1000;

    public LocatorWidget(long updateWait) {
        super(updateWait);
        new Thread() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime > lastUpdateTime + UPDATE_TIMEOUT) {
                        updateState();
                        lastUpdateTime = currentTime;
                    }
                }
            }
        }.start();
    }

    long lastUpdateTime = 0;

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

        List<TeamInfo> teams = data.getTeams();
        List<TeamInfo> teams2 = new ArrayList<>();
        for (TeamInfo team : teams) {
            LocatorPoint p = getCoordinates(team);
            if (p.x > 0 && p.x < WIDTH && p.y > 0 && p.y < HEIGHT) {
                teams2.add(team);
            }
        }
        teams = teams2;

        if (teams.size() == 0) {
            g2.setColor(new Color(0, 0, 0, (int) (textOpacity * 120)));
            g2.fillOval(width - 100, 50, 50, 50);
            return;
        }

        LocatorPoint[] c = new LocatorPoint[teams.size()];

        for (int i = 0; i < teams.size(); i++) {
            TeamInfo teamInfo = teams.get(i);
            c[i] = getCoordinates(teamInfo);
            int r = radius[teamInfo.getId()];
            Ellipse2D ellipse = new Ellipse2D.Double(c[i].x - r, c[i].y - r, r * 2, r * 2);
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

            LocatorPoint[] cTop = new LocatorPoint[n1];
            LocatorPoint[] cBottom = new LocatorPoint[n2];
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
            int x2 = (int) c[i].x;
            int y2 = (int) c[i].y;
            int r = radius[teams.get(i).getId()];
            double d = Math.hypot(x1 - x2, y1 - y2);

            if (d > r * 1.1 && textOpacity > 0) {
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
        }
    }

    int penalty;

    private int[] placeTeamPanes(LocatorPoint[] c, int min, int max, int y) {
        int n = c.length;
        int[][] ap = new int[n][2];
        for (int i = 0; i < n; i++) {
            ap[i][0] = (int) c[i].x;
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
            penalty += ((int) c[i].y - y) * ((int) c[i].y - y);
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

    @Override
    public CachedData getCorrespondingData(Data data) {
        return data.locatorData;
    }

    // BAD CODE DOWN HERE, USE CAREFULLY

//    Point[] points = new Point[10000];
//    int[] radius = new int[10000];
    private LocatorPoint[] points;
    private int[] radius;

    private LocatorPoint getCoordinates(TeamInfo teamInfo) {
        if (points == null || teamInfo == null) {
            System.out.println("!!!");
        }
        return points[teamInfo.getId()];
    }

    private static final double ANGLE = 1.1;
//    double pan = 0, tilt = 0, angle = ANGLE;
//    LocatorConfig config = new LocatorConfig(0, 0, ANGLE);
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;

    private LocatorCamera getCamera() {
        return LocatorsData.locatorCameras.get(data.getCameraID());
    }

    private void updateState() {
        try {
            LocatorCamera camera = getCamera();
            String response = sendGet("http://" + camera.hostName + "/axis-cgi/com/ptz.cgi?query=position,limits&camera=1&html=no&timestamp=" + getUTCTime());
            camera.update();
            LocatorConfig config = parseCameraConfiguration(response);
            int n = camera.coordinates.length;
            LocatorPoint[] newPoints = new LocatorPoint[n];
            int[] newRadius = new int[n];
            for (int i = 0; i < n; i++) {
                LocatorPoint p = camera.coordinates[i];
                p = p.rotateY(config.pan);
                p = p.rotateX(-config.tilt);
                int R = (int) (BASE_RADIUS / Math.abs(p.z) * ANGLE / config.angle) + 2;
                p = p.multiply(1 / p.z);
                p = p.multiply(WIDTH / config.angle);
                p = p.move(new LocatorPoint(WIDTH / 2, HEIGHT / 2, 0));
                newPoints[i] = p;
                newRadius[i] = R;
            }
            points = newPoints;
            radius = newRadius;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String sendGet(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    public static String getUTCTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ss'Z'");
        sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        return sdf.format(cal.getTime());
    }

    LocatorConfig parseCameraConfiguration(String s) {
        s = s.trim();
        int l = 0;
        int r = 0;
        double newPan = Double.NaN;
        double newTilt = Double.NaN;
        double newAngle = Double.NaN;
        while (r < s.length()) {
            l = r;
            r = l + 1;
            while (r < s.length() && Character.isAlphabetic(s.charAt(r))) {
                r++;
            }
            String key = s.substring(l, r);
            l = r + 1;
            r = l + 1;
            while (r < s.length() && !Character.isAlphabetic(s.charAt(r))) {
                r++;
            }
            try {
                double value = Double.parseDouble(s.substring(l, r));
                switch (key) {
                    case "pan":
                        newPan = value * Math.PI / 180;
                        break;
                    case "tilt":
                        newTilt = value * Math.PI / 180;
                        break;
                    case "zoom":
                        double maxmag = 35;
                        double mag = 1 + (maxmag - 1) * value / 9999;
                        newAngle = ANGLE / mag;
                        break;
                }
            } catch (Exception e) {
            }
        }
        if (Double.isNaN(newPan) || Double.isNaN(newTilt) || Double.isNaN(newAngle)) {
            throw new AssertionError();
        }
        return new LocatorConfig(newPan, newTilt, newAngle);
    }

}
