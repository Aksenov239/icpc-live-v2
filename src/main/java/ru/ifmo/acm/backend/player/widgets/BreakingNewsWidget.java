package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;

import java.awt.*;

/**
 * @author: pashka
 */
public class BreakingNewsWidget extends VideoWidget {
    private int x;
    private int y;
    private int duration;
    private int wVideo;
    private int hVideo;
    private final int PLATE_WIDTH;
    private final int GAP;

    public BreakingNewsWidget(int x, int y, int width, int height, double aspectRatio, int sleepTime, int duration) {
        super(x, y, width, (int) (height / aspectRatio), sleepTime, 0);
        wVideo = width;
        hVideo = (int) (height / aspectRatio);

        PLATE_WIDTH = (int) (1.5 * width);
        GAP = (int) (0.05 * hVideo);

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.duration = duration;
    }

    private String caption;
    private RunInfo run;
    private TeamInfo team;
    private boolean isLive;
    private TeamInfo currentShow;
    private double timer;

    @Override
    protected void updateImpl(Data data) {
        if (lastUpdate + duration < System.currentTimeMillis()) {
            setVisible(false);
        } else {
            if (!data.breakingNewsData.isVisible) {
                setVisible(false);
                return;
            }
            if (isVisible())
                return;
            setVisible(true);

            int teamId = data.breakingNewsData.teamId;
            int problemId = data.breakingNewsData.problemId;

            team = Preparation.eventsLoader.getContestData().getParticipant(teamId).getSmallTeamInfo();
            java.util.List<RunInfo> runs = team.getRuns()[problemId];
            run = runs.get(runs.size() - 1);
            for (RunInfo run1 : runs) {
                if (run1.isAccepted()) {
                    run = run1;
                }
            }

            String url;
            if (data.breakingNewsData.isLive) {
                url = TeamWidget.getUrl(team, data.breakingNewsData.infoType);
            } else {
                url = run.toString();
            }

            change(url);
            isLive = data.breakingNewsData.isLive;

            if (run.getResult().equals("AC")) {
                if (run.getTime() == Preparation.eventsLoader.getContestData().firstTimeSolved()[problemId]) {
                    caption = "First to solve";
                } else if (team.getRank() <= 12) {
                    caption = "Get to " + team.getRank() + " by solving";
                } else {
                    caption = "Solved";
                }
            } else if (run.getResult().length() == 0) {
                caption = "Submitted";
            }
            caption += " problem " + (char) ('A' + problemId);

            currentShow = run.getTeamInfoBefore();
            timer = 0;
        }

        lastUpdate = System.currentTimeMillis();
    }

    @Override
    public void paintImpl(Graphics2D g, int width, int height) {
        update();

        if (visibilityState == 0) {
            stop();
            return;
        }
        int dt = updateVisibilityState();

        timer += dt;

        if (timer > duration / 2) {
            currentShow = team;
        }

        if (run == null || URL.get() == null) {
            int hh = (int) (hVideo * opacity);
            g.drawImage(image.get(), x, y + (hVideo - hh) / 2, wVideo, hh, null);
        }

        int y = this.y + hVideo + GAP;
        int x = this.x + (wVideo - PLATE_WIDTH) / 2;
        drawTeamPane(g, currentShow, x, y + hVideo, PLATE_WIDTH, visibilityState);
        drawTextInRect(g, caption, (int) (x - 0.005 * PLATE_WIDTH), y, -1, PLATE_WIDTH / 10,
                POSITION_RIGHT, ACCENT_COLOR, Color.white, visibilityState);
    }
}
