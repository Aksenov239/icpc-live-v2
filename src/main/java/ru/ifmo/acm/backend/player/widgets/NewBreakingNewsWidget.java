package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.backend.player.urls.TeamUrls;
import ru.ifmo.acm.backend.player.widgets.stylesheets.BreakingNewsStylesheet;
import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;

import java.awt.*;

/**
 * @author: pashka
 */
public class NewBreakingNewsWidget extends Widget {
    private int x;
    private int y;
    private int width;
    private int height;
    private int duration;
    private PlayerWidget video;
    private final int PLATE_WIDTH;
    private final int PLATE_HEIGHT;
    private final int GAP;

    public NewBreakingNewsWidget(long updateWait, int width, int height) {
        video = PlayerWidget.getPlayerWidget(x, y, width, height, 1000, 0);
        this.updateWait = updateWait;

        PLATE_HEIGHT = (int) (0.1 * video.height);
        double total_factor = Widget.RANK_WIDTH + Widget.NAME_WIDTH + Widget.TOTAL_WIDTH + Widget.PENALTY_WIDTH + 3 * Widget.SPACE_X;
        PLATE_WIDTH = (int) (PLATE_HEIGHT * total_factor);
        GAP = (int) (0.02 * video.height);

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.duration = 20000;
        last = System.currentTimeMillis();
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

            video.sleepTime = data.teamData.sleepTime;

            int teamId = data.breakingNewsData.teamId;
            int problemId = data.breakingNewsData.problemId;

            log.info("Get request for " + teamId + " " + problemId);

            team = Preparation.eventsLoader.getContestData().getParticipant(teamId);

            run = data.breakingNewsData.runId == -1 ? null :
                    Preparation.eventsLoader.getContestData().getRun(data.breakingNewsData.runId);

            if (run == null || (run.getTeamId() != teamId || run.getProblemNumber() != problemId)) {
                java.util.List<RunInfo> runs = team.getRuns()[problemId];

                run = null;
                if (runs.size() != 0) {
                    run = runs.get(runs.size() - 1);
                    for (RunInfo run1 : runs) {
                        if (run1.isAccepted()) {
                            run = run1;
                        }
                    }
                }
            }

            String url;
            if (data.breakingNewsData.isLive) {
                if ("".equals(data.breakingNewsData.infoType)) {
                    url = null;
                } else {
                    url = TeamUrls.getUrl(team, data.breakingNewsData.infoType);
                }
            } else {
                if (run == null) {
                    log.warn("Couldn't find run for team " + teamId + " and problem " + problemId);
                    return;
                }
                url = TeamUrls.getUrl(run);
            }

            log.info("Change to " + url);

            video.loadNext(url);
            isLive = data.breakingNewsData.isLive;

            if (run != null) {
                currentShow = run.getTeamInfoBefore();
                currentShow = currentShow == null ? team : currentShow;
            } else {
                currentShow = team;
            }

            if (data.breakingNewsData.newsMessage.length() == 0) {
                if (isLive && run == null) {
                    log.warn("Can't generate caption for team" + teamId + " problem " + problemId + ", " +
                            "because video is live and don't know run id");
                    return;
                }
                if (run.getResult().equals("AC")) {
                    if (run.getTime() == Preparation.eventsLoader.getContestData().firstTimeSolved()[problemId]) {
                        caption = "First to solve";
                    } else if (team.getRank() <= 12) {
                        caption = "Solved";
                    } else {
                        caption = "Solved";
                    }
                } else if (run.getResult().length() == 0) {
                    caption = "Submitted";
                } else {
                    caption = "Got " + run.getResult() + " on";
                }
                caption += " problem " + (char) ('A' + problemId);
            } else {
                caption = data.breakingNewsData.newsMessage;
            }
            log.info("Caption: " + caption);
            timer = 0;
            rankState = 0;
            visibilityState = 0;
            setVisible(true);
            if (team == currentShow) {
                rankState = 4;
            }
        }

        lastUpdate = System.currentTimeMillis();
    }

    private double localVisibility;
    private int rankState;

    @Override
    public void paintImpl(Graphics g, int width, int height) {
        update();

        int dt = updateVisibilityState();

        video.updateState(g, false);

        if (visibilityState == 0 && !isVisible()) {
            video.stop();
            return;
        }

        if (video.nextIsReady()) {
            video.switchToNext();
        }

        if (video.getCurrentURL() == null) {
            visibilityState = 0;
            return;
        }


        timer += dt;
        if (rankState == 0) {
            setVisibilityState(0);
            rankState = 1;
        }
        if (timer > duration / 2 - 1000 && rankState == 1) {
            rankState = 2;
            localVisibility = 1;
        }
        if (rankState == 2) {
            localVisibility -= dt * V;
            localVisibility = Math.max(localVisibility, 0);
            if (localVisibility == 0) {
                rankState = 3;
            }
        }
        if (rankState == 3) {
            localVisibility += dt * V;
            localVisibility = Math.min(localVisibility, 1);
            currentShow = team;
            if (localVisibility == 1) {
                rankState = 4;
            }
        }

//        log.debug(visibilityState + " " + opacity);

        if (//run == null ||
                video.getCurrentURL() != null) {
            int hh = video.height;//(int) (video.height * opacity);
            video.draw(g, x, y + (video.height - hh) / 2, video.width, hh, opacity);
        }

//        int y = this.y + video.height + GAP;
//        int x = this.x + (int) (1.1 * video.width - PLATE_WIDTH);
//        drawTeamPane(g, currentShow, x, y, PLATE_HEIGHT, rankState == 2 || rankState == 3 ? localVisibility : visibilityState);
//        Font font = Font.decode("Open Sans " + (int) Math.round(PLATE_HEIGHT * 0.7));
//        drawTextInRect(g, caption, (int) (x - 0.005 * PLATE_WIDTH), y, -1, PLATE_HEIGHT,
//                Graphics.Alignment.RIGHT, font, BreakingNewsStylesheet.caption, visibilityState, WidgetAnimation.UNFOLD_ANIMATED);
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {

        return data.breakingNewsData;
    }

    public RunInfo getRun() {
        return run;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
