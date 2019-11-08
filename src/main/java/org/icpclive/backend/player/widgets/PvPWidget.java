package org.icpclive.backend.player.widgets;

import org.apache.http.cookie.SM;
import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.player.PlayerInImage;
import org.icpclive.backend.player.urls.TeamUrls;
import org.icpclive.backend.player.widgets.stylesheets.BigStandingsStylesheet;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.backend.player.widgets.stylesheets.QueueStylesheet;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.datapassing.PvPData;
import org.icpclive.events.ContestInfo;
import org.icpclive.events.EventsLoader;
import org.icpclive.events.RunInfo;
import org.icpclive.events.TeamInfo;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PvPWidget extends Widget {

    public static final String MAIN_VIDEO = "screen";
    public static final String SECOND_VIDEO = "camera";

    private static boolean aspect43;
    private static double standardAspect;
    private PvPData data;

    public PvPWidget(int sleepTime, boolean aspect43) {
        this.sleepTime = sleepTime;
        this.aspect43 = aspect43;
        if (aspect43) {
            standardAspect = 4. / 3;
        } else {
            standardAspect = 16. / 9;
        }
    }

    java.util.List<TeamStatusView> views = new ArrayList<>();

    @Override
    public void updateImpl(Data data) {
        this.data = data.pvpData;
        setVisible(data.pvpData.isVisible());
        if (isVisible() && views.size() == 0) {
            views.clear();
            views.add(new TeamStatusView(EventsLoader.getInstance().getContestData().getParticipant(data.pvpData.shownTeamId[0]), sleepTime, 0));
            views.add(new TeamStatusView(EventsLoader.getInstance().getContestData().getParticipant(data.pvpData.shownTeamId[1]), sleepTime, 1));
            setVisible(true);
        }
        if (!isVisible() && visibilityState == 0 && views.size() > 0) {
            for (TeamStatusView view : views) {
                view.stop();
            }
            views.clear();
        }
    }

    @Override
    public void paintImpl(AbstractGraphics g, int width, int height) {
        super.paintImpl(g, width, height);
        update();
        if (!isVisible() && visibilityState == 0) {
            return;
        }
        for (TeamStatusView view : views) {
            view.mainVideo.setVolume((int) (visibilityState * 100));
            view.secondVideo.setVolume((int) (visibilityState * 100));
        }
        boolean ok = true;
        for (TeamStatusView view : views) {
            if (view.mainVideo.isBlack() || view.secondVideo.isBlack()) {
                ok = false;
            }
        }
        if (!ok) {
            visibilityState = 0;
            return;
        }
        setBackgroundColor(Color.BLACK);
        setMaximumOpacity(0.3);
        drawRectangle(0, 0, width, height);
        for (TeamStatusView view : views) {
            view.setVisibilityState(visibilityState);
            view.paint(g, width, height);
        }
////        System.err.println("!!!");%tInstance().getContestData().getParticipant(data.shownTeamId[0]), 500, 500, 50, visibilityState);
//        drawTeamPane(g, EventsLoader.getInstance().getContestData().getParticipant(data.shownTeamId[1]), 500, 600, 50, visibilityState);
    }

    @Override
    public CachedData getCorrespondingData(Data data) {
        return data.pvpData;
    }

    static class TeamStatusView extends Widget {

        private final int id;

        private static final int BIG_HEIGHT = 474;
        private static final int BIG_X_RIGHT = 1896;
        private final int PROBLEMS_Y;
        private int BIG_Y;

        private static final int SMALL_HEIGHT = 270;
        private static final int SMALL_X = 569;
        private int SMALL_Y;

//        private static final int BIG_HEIGHT_43 = 1285 * 3 / 4;
//        private static final int BIG_X_RIGHT_43 = 1893;
//        private static final int BIG_Y_43 = 52;
//
//        private static final int SMALL_HEIGHT = 270;
//        private static final int SMALL_X_RIGHT = 1035;
//        private static final int[] SMALL_Y = {160, 627};

        private static final int TEAM_PANE_X = SMALL_X;
        private int TEAM_PANE_Y;
        private static final int TEAM_PANE_HEIGHT = 41;

        private final int nameWidth;
        private final int rankWidth;
        private final int solvedWidth;
        private final int problemWidth;
        private final int statusWidth;
        private final int timeWidth;
        private final int penaltyWidth;
        private final int teamPaneWidth;

        private int mainWidth;
        private int mainHeight;

        private int secondWidth;
        private int secondHeight;

        private final PlayerInImage mainVideo;
        private final PlayerInImage secondVideo;

        private TeamInfo team;

        public TeamStatusView(TeamInfo team, int sleepTime, int id) {
            this.team = team;
            this.id = id;

            if (id == 0) {
                BIG_Y = 500 - BIG_HEIGHT;
                PROBLEMS_Y =  500 - TEAM_PANE_HEIGHT;
                TEAM_PANE_Y = 500 - TEAM_PANE_HEIGHT - TEAM_PANE_HEIGHT;
                SMALL_Y = 500 - TEAM_PANE_HEIGHT - TEAM_PANE_HEIGHT - SMALL_HEIGHT;
            } else {
                BIG_Y = 530;
                PROBLEMS_Y = 530;
                TEAM_PANE_Y = 530 + TEAM_PANE_HEIGHT;
                SMALL_Y = 530 + TEAM_PANE_HEIGHT + TEAM_PANE_HEIGHT;
            }

//            if (aspect43) {
//                mainHeight = BIG_HEIGHT_43;
//            } else {
                mainHeight = BIG_HEIGHT;
//            }
            mainWidth = (int) (standardAspect * mainHeight);

//            if (aspect43) {
//                secondHeight = SMALL_HEIGHT_43;
//            } else {
                secondHeight = SMALL_HEIGHT;
//            }
            secondWidth = (int) (standardAspect * secondHeight);

            nameWidth = (int) Math.round(NAME_WIDTH * TEAM_PANE_HEIGHT);
            rankWidth = (int) Math.round(RANK_WIDTH * TEAM_PANE_HEIGHT);
            solvedWidth = (int) Math.round(PROBLEM_WIDTH * TEAM_PANE_HEIGHT);
            problemWidth = (int) Math.round(TOTAL_WIDTH * TEAM_PANE_HEIGHT);
            statusWidth = (int) Math.round(STATUS_WIDTH * TEAM_PANE_HEIGHT);
            timeWidth = (int) Math.round(TIME_WIDTH * TEAM_PANE_HEIGHT);
            penaltyWidth = (int) Math.round(PENALTY_WIDTH * TEAM_PANE_HEIGHT);

            teamPaneWidth = nameWidth + rankWidth + solvedWidth + penaltyWidth;

            setFont(Font.decode(MAIN_FONT + " " + (int) (TEAM_PANE_HEIGHT * 0.7)));

            System.err.println("Load video: " + TeamUrls.getUrl(team, MAIN_VIDEO));
            PlayerInImage video = null;
            try {
                video = new PlayerInImage(mainWidth, mainHeight, null, TeamUrls.getUrl(team, MAIN_VIDEO));
            } catch (Exception e) {
                e.printStackTrace();
            }
            mainVideo = video;
            System.err.println("Load video: " + TeamUrls.getUrl(team, SECOND_VIDEO));
            try {
                video = new PlayerInImage(mainWidth, mainHeight, null, TeamUrls.getUrl(team, SECOND_VIDEO));
            } catch (Exception e) {
                e.printStackTrace();
            }
            secondVideo = video;
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

            if (mainVideo != null) {
                drawVideos();
                drawStatus();
            }
        }

        private void drawVideos() {
            if (!mainVideo.isBlack()) {
                graphics.drawImage(mainVideo.getImage(), BIG_X_RIGHT - mainWidth, BIG_Y, mainWidth, mainHeight, opacity);
            }
            if (!secondVideo.isBlack()) {
                graphics.drawImage(secondVideo.getImage(), SMALL_X, SMALL_Y, secondWidth, secondHeight, opacity);
            }
        }

        private void drawStatus() {
            String name = team.getShortName();

            PlateStyle teamColor = QueueStylesheet.name;

            int x = TEAM_PANE_X;
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

            ContestInfo contestInfo = EventsLoader.getInstance().getContestData();

            int n = contestInfo.getProblemsNumber();
            int[] status = new int[n];
            for (int i = 0; i < n; i++) {
                for (RunInfo run : team.getRuns()[i]) {
                    if (run.isAccepted()) {
                        status[i] = Math.max(status[i], 3);
                    }
                    if (!run.isJudged()) {
                        status[i] = Math.max(status[i], 2);
                    }
                    status[i] = Math.max(status[i], 1);
                }
            }
            for (int i = 0; i < n; i++) {
                int x1 = i * teamPaneWidth / n;
                int x2 = (i + 1) * teamPaneWidth / n;

                PlateStyle resultColor;
                if (status[i] == 3) {
                    resultColor = BigStandingsStylesheet.acProblem;
                } else if (status[i] == 2) {
                    resultColor = BigStandingsStylesheet.udProblem;
                } else if (status[i] == 1) {
                    resultColor = BigStandingsStylesheet.waProblem;
                } else {
                    resultColor = BigStandingsStylesheet.noProblem;
                }
                applyStyle(resultColor);
                drawRectangleWithText(
                        contestInfo.problems.get(i).letter,
                        TEAM_PANE_X + x1, PROBLEMS_Y,
                        x2 - x1, TEAM_PANE_HEIGHT, PlateStyle.Alignment.CENTER);
            }
        }

        @Override
        protected CachedData getCorrespondingData(Data data) {
            return null;
        }

        public void stop() {
            System.err.println("Stop");
            if (mainVideo != null) mainVideo.stop();
            if (secondVideo != null) secondVideo.stop();
        }
    }

}
