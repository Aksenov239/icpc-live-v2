package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.backend.player.TickPlayer;
import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;

import java.awt.*;
import java.util.HashMap;
import java.util.Properties;

/**
 * @author: pashka
 */
public abstract class TeamWidget extends VideoWidget implements Scalable {
    public static String[] types;
    public static HashMap<String, String> urlTemplates;

    static {
        Properties properties = new Properties();
        try {
            properties.load(TeamWidget.class.getClassLoader().getResourceAsStream("mainscreen.properties"));
            types = properties.getProperty("info.types", "screen;camera;info").split(";");
            urlTemplates = new HashMap<>();
            for (int i = 0; i < types.length; i++) {
                String url = properties.getProperty("info." + types[i], "");
                urlTemplates.put(types[i], url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int xVideo;
    private int yVideo;
    private int widthVideo;
    private int heightVideo;
    private int width;
    private int height;
    public TeamWidget(int x, int y, int width, int height, double aspectRatio, int sleepTime) {
        super(x, y, (int)(height * aspectRatio), height, sleepTime, 0);
        this.width = width;
        this.height = height;
        this.widthVideo = (int)(height * aspectRatio);
        this.heightVideo = height;
        this.xVideo = x + width - widthVideo;
        this.yVideo = y;
    }

    protected abstract int getTeamId();

    protected Font FONT1 = Font.decode("Open Sans Italic " + 40);

    protected int X = 20;
    protected int Y = 20;
    protected int GAP_Y = 5;
    protected int GAP_X = 5;
    protected int PR_WIDTH = 50;
    protected int RUN_WIDTH = 80;
    protected int RUN_SMALL_WIDTH = 20;
    protected int HEIGHT = 45;
    protected double STAR_SIZE = 5;
    Font FONT2 = Font.decode("Open Sans Italic " + 30);

    private static final Color GREEN = new Color(27, 155, 82);//Color.decode("0x33ff00");
    private static final Color RED = Color.decode("0xaa0000");
    private static final Color YELLOW = new Color(250, 200, 82);//Color.decode("0x33ff00");
    protected TeamInfo team;

    public static final int PERIOD = 500;

    private double getTimeOpacity() {
        long time = System.currentTimeMillis();
        long second = time / PERIOD;
        long percent = time % PERIOD;

        double v = percent * 1.0 / PERIOD;
        return (second % 2 == 0 ? v : 1 - v) / 2 + 0.5;
    }

    private void drawReplay(Graphics2D g, int x, int y, int width, int height) {
        g.setFont(FONT2);
        drawTextInRect(g, "R", (int) (x + width * 0.95), (int) (y + height * 0.17), -1, HEIGHT, POSITION_CENTER, RED, Color.WHITE, getTimeOpacity());
    }

    @Override
    public void paintImpl(Graphics2D g, int width, int height) {
        if (!isVisible())
            return;

        if (team != null && URL.get() != null) {
            g.drawImage(image.get(), xVideo, yVideo, null);
        }
        if (inChange.get() || team == null) {
            team = Preparation.eventsLoader.getContestData().getParticipant(getTeamId());
            inChange.set(false);
        }
        if (URL.get() == null || URL.get().contains("info")) {
            return;
        }
        g.setColor(Color.WHITE);
        g.setColor(new Color(0, 0, 30));
        g.fillRect(x, y, this.width - widthVideo, height);
//        team = Preparation.eventsLoader.getContestData().getParticipant(getTeamId());
//        if (team == null) return;
        g.setFont(FONT1);
        int dx = (int) (this.width * 0.52);
        int dy = (int) (this.height * 0.9);
        drawTeamPane(g, team, x + dx, y + dy, (int) (this.width * 0.45), 1);

        g.setFont(FONT2);
        for (int i = 0; i < team.getRuns().length; i++) {
            int y = Y + (HEIGHT + GAP_Y) * i;
            RunInfo[] runs = team.getRuns()[i].toArray(new RunInfo[0]);

            Color problemColor = MAIN_COLOR;
            for (int j = 0; j < runs.length; j++) {
                RunInfo run = runs[j];
                if (run.getResult().equals("AC")) {
                    problemColor = GREEN;
                    break;
                }
                if (run.getResult().equals("")) {
                    problemColor = YELLOW;
                } else {
                    problemColor = RED;
                }
            }

            drawTextInRect(g, "" + (char) ('A' + i), this.x + X, this.y + y,
                    PR_WIDTH, HEIGHT, POSITION_CENTER, problemColor, Color.WHITE, 1);

            int x = X + PR_WIDTH + GAP_X;
            for (int j = 0; j < runs.length; j++) {
                RunInfo run = runs[j];
                Color color = run.getResult().equals("AC") ? GREEN : run.getResult().equals("") ? YELLOW : RED;
                if (j == runs.length - 1) {
                    drawTextInRect(g, format(run.getTime()), this.x + x, this.y + y,
                            RUN_WIDTH, HEIGHT, POSITION_CENTER, color, Color.WHITE, 1);
                    //System.err.println(Arrays.toString(Preparation.eventsLoader.getContestData().firstTimeSolved()));
                    if (run.getResult().equals("AC") && run.getTime() == Preparation.eventsLoader.getContestData().firstTimeSolved()[run.getProblemNumber()]) {
                        drawStar(g, this.x + x + RUN_WIDTH, (int) (this.y + y + STAR_SIZE / 2));
                    }
                    x += RUN_WIDTH + GAP_X;
                } else if (run.getTime() != runs[j + 1].getTime()) {
                    drawTextInRect(g, "", this.x + x, this.y + y,
                            RUN_SMALL_WIDTH, HEIGHT, POSITION_CENTER, color, Color.WHITE, 1);
                    x += RUN_SMALL_WIDTH + GAP_X;
                }

            }
        }
    }

    private void drawStar(Graphics2D g, int x, int y) {
        g.setColor(Color.YELLOW);
        int[] xx = new int[10];
        int[] yy = new int[10];
        double[] d = {STAR_SIZE, STAR_SIZE * 2};
        for (int i = 0; i < 10; i++) {
            xx[i] = (int) (x + Math.sin(Math.PI * i / 5) * d[i % 2]);
            yy[i] = (int) (y + Math.cos(Math.PI * i / 5) * d[i % 2]);
        }
        g.fillPolygon(new Polygon(xx, yy, 10));
    }

    private String format(double time) {
        int s = (int) time;
        int m = s / 60;
        s %= 60;
        int h = m / 60;
        m %= 60;
        return String.format("%d:%02d", h, m);
    }
}
