package org.icpclive.backend.player.widgets;

import org.icpclive.backend.Preparation;
import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.player.urls.TeamUrls;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.backend.player.widgets.stylesheets.TeamStylesheet;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.events.RunInfo;
import org.icpclive.events.TeamInfo;

import java.awt.*;

/**
 * @author: pashka
 */
public class TeamWidget extends Widget {

    protected int teamId;

    private int width;
    private int height;

    private boolean isFull = false;

    PlayerWidget mainVideo = null;
    PlayerWidget smallVideo = null;

    public TeamWidget(int x, int y, int width, int height, double aspectRatio, int sleepTime) {
        mainVideo = PlayerWidget.getPlayerWidget(x + width - (int) (height * aspectRatio), y, (int) (height * aspectRatio), height, sleepTime, 0);
        this.width = width;
        this.height = height;
        teamId = -1;

        int xSmallVideo = x + (int) (width * 0.7);
        int ySmallVideo = y + (int) (height * 0.6);
        int hSmallVideo = (int) (height * 0.25);
        int wSmallVideo = (int) (hSmallVideo * aspectRatio);
        smallVideo = PlayerWidget.getPlayerWidget(xSmallVideo, ySmallVideo, wSmallVideo, hSmallVideo, sleepTime, 0);
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

    protected Font FONT1 = Font.decode(MAIN_FONT + " " + 40);

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
    Font FONT2 = Font.decode(MAIN_FONT + " " + 30);

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

    private void drawReplay(AbstractGraphics g, int x, int y, int width, int height) {
        drawTextInRect(g, "R", (int) (x + width * 0.95), (int) (y + height * 0.17), -1,
                HEIGHT, PlateStyle.Alignment.CENTER, FONT2, TeamStylesheet.replay, getTimeOpacity());
    }

    @Override
    public void paintImpl(AbstractGraphics g, int width, int height) {
        mainVideo.updateState(g, false);
        if (!isVisible())
            return;

        if (team != null && mainVideo.getCurrentURL() != null) {
            mainVideo.paint(g, width, height);
        }
        if (mainVideo.inChange) {
            team = Preparation.eventsLoader.getContestData().getParticipant(getTeamId());
            currentProblemId = nextProblemId;
//            log.info(this + " " + inChange);
            mainVideo.inChange = false;
            smallVideo.switchToNext();
        }

        if (mainVideo.getCurrentURL() == null || mainVideo.getCurrentURL().contains("info")) {
            return;
        }

        if (smallVideo != null && smallVideo.getCurrentURL() != null) {
            smallVideo.paintImpl(g, width, height);
        }

        int x = mainVideo.x;
        int y = mainVideo.y;

        if (currentProblemId >= 0) {
            drawReplay(g, x, y, this.width, this.height);
        }

        // TODO: fill rect
        g.drawRect(x, y, this.width - mainVideo.width, height, new Color(0, 0, 30), 1, PlateStyle.RectangleType.SOLID);
//        teamId = Preparation.eventsLoader.getContestData().getParticipant(getTeamId());
//        if (teamId == null) return;

        g.setFont(FONT2);

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
                int yy = Y + (HEIGHT + GAP_Y) * i;
                drawTextInRect(g, "" + (char) ('A' + i), x + X, y + yy,
                        PR_WIDTH, HEIGHT, PlateStyle.Alignment.CENTER, FONT2,
                        problemColor, 1, 1, WidgetAnimation.UNFOLD_ANIMATED);
            } else {
                // int x = X + (PR_WIDTH + GAP_X) * i;
                int xx = dx - (PR_WIDTH + GAP_X) * (problemsNumber - i) - X;
                double timeOpacity = i == currentProblemId ? getTimeOpacity() : 1;
                drawTextInRect(g, "" + (char) ('A' + i), x + xx, y + dy,
                        PR_WIDTH, HEIGHT, PlateStyle.Alignment.CENTER, FONT2, problemColor,
                        timeOpacity, 1, WidgetAnimation.UNFOLD_ANIMATED);
            }


            for (int j = 0; j < runs.length; j++) {
                RunInfo run = runs[j];
                if ("AC".equals(run.getResult())) {
                    if (!isFull) {
                        drawTextInRect(g, format(run.getTime() / 1000), x + x, y + y,
                                RUN_WIDTH, HEIGHT, PlateStyle.Alignment.CENTER, FONT2, TeamStylesheet.acProblem,
                                i == currentProblemId ? getTimeOpacity() : 1,
                                1, WidgetAnimation.UNFOLD_ANIMATED
                        );
                    }
                    if (run.getTime() == Preparation.eventsLoader.getContestData().firstTimeSolved()[run.getProblemNumber()]) {
                        if (!isFull) {
                            drawStar(g, x + x + RUN_WIDTH, (int) (y + y + STAR_SIZE / 2), (int) STAR_SIZE);
                        } else {
                            int star_shift = 6;
                            int xx = dx - (PR_WIDTH + GAP_X) * (problemsNumber - i - 1) - X - star_shift;
                            drawStar(g, x + xx, (y + dy + star_shift), (int) (STAR_SIZE * 0.8));
                        }
                    }
                    break;
                } else {
                    if (isFull) {
                        continue;
                    }
                    PlateStyle color = "".equals(run.getResult()) ? TeamStylesheet.udProblem: TeamStylesheet.waProblem;
                    if (j == runs.length - 1) {
                        drawTextInRect(g, format(run.getTime() / 1000), x + x, y + y,
                                RUN_WIDTH, HEIGHT, PlateStyle.Alignment.CENTER, FONT2, color,
                                i == currentProblemId ? getTimeOpacity() : 1,
                                1, WidgetAnimation.UNFOLD_ANIMATED
                        );

                        x += RUN_WIDTH + GAP_X;
                    } else {
                        drawTextInRect(g, "", x + x, y + y,
                                RUN_SMALL_WIDTH, HEIGHT, PlateStyle.Alignment.CENTER, FONT2, color, 1, 1, WidgetAnimation.UNFOLD_ANIMATED);
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
        mainVideo.change(TeamUrls.getUrl(team, infoType));
        if (!infoType.equals("camera")) {
            smallVideo.loadNext(TeamUrls.getUrl(team, "camera"));
        } else {
            smallVideo.loadNext(TeamUrls.getUrl(team, "screen"));
        }
        nextProblemId = -1;
        teamId = team.getId();
    }

    public void change(RunInfo run) {
        mainVideo.change(TeamUrls.getUrl(run));
        smallVideo.loadNext(null);
        nextProblemId = run.getProblemNumber();
        teamId = run.getTeamId();
    }

    public void change(RunInfo run, TeamInfo teamInfo) {
        mainVideo.change(TeamUrls.getUrl(teamInfo, "camera"));
        smallVideo.loadNext(null);
        nextProblemId = run.getProblemNumber();
        teamId = run.getTeamId();
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return null;
    }
}