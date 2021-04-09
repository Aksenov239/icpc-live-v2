package org.icpclive.backend.player.widgets;

import org.icpclive.backend.Preparation;
import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.player.PlayerInImage;
import org.icpclive.backend.player.urls.TeamUrls;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.backend.player.widgets.stylesheets.QueueStylesheet;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.events.ContestInfo;
import org.icpclive.events.EventsLoader;
import org.icpclive.events.PCMS.PCMSTeamInfo;
import org.icpclive.events.RunInfo;
import org.icpclive.events.TeamInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author: pashka
 */
public class IOITeamWidget extends Widget {

    private static double standardAspect = 16. / 9;

    List<TeamStatusView> views = new ArrayList<>();
    int currentView;
    private int timeToSwitch;

    boolean aspect43 = false;

    public IOITeamWidget(int sleepTime, boolean aspect43) {
        this.sleepTime = sleepTime;
        this.aspect43 = aspect43;
        if (aspect43) {
            standardAspect = 4. / 3;
        }
        views.add(emptyView);
    }

    public void updateImpl(Data data) {
        timeToSwitch = data.teamData.sleepTime;
        if (!data.teamData.isVisible) {
            setVisible(false);
            if (views.get(currentView) != emptyView) {
                views.get(currentView).timeToSwitch = 0;
            }
        } else {
            setVisible(true);
            int teamId = data.teamData.getTeamId();
            boolean ok;
            if (teamId >= 0) {
                TeamInfo team = Preparation.eventsLoader.getContestData().getParticipant(data.teamData.getTeamId());
                ok = false;
                for (int i = currentView; i < views.size(); i++) {
                    TeamStatusView view = views.get(i);
                    if (view.team != null && team != null &&
                            view.team.getId() == team.getId()) {
                        ok = true;
                        break;
                    }
                }
            } else {
                ok = true;
            }
            if (!ok) {
                TeamInfo team = Preparation.eventsLoader.getContestData().getParticipant(data.teamData.getTeamId());
                String infoType = data.teamData.infoType;
                timeToSwitch = data.teamData.sleepTime;
                System.err.println("SWITCH " + timeToSwitch + " " + data.teamData.sleepTime);
                addView(team, infoType);
            }
        }
    }

    @Override
    public void paintImpl(AbstractGraphics g, int width, int height) {
        super.paintImpl(g, width, height);
        for (TeamStatusView view : views) {
            view.paintImpl(g, width, height);
        }
        if ((views.get(currentView).timeToSwitch <= System.currentTimeMillis() ||
                (currentView + 1 < views.size() && !views.get(currentView + 1).mainVideo.isBlack()))
                && views.get(currentView).isVisible()) {
            views.get(currentView).setVisible(false);
        }
        if (views.get(currentView).visibilityState <= 0) {
            System.err.println("Switch view");
            if (views.get(currentView).mainVideo != null)
                views.get(currentView).mainVideo.stop();
            currentView++;
            if (currentView == views.size()) {
                views.add(emptyView);
                emptyView.timeToSwitch = Long.MAX_VALUE;
            }
            views.get(currentView).setVisible(true);
        }
    }

    public void addView(TeamInfo team, String infoType) {
        System.err.println("Add view " + team + " " + infoType);
        if (views.size() - currentView > 0) {
            views.get(currentView).timeToSwitch = System.currentTimeMillis() + timeToSwitch; // FIX!!!
            System.err.println("TTL " + timeToSwitch);
        }
        views.add(new TeamStatusView(team, infoType, sleepTime));
    }

    public CachedData getCorrespondingData(Data data) {
        return data.teamData;
    }

    TeamStatusView emptyView = new TeamStatusView(null, null, sleepTime);

    class TeamStatusView extends Widget {

        private static final int BIG_HEIGHT = 1305 * 9 / 16;//780;
        private static final int BIG_X_RIGHT = 1893;//493;
        private static final int BIG_Y = 52;

        private static final int BIG_HEIGHT_43 = 1285 * 3 / 4;
        private static final int BIG_X_RIGHT_43 = 1893;
        private static final int BIG_Y_43 = 52;

        private static final int TEAM_PANE_X = 30;
        private static final int TEAM_PANE_Y = 52;
        private static final int TEAM_PANE_HEIGHT = 41;

        private final int nameWidth;
        private final int rankWidth;
        private final int solvedWidth;
        private final int problemWidth;
        private final int statusWidth;
        private final int timeWidth;
        private final int penaltyWidth;

        private int width;
        private int height;

        private long timeToChange;

        private String infoType;

        private final PlayerInImage mainVideo;
        private final TeamStatsWidget stats;
        private TeamInfo team;
        long timeToSwitch = Long.MAX_VALUE;

        public TeamStatusView(TeamInfo team, String infoType, int sleepTime) {
            this.team = team;
            this.infoType = infoType;
            if (aspect43) {
                height = BIG_HEIGHT_43;
            } else {
                height = BIG_HEIGHT;
            }
            width = (int) (standardAspect * height);

            nameWidth = (int) Math.round(NAME_WIDTH * TEAM_PANE_HEIGHT);
            rankWidth = (int) Math.round(RANK_WIDTH * TEAM_PANE_HEIGHT);
            solvedWidth = (int) Math.round(PROBLEM_WIDTH * TEAM_PANE_HEIGHT);
            problemWidth = (int) Math.round(TOTAL_WIDTH * TEAM_PANE_HEIGHT);
            statusWidth = (int) Math.round(STATUS_WIDTH * TEAM_PANE_HEIGHT);
            timeWidth = (int) Math.round(TIME_WIDTH * TEAM_PANE_HEIGHT);
            penaltyWidth = (int) Math.round(PENALTY_WIDTH * TEAM_PANE_HEIGHT);
            setFont(Font.decode(MAIN_FONT + " " + (int) (TEAM_PANE_HEIGHT * 0.7)));

            if (team == null) {
                mainVideo = null;
                stats = null;
            } else if (infoType.equals("") || infoType.equals("stats")) {
                mainVideo = null;
                stats = new TeamStatsWidget(team);
            } else {
                System.err.println("Load video: " + TeamUrls.getUrl(team, infoType));
                PlayerInImage video = null;
                try {
                    video = new PlayerInImage(width, height, null, TeamUrls.getUrl(team, infoType));
                    ;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mainVideo = video;
                if (team instanceof PCMSTeamInfo) {
                    stats = null;
                } else {
                    stats = new TeamStatsWidget(team);
                }
            }
        }

        @Override
        protected void paintImpl(AbstractGraphics g, int width, int height) {
            super.paintImpl(g, width, height);
            if (team == null) {
                return;
            }
            team = EventsLoader.getInstance().getContestData().getParticipant(team.getId());
            if (visibilityState == 0) {
                return;
            }
            if (!(infoType.equals("") || infoType.equals("stats"))) {
                setBackgroundColor(Color.BLACK);
                setMaximumOpacity(0.3);
                drawRectangle(0, 0, width, height);
            }
            if (mainVideo != null) {
                drawVideos();
                drawStatus();
            }
            if (stats != null) {
                drawInfo();
            }
        }

        private void drawInfo() {
            stats.setVisibilityState(visibilityState);
            stats.paint(graphics, 1920, 1080);
        }

        private void drawVideos() {
            if (!mainVideo.isBlack()) {
                applyStyle(QueueStylesheet.name);
                drawRectangle(BIG_X_RIGHT - width, BIG_Y, width, height);
                int BORDER = 0;
                graphics.drawImage(mainVideo.getImage(), BIG_X_RIGHT - width + BORDER, BIG_Y + BORDER, width - 2 * BORDER, height - 2 * BORDER, opacity);
            }
        }

        private void drawStatus() {
            String name = team.getShortName();

            PlateStyle teamColor = QueueStylesheet.name;

            int baseX = BIG_X_RIGHT;

            int x = baseX - rankWidth - nameWidth - solvedWidth - penaltyWidth;
            int y = TEAM_PANE_Y;

            PlateStyle color = getTeamRankColor(team);
            applyStyle(color);
            drawRectangleWithText("" + Math.max(team.getRank(), 1), x, y,
                    rankWidth, TEAM_PANE_HEIGHT, PlateStyle.Alignment.CENTER, false, false);

            x += rankWidth;

            applyStyle(teamColor);
            drawRectangleWithText(name, x, y,
                    nameWidth, TEAM_PANE_HEIGHT, PlateStyle.Alignment.LEFT);

            x += nameWidth;

            drawRectangleWithText("" + team.getSolvedProblemsNumber(), x, y,
                    solvedWidth, TEAM_PANE_HEIGHT, PlateStyle.Alignment.CENTER);

            x += solvedWidth;

            drawRectangleWithText("" + team.getPenalty(), x, y,
                    penaltyWidth, TEAM_PANE_HEIGHT, PlateStyle.Alignment.CENTER);


            List<RunInfo> lastRuns = new ArrayList<>();
            for (List<? extends RunInfo> runs : team.getRuns()) {
                RunInfo lastRun = null;
                for (RunInfo run : runs) {
                    lastRun = run;
                    if (lastRun.isAccepted()) {
                        break;
                    }
                }
                if (lastRun != null) {
                    lastRuns.add(lastRun);
                }
            }

            Collections.sort(lastRuns, (o1, o2) -> Long.compare(o1.getTime(), o2.getTime()));

            boolean odd = false;

            for (RunInfo run : lastRuns) {
                odd = !odd;
                y += TEAM_PANE_HEIGHT;
                x = baseX - timeWidth - problemWidth - statusWidth;
                applyStyle(teamColor);
                if (odd) {
                    setMaximumOpacity(maximumOpacity * .9);
                }
                drawRectangleWithText("" + format(run.getTime()), x, y,
                        timeWidth, TEAM_PANE_HEIGHT, PlateStyle.Alignment.CENTER);
                x += timeWidth;
                drawProblemPane(run.getProblem(), x, y, problemWidth, TEAM_PANE_HEIGHT);
                x += problemWidth;

                PlateStyle resultColor = QueueStylesheet.udProblem;

                boolean inProgress = false;
                int progressWidth = 0;

                if (run.isJudged()) {
                    if (run.isAccepted()) {
                        resultColor = QueueStylesheet.acProblem;
                    } else {
                        resultColor = QueueStylesheet.waProblem;
                    }
                } else {
                    inProgress = true;
                    progressWidth = (int) Math.round(statusWidth * run.getPercentage());
                }

                String result = run.getResult();
                if (run.getTime() > ContestInfo.FREEZE_TIME) {
                    result = "?";
                    resultColor = QueueStylesheet.frozenProblem;
                    inProgress = false;
                }

                applyStyle(resultColor);
                if (odd && resultColor.background.equals(QueueStylesheet.frozenProblem.background)) {
                    setMaximumOpacity(maximumOpacity * .9);
                }
                drawRectangleWithText(result, x, y, statusWidth,
                        TEAM_PANE_HEIGHT, PlateStyle.Alignment.CENTER);

                if (inProgress) {
                    setBackgroundColor(QueueStylesheet.udTests);
                    drawRectangle(x, y, progressWidth, TEAM_PANE_HEIGHT);
                }
                if (run == EventsLoader.getInstance().getContestData().firstSolvedRun()[run.getProblemId()]) {
                    drawStar(x + statusWidth - STAR_SIZE, y + 2 * STAR_SIZE, STAR_SIZE, 1);
                }
            }


//        drawProblemPane(problem, x, y, problemWidth, plateHeight, blinking);
//
//        x += problemWidth + spaceX;

        }

        private String format(long time) {
            int s = (int) (time / 1000);
            int m = s / 60;
            s %= 60;
            int h = m / 60;
            m %= 60;
            return String.format("%d:%02d", h, m);
        }

        @Override
        protected CachedData getCorrespondingData(Data data) {
            return null;
        }
    }
}
