package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.backend.player.urls.TeamUrls;
import ru.ifmo.acm.datapassing.CachedData;
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
    private final int PLATE_HEIGHT;
    private final int GAP;

    public BreakingNewsWidget(long updateWait, int x, int y, int width, int height, double aspectRatio, int sleepTime, int duration) {
        super(x, y, width, (int) (height / aspectRatio), sleepTime, 0);
        this.updateWait = updateWait;
        wVideo = width;
        hVideo = (int) (width / aspectRatio);

        PLATE_HEIGHT = (int) (0.1 * hVideo);
        double total_factor = Widget.RANK_WIDTH + Widget.NAME_WIDTH + Widget.TOTAL_WIDTH + Widget.PENALTY_WIDTH + 3 * Widget.SPACE_X;
        PLATE_WIDTH = (int) (PLATE_HEIGHT * total_factor);
        GAP = (int) (0.02 * hVideo);

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.duration = duration;
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
                url = TeamUrls.getUrl(team, data.breakingNewsData.infoType);
            } else {
                if (run == null) {
                    log.warn("Couldn't find run for team " + teamId + " and problem " + problemId);
                    return;
                }
                url = TeamUrls.getUrl(run);
            }

            log.info("Change to " + url);

            change(url);
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
                        caption = "Becomes " + team.getRank() + " by solving";
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
    public void paintImpl(Graphics2D g, int width, int height) {
        update();

        int dt = updateVisibilityState();

        if (visibilityState == 0 && !isVisible()) {
            stop();
            return;
        }

        if (!ready) {
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

        if (run == null || currentUrl != null) {
            int hh = (int) (hVideo * opacity);
            g.drawImage(image, x, y + (hVideo - hh) / 2, wVideo, hh, null);
        }

        int y = this.y + hVideo + GAP;
        int x = this.x + (int) (1.1 * wVideo - PLATE_WIDTH);
        drawTeamPane(g, currentShow, x, y, PLATE_HEIGHT, rankState == 2 || rankState == 3 ? localVisibility : visibilityState);
        drawTextInRect(g, caption, (int) (x - 0.005 * PLATE_WIDTH), y, -1, PLATE_HEIGHT,
                POSITION_RIGHT, ACCENT_COLOR, Color.white, visibilityState, WidgetAnimation.UNFOLD_ANIMATED);
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return data.breakingNewsData;
    }
}
