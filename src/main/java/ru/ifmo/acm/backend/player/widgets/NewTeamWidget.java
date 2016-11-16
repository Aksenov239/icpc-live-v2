package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.backend.player.urls.TeamUrls;
import ru.ifmo.acm.backend.player.widgets.stylesheets.PlateStyle;
import ru.ifmo.acm.backend.player.widgets.stylesheets.TeamStylesheet;
import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;

import java.awt.*;

/**
 * @author: pashka
 */
public class NewTeamWidget extends VideoWidget {
    private static final int BIG_HEIGHT = 780;
    private static final int BIG_WIDTH = BIG_HEIGHT * 16 / 9;
    private static final int BIG_X = 493;
    private static final int BIG_Y = 89;

    private static final int SMALL_HEIGHT = 230;
    private static final int SMALL_WIDTH = SMALL_HEIGHT * 16 / 9;
    private static final int SMALL_X = 35;
    private static final int SMALL_Y = 89;

    private static final int TEAM_PANE_X = 936;
    private static final int TEAM_PANE_Y = 909;
    private static final int TEAM_PANE_HEIGHT = 85;

    protected int teamId;

    private int xVideo;
    private int yVideo;
    private int widthVideo;
    private int heightVideo;
    private int width;
    private int height;

    PlayerWidget smallVideo = null;
    boolean doubleVideo;
    private String currentInfoType;

    public NewTeamWidget(int sleepTime, boolean doubleVideo) {
        super(BIG_X, BIG_Y, BIG_WIDTH, BIG_HEIGHT, sleepTime, 0);
        this.width = BIG_WIDTH;
        this.height = BIG_HEIGHT;
        this.widthVideo = BIG_WIDTH;
        this.heightVideo = BIG_HEIGHT;
        this.xVideo = x + width - widthVideo;
        this.yVideo = y;
        teamId = -1;

        this.doubleVideo = doubleVideo;
        if (doubleVideo) {
            smallVideo = PlayerWidget.getPlayerWidget(SMALL_X, SMALL_Y, SMALL_WIDTH, SMALL_HEIGHT, sleepTime, 0);
        }
    }

    public void updateImpl(Data data) {
        //log.info(data.teamData.isTeamVisible);

        if (!data.teamData.isVisible) {
            setVisible(false);
        } else {
            setVisible(true);
            //log.info(data.teamData.teamId + " " + teamId + " " + ready.get());
            if ((data.teamData.getTeamId() != teamId || !data.teamData.infoType.equals(currentInfoType)) && ready) {
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


    protected int getTeamId() {
        return teamId;
    }

    public void setTeamId(int id) {
        teamId = id;
    }

    protected Font FONT1 = Font.decode("Open Sans " + 40);

    protected int X = 1320;
    protected int Y = 20;
    protected int GAP_Y = 5;
    protected int GAP_X = 5;
    protected int PR_WIDTH = 50;
    protected int RUN_WIDTH = 80;
    protected int RUN_SMALL_WIDTH = 20;
    protected int HEIGHT = 45;
    protected int STAR_SIZE = 5;
    Font FONT2 = Font.decode("Open Sans " + 30);

    protected TeamInfo team;
    protected int currentProblemId = -1;
    protected int nextProblemId = -1;

    public static final int PERIOD = 500;

    int volume = 0;

    private double getTimeOpacity() {
        long time = System.currentTimeMillis();
        long second = time / PERIOD;
        long percent = time % PERIOD;

        double v = percent * 1.0 / PERIOD;
        return (second % 2 == 0 ? v : 1 - v) / 2 + 0.5;
    }

    private void drawReplay(Graphics g, int x, int y, int width, int height) {
        drawTextInRect(g, "R", (int) (x + width * 0.95), (int) (y + height * 0.17), -1, HEIGHT,
                Graphics.Alignment.CENTER, FONT2, TeamStylesheet.replay, getTimeOpacity());
    }

    @Override
    public void paintImpl(Graphics g, int width, int height) {
        update();

//        if (teamId == -1) return;

        updateVisibilityState();

        if (visibilityState == 0) {
            if (teamId != -1) {
                teamId = -1;
                stop();
//                smallVideo.stop();
            }
            return;
        }

        if (inChange) {
            team = Preparation.eventsLoader.getContestData().getParticipant(getTeamId());
            currentProblemId = nextProblemId;
//            log.info(this + " " + inChange);
            inChange = false;
            if (doubleVideo) {
                smallVideo.switchManually();
            }
        }

        if (team == null || currentUrl == null) {
            setVisibilityState(0);
            return;
        }

//        if (currentUrl.get() == null || currentUrl.get().contains("info")) {
//            return;
//        }

        int newVolume = (int) (10 * visibilityState) * 10;
        newVolume = Math.min(newVolume, 99);
        if (newVolume != volume) {
            System.out.println("Set volume " + newVolume);
            volume = newVolume;
            setVolume(volume);
            if (doubleVideo) {
                smallVideo.setVolume(volume);
            }
        }

        {
            double x = visibilityState;
            xVideo = this.x = (int) (BIG_X + width * (1 - 3 * x * x + 2 * x * x * x));
        }
        if (doubleVideo && smallVideo != null && smallVideo.getCurrentURL() != null) {
            double x = visibilityState;
            smallVideo.x = (int) (SMALL_X - width * (1 - 3 * x * x + 2 * x * x * x));
            smallVideo.paintImpl(g, width, height);
        }

        g.drawImage(image, xVideo, yVideo, this.widthVideo, this.heightVideo);

        if (currentProblemId >= 0) {
            drawReplay(g, x, y, this.width, this.height);
        }

//        g.setColor(Color.WHITE);
//        g.setColor(new Color(0, 0, 30));
//        g.fillRect(x, y, this.width - widthVideo, height);
//        teamId = Preparation.eventsLoader.getContestData().getParticipant(getTeamId());
//        if (teamId == null) return;

        g.setFont(FONT1);
        drawTeamPane(g, team, TEAM_PANE_X, TEAM_PANE_Y, TEAM_PANE_HEIGHT, visibilityState);

        for (int i = 0; i < team.getRuns().length; i++) {
            int y = Y + (HEIGHT + GAP_Y) * i;
            RunInfo[] runs = team.getRuns()[i].toArray(new RunInfo[0]);

            PlateStyle problemColor = TeamStylesheet.noProblem;
            for (int j = 0; j < runs.length; j++) {
                RunInfo run = runs[j];
                if (run.getResult().equals("AC")) {
                    problemColor = TeamStylesheet.acProblem;
                    break;
                }
                if (run.getResult().equals("")) {
                    problemColor = TeamStylesheet.udProblem;
                } else {
                    problemColor = TeamStylesheet.waProblem;
                }
            }

            drawTextInRect(g, "" + (char) ('A' + i), this.x + X, this.y + y,
                    PR_WIDTH, HEIGHT, Graphics.Alignment.CENTER, FONT2, problemColor, 1);

            int x = X - GAP_X;
            for (int j = 0; j < runs.length; j++) {
                RunInfo run = runs[j];
                PlateStyle color = run.getResult().equals("AC") ? TeamStylesheet.acProblem : run.getResult().equals("") ? TeamStylesheet.udProblem : TeamStylesheet.waProblem;
                if (j == runs.length - 1 || run.getResult().equals("AC")) {
                    drawTextInRect(g, format(run.getTime() / 1000), this.x + x - RUN_WIDTH, this.y + y,
                            RUN_WIDTH, HEIGHT, Graphics.Alignment.CENTER, FONT2, color,
                            i == currentProblemId ? getTimeOpacity() : 1
                    );
                    //log.info(Arrays.toString(Preparation.eventsLoader.getContestData().firstTimeSolved()));
                    if (run.getResult().equals("AC") && run.getTime() == Preparation.eventsLoader.getContestData().firstTimeSolved()[run.getProblemNumber()]) {
                        drawStar(g, this.x + x - STAR_SIZE, (int) (this.y + y + STAR_SIZE), (int) STAR_SIZE);
                    }
                    x -= RUN_WIDTH + GAP_X;
                    break;
                } else if (run.getTime() != runs[j + 1].getTime()) {
                    drawTextInRect(g, "", this.x + x - RUN_SMALL_WIDTH, this.y + y,
                            RUN_SMALL_WIDTH, HEIGHT, Graphics.Alignment.CENTER, FONT2, color, 1);
                    x -= RUN_SMALL_WIDTH + GAP_X;
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
        if (doubleVideo) {
            if (!infoType.equals("camera")) {
                smallVideo.changeManually(TeamUrls.getUrl(team, "camera"));
            } else {
                smallVideo.changeManually(TeamUrls.getUrl(team, "screen"));
            }
        }
        nextProblemId = -1;
        teamId = team.getId();
    }

    public void change(RunInfo run) {
        change(TeamUrls.getUrl(run));
        if (doubleVideo) {
            smallVideo.changeManually(null);
        }
        nextProblemId = run.getProblemNumber();
        teamId = run.getTeamId();
    }

    public CachedData getCorrespondingData(Data data) {
        return data.teamData;
    }
}
