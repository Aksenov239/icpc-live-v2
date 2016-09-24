package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.backend.player.urls.TeamUrls;
import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;
import ru.ifmo.acm.backend.graphics.Graphics;
import java.awt.*;
import ru.ifmo.acm.backend.player.widgets.stylesheets.*;

/**
 * @author: pashka
 */
public class TeamWidget extends VideoWidget {

    protected int teamId;

    private int xVideo;
    private int yVideo;
    private int widthVideo;
    private int heightVideo;
    private int width;
    private int height;

    private boolean isFull = false;

    VideoWidget smallVideo = null;

    public TeamWidget(int x, int y, int width, int height, double aspectRatio, int sleepTime) {
        super(x, y, (int) (height * aspectRatio), height, sleepTime, 0);
        this.width = width;
        this.height = height;
        this.widthVideo = (int) (height * aspectRatio);
        this.heightVideo = height;
        this.xVideo = x + width - widthVideo;
        this.yVideo = y;
        teamId = -1;

        int xSmallVideo = x + (int) (width * 0.7);
        int ySmallVideo = y + (int) (height * 0.6);
        int hSmallVideo = (int) (height * 0.25);
        int wSmallVideo = (int) (hSmallVideo * aspectRatio);
        smallVideo = new VideoWidget(xSmallVideo, ySmallVideo, wSmallVideo, hSmallVideo, sleepTime, 0);
    }

    public TeamWidget(int x, int y, int width, int height, double aspectRatio, int sleepTime, boolean full) {
        this(x, y, width, height, aspectRatio, sleepTime);

        this.isFull = full;
        if (full) {
            int problems = Preparation.eventsLoader.getContestData().getProblemsNumber();
            X = 10;
            Y = 17;
            GAP_Y = 4;
            GAP_X = 3;
            HEIGHT = 35;
            // WIGHT =  (int) (1. * (width - X - (problems - 1) * GAP_X ) / problems);
            PR_WIDTH = 38;
        }
    }

    protected int getTeamId() {
        return teamId;
    }

    public void setTeamId(int id) {
        teamId = id;
    }

    protected Font FONT1 = Font.decode("Open Sans Italic " + 40);

    protected int X = 20;
    protected int Y = 20;
    protected int GAP_Y = 5;
    protected int GAP_X = 5;
    protected int PR_WIDTH = 50;
    protected int RUN_WIDTH = 80;
    protected int RUN_SMALL_WIDTH = 20;
    protected int HEIGHT = 45;
    protected int WIGHT = 30;
    protected int STAR_SIZE = 5;
    Font FONT2 = Font.decode("Open Sans Italic " + 30);

    private static final Color GREEN = new Color(27, 155, 82);//Color.decode("0x33ff00");
    private static final Color RED = Color.decode("0xaa0000");
    private static final Color YELLOW = new Color(250, 200, 82);//Color.decode("0x33ff00");
    protected TeamInfo team;
    protected int currentProblemId = -1;
    protected int nextProblemId = -1;

    public static final int PERIOD = 500;

    private double getTimeOpacity() {
        long time = System.currentTimeMillis();
        long second = time / PERIOD;
        long percent = time % PERIOD;

        double v = percent * 1.0 / PERIOD;
        return (second % 2 == 0 ? v : 1 - v) / 2 + 0.5;
    }

    private void drawReplay(Graphics g, int x, int y, int width, int height) {
        drawTextInRect(g, "R", (int) (x + width * 0.95), (int) (y + height * 0.17), -1, HEIGHT, Graphics.Position.POSITION_CENTER, FONT2, RED, Color.WHITE, getTimeOpacity());
    }

    @Override
    public void paintImpl(Graphics g, int width, int height) {
        if (!isVisible())
            return;

        if (team != null && currentUrl != null) {
            g.drawImage(image, xVideo, yVideo, widthVideo, heightVideo);
        }
        if (inChange) {
            team = Preparation.eventsLoader.getContestData().getParticipant(getTeamId());
            currentProblemId = nextProblemId;
//            log.info(this + " " + inChange);
            inChange = false;
            smallVideo.switchManually();
        }

        if (currentUrl == null || currentUrl.contains("info")) {
            return;
        }

        if (smallVideo != null && smallVideo.currentUrl != null) {
            smallVideo.paintImpl(g, width, height);
        }

        if (currentProblemId >= 0) {
            drawReplay(g, x, y, this.width, this.height);
        }

        // TODO: fill rect
        g.drawRect(x, y, this.width - widthVideo, height, new Color(0, 0, 30), 1);
//        teamId = Preparation.eventsLoader.getContestData().getParticipant(getTeamId());
//        if (teamId == null) return;
        int problemsNumber = team.getRuns().length;

        int dx = (isFull) ? (int) (this.width - X - HEIGHT * (RANK_WIDTH * 0.9 + NAME_WIDTH + TOTAL_WIDTH * 0.9 + PENALTY_WIDTH * 0.9))
                : (int) (this.width * 0.45);

        // int dx = (isFull) ? (this.width - X + (PR_WIDTH + GAP_X) * problemsNumber) : (int) (this.width * 0.45);
        int dy = (int) (this.height * 0.9);
        if (!isFull) {
            drawTeamPane(g, team, x + dx, y + dy, (int) (this.height * 0.08), 1);
        } else {
            drawTeamPane(g, team, x + dx, y + dy, HEIGHT, 1, RANK_WIDTH * 0.9, NAME_WIDTH, TOTAL_WIDTH * 0.9, PENALTY_WIDTH * 0.9);
        }

        for (int i = 0; i < team.getRuns().length; i++) {
            RunInfo[] runs = team.getRuns()[i].toArray(new RunInfo[0]);

            PlateStyle problemColor = TeamStylesheet.noProblem;
            for (int j = 0; j < runs.length; j++) {
                RunInfo run = runs[j];
                if ("AC".equals(run.getResult())) {
                    problemColor = TeamStylesheet.acProblem;
                    break;
                }

                problemColor = "".equals(run.getResult()) ? TeamStylesheet.udProblem: TeamStylesheet.waProblem;
            }

            if (!isFull) {
                int y = Y + (HEIGHT + GAP_Y) * i;
                drawTextInRect(g, "" + (char) ('A' + i), this.x + X, this.y + y,
                        PR_WIDTH, HEIGHT, Graphics.Position.POSITION_CENTER, FONT2, problemColor.background, problemColor.text, 1, WidgetAnimation.UNFOLD_ANIMATED);
            } else {
                // int x = X + (PR_WIDTH + GAP_X) * i;
                int x = dx - (PR_WIDTH + GAP_X) * (problemsNumber - i) - X;
                double timeOpacity = i == currentProblemId ? getTimeOpacity() : 1;
                drawTextInRect(g, "" + (char) ('A' + i), this.x + x, this.y + dy,
                        PR_WIDTH, HEIGHT, Graphics.Position.POSITION_CENTER, FONT2, problemColor.background, problemColor.text, timeOpacity, WidgetAnimation.UNFOLD_ANIMATED);
            }


            for (int j = 0; j < runs.length; j++) {
                RunInfo run = runs[j];
                if ("AC".equals(run.getResult())) {
                    if (!isFull) {
                        drawTextInRect(g, format(run.getTime() / 1000), this.x + x, this.y + y,
                                RUN_WIDTH, HEIGHT, Graphics.Position.POSITION_CENTER, FONT2, TeamStylesheet.acProblem.background, TeamStylesheet.acProblem.text,
                                i == currentProblemId ? getTimeOpacity() : 1,
                                WidgetAnimation.UNFOLD_ANIMATED
                        );
                    }
                    if (run.getTime() == Preparation.eventsLoader.getContestData().firstTimeSolved()[run.getProblemNumber()]) {
                        if (!isFull) {
                            drawStar(g, this.x + x + RUN_WIDTH, (int) (this.y + y + STAR_SIZE / 2), (int) STAR_SIZE);
                        } else {
                            int star_shift = 6;
                            int x = dx - (PR_WIDTH + GAP_X) * (problemsNumber - i - 1) - X - star_shift;
                            drawStar(g, this.x + x, (this.y + dy + star_shift), (int) (STAR_SIZE * 0.8));
                        }
                    }
                    break;
                } else {
                    if (isFull) {
                        continue;
                    }
                    PlateStyle color = "".equals(run.getResult()) ? TeamStylesheet.udProblem: TeamStylesheet.waProblem;
                    if (j == runs.length - 1) {
                        drawTextInRect(g, format(run.getTime() / 1000), this.x + x, this.y + y,
                                RUN_WIDTH, HEIGHT, Graphics.Position.POSITION_CENTER, FONT2, color.background, color.text,
                                i == currentProblemId ? getTimeOpacity() : 1,
                                WidgetAnimation.UNFOLD_ANIMATED
                        );

                        x += RUN_WIDTH + GAP_X;
                    } else {
                        drawTextInRect(g, "", this.x + x, this.y + y,
                                RUN_SMALL_WIDTH, HEIGHT, Graphics.Position.POSITION_CENTER, FONT2, color.background, color.text, 1, WidgetAnimation.UNFOLD_ANIMATED);
                        x += RUN_SMALL_WIDTH + GAP_X;
                    }
                }
            }
        }
    }

    private String format(double time) {
        int s = (int) time;
        int m = s / 60;
        s %= 60;
        int h = m / 60;
        m %= 60;
        return String.format("%d:%02d", h, m);
    }

    public void change(TeamInfo team, String infoType) {
        change(TeamUrls.getUrl(team, infoType));
        if (!infoType.equals("camera")) {
            smallVideo.changeManually(TeamUrls.getUrl(team, "camera"));
        } else {
            smallVideo.changeManually(TeamUrls.getUrl(team, "screen"));
        }
        nextProblemId = -1;
        teamId = team.getId();
    }

    public void change(RunInfo run) {
        change(TeamUrls.getUrl(run));
        smallVideo.changeManually(null);
        nextProblemId = run.getProblemNumber();
        teamId = run.getTeamId();
    }
}