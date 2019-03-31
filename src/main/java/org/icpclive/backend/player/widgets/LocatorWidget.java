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
import java.io.BufferedReader;
import java.io.File;
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
    private LocatorData data;
    private static final int TEAM_PANE_HEIGHT = 41;
    private static final int TEAM_PANE_WIDTH = getTeamPaneWidth(TEAM_PANE_HEIGHT);
    private static final int MARGIN = 30;
    private static final int UPDATE_TIMEOUT = 1000;

    public LocatorWidget(long updateWait) {
        super(updateWait);
    }

    long lastUpdateTime = 0;

    @Override
    public void updateImpl(Data data) {
        this.data = data.locatorData;
        setVisible(data.locatorData.isVisible());
        long currentTime = System.currentTimeMillis();
        if (currentTime > lastUpdateTime + UPDATE_TIMEOUT) {
            updateState();
            lastUpdateTime = currentTime;
        }
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

        Point[] c = new Point[data.getTeams().size()];

        List<TeamInfo> teams = data.getTeams();
        for (int i = 0; i < teams.size(); i++) {
            TeamInfo teamInfo = teams.get(i);
            c[i] = getCoordinates(teamInfo);
            int r = radius[i];
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

            Point[] cTop = new Point[n1];
            Point[] cBottom = new Point[n2];
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
            int x2 = (int)c[i].x;
            int y2 = (int)c[i].y;
            int r = radius[i];
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
        }
    }

    int penalty;

    private int[] placeTeamPanes(Point[] c, int min, int max, int y) {
        int n = c.length;
        int[][] ap = new int[n][2];
        for (int i = 0; i < n; i++) {
            ap[i][0] = (int)c[i].x;
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
            penalty += ((int)c[i].y - y) * ((int)c[i].y - y);
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

    Point[] points = new Point[10000];
    int[] radius = new int[10000];

    private Point getCoordinates(TeamInfo teamInfo) {
        return points[teamInfo.getId()];
    }

    private static final double ANGLE = 1.01;
    public static final String CAMERA_IP = "10.250.25.111";
    double pan = 0, tilt = 0, angle = ANGLE;
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;

    private synchronized void updateState() {
        try {
            parse(sendGet("http://" + CAMERA_IP + "/axis-cgi/com/ptz.cgi?query=position,limits&camera=1&html=no&timestamp=" + getUTCTime()));
            Scanner in = new Scanner(new File("coordinates.txt"));
            int n = in.nextInt();             
            for (int i = 1; i <= n; i++) {
                Point p = new Point(in.nextInt(), in.nextInt(), in.nextInt());
                p = p.rotateY(pan);
                p = p.rotateX(-tilt);
                int R = (int) (20 / p.z * ANGLE / angle) + 2;
                p = p.multiply(1 / p.z);
                p = p.multiply(WIDTH / angle);
                p = p.move(new Point(WIDTH / 2, HEIGHT / 2, 0));
                points[i] = p;
                radius[i] = R;
            }
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
        StringBuffer response = new StringBuffer();
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
        String utcTime = sdf.format(cal.getTime());
        return utcTime;
    }

    void parse(String s) {
        s = s.trim();
        int l = 0;
        int r = 0;
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
                        pan = value * Math.PI / 180;
                        break;
                    case "tilt":
                        tilt = value * Math.PI / 180;
                        break;
                    case "zoom":
                        double maxmag = 35;
                        double mag = 1 + (maxmag - 1) * value / 9999;
                        angle = ANGLE / mag;
                        break;
                }
            } catch (Exception e) {
            }
        }
    }

    static class Point {
        double x, y, z;

        public Point(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        Point move(Point d) {
            return new Point(x + d.x, y + d.y, z + d.z);
        }

        Point multiply(double d) {
            return new Point(x * d, y * d, z * d);
        }

        Point rotateZ(double a) {
            return new Point(x * Math.cos(a) - y * Math.sin(a),
                    x * Math.sin(a) + y * Math.cos(a),
                    z);
        }

        Point rotateY(double a) {
            return new Point(x * Math.cos(a) - z * Math.sin(a),
                    y,
                    x * Math.sin(a) + z * Math.cos(a));
        }

        Point rotateX(double a) {
            return new Point(x,
                    y * Math.cos(a) - z * Math.sin(a),
                    y * Math.sin(a) + z * Math.cos(a));
        }
    }

}
