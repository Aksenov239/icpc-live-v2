package org.icpclive.backend.player.widgets;

import org.icpclive.Config;
import org.icpclive.backend.Preparation;
import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.player.widgets.stylesheets.BigStandingsStylesheet;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.backend.player.widgets.stylesheets.TeamPaneStylesheet;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.datapassing.StandingsData;
import org.icpclive.events.ContestInfo;
import org.icpclive.events.PCMS.ioi.IOIPCMSTeamInfo;
import org.icpclive.events.ProblemInfo;
import org.icpclive.events.RunInfo;
import org.icpclive.events.TeamInfo;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/**
 * Full scale standings
 */
public class IOIBigStandingsWidget extends Widget {
    private static final double V = 0.01;
    private static final int STAR_SIZE = 5;

    public static final String CURRENT_STANDINGS = "CURRENT STANDINGS";

    private static int STANDING_TIME = 10000;
    private static int TOP_PAGE_STANDING_TIME = 20000;
    private static final int MOVING_TIME = 500;
    private static int PERIOD = STANDING_TIME + MOVING_TIME;

    private static final double TOTAL_WIDTH = 1.8;

    private final int plateHeight;

    private final int nameWidth;
    private final int rankWidth;
    private final int totalWidth;

    public int length;

    private int timer;
    private int start;
    private final int baseX;
    private int baseY;
    private final int width;
    private final boolean controlled;
    private final int teamsOnPage;

    private ContestInfo contestData;
    private StandingsData.OptimismLevel optimismLevel = StandingsData.OptimismLevel.NORMAL;
    private String region = "all";

    private double[] currentTeamPositions;
    private double[] desiredTeamPositions;

    private long blinkingTime;

    private static int NAME_WIDTH = 11;

    public IOIBigStandingsWidget(int baseX, int baseY, int width, int plateHeight, long updateWait, int teamsOnPage, boolean controlled) throws IOException {
        super(updateWait);
        last = System.currentTimeMillis();

        this.baseX = baseX;
        this.baseY = baseY;
        this.width = width;
        this.plateHeight = plateHeight;
        this.teamsOnPage = teamsOnPage;
        this.controlled = controlled;

        if (!controlled) {
            setVisibilityState(1);
            setVisible(true);
        }

        nameWidth = (int) Math.round(NAME_WIDTH * plateHeight);
        rankWidth = (int) Math.round(RANK_WIDTH * plateHeight);
        totalWidth = (int) Math.round(TOTAL_WIDTH * plateHeight);

        this.updateWait = updateWait;

        setFont(Font.decode(MAIN_FONT + " " + (int) (plateHeight * 0.7)));

        Properties properties = Config.loadProperties("mainscreen");
        blinkingTime = Long.parseLong(properties.getProperty("standings.blinking.time"));
    }

    public void setState(StandingsData.StandingsType type) {
        switch (type) {
            case ONE_PAGE:
                length = Math.min(teamsOnPage, contestData.getTeamsNumber());
                start = 0;
                timer = -Integer.MAX_VALUE;
                break;
            case TWO_PAGES:
                TOP_PAGE_STANDING_TIME = 10000;
                STANDING_TIME = 10000;
                PERIOD = STANDING_TIME + MOVING_TIME;
                length = Math.min(teamsOnPage * 2, contestData.getTeamsNumber());
                start = 0;
                timer = 0;
                break;
            case ALL_PAGES:
                TOP_PAGE_STANDING_TIME = 10000;
                STANDING_TIME = 5000;
                PERIOD = STANDING_TIME + MOVING_TIME;
                length = contestData.getTeamsNumber();
                start = 0;
                timer = -TOP_PAGE_STANDING_TIME + STANDING_TIME;
        }
        setVisible(true);
    }

    public static long totalTime(StandingsData.StandingsType type, int teamNumber) {
        int pages = teamNumber / 20;
        switch (type) {
            case ONE_PAGE:
                return Integer.MAX_VALUE;
            case TWO_PAGES:
                return TOP_PAGE_STANDING_TIME + STANDING_TIME + MOVING_TIME;
            default:
                return (pages - 1) * (STANDING_TIME + MOVING_TIME) + TOP_PAGE_STANDING_TIME;
        }
    }

    private long lastChange;

    protected void updateImpl(Data data) {
        contestData = Preparation.eventsLoader.getContestData();
        if (data.standingsData.isStandingsVisible() && data.standingsData.isBig()) {
            if (lastChange != data.standingsData.getStandingsTimestamp()) {
                if (!isVisible()) {
                    setState(data.standingsData.getStandingsType());
                }
            }
            optimismLevel = data.standingsData.optimismLevel;
            region = data.standingsData.region;
        } else {
            setVisible(false);
        }
        lastChange = data.standingsData.getStandingsTimestamp();
    }

    @Override
    public void paintImpl(AbstractGraphics g, int width, int height) {
        super.paintImpl(g, width, height);
        contestData = Preparation.eventsLoader.getContestData();
        if (contestData == null) {
            return;
        }

        if (!isVisible() && visibilityState == 0) {
            currentTeamPositions = null;
            return;
        }

        graphics.translate(baseX, baseY);

        TeamInfo[] standings;
        standings = contestData.getStandings(region, optimismLevel);
        length = standings.length;

        if (contestData == null || standings == null) return;

        if (desiredTeamPositions == null || desiredTeamPositions.length != contestData.getTeamsNumber() + 1) {
            desiredTeamPositions = new double[contestData.getTeamsNumber() + 1];
        }
        {
            int i = 0;
            for (TeamInfo teamInfo : standings) {
                desiredTeamPositions[teamInfo.getId()] = i;
                i++;
            }
        }
        if (currentTeamPositions == null || currentTeamPositions.length != contestData.getTeamsNumber() + 1) {
            currentTeamPositions = desiredTeamPositions.clone();
        }

        if (visibilityState > 0) {
            if (isVisible()) {
                timer = timer + dt;
                if (timer >= PERIOD) {
                    timer -= PERIOD;
                    start += teamsOnPage;
                    if (start >= length && !controlled) {
                        start = 0;
                        timer = -TOP_PAGE_STANDING_TIME + STANDING_TIME;
                    }
                }
            }
            double start = this.start;
            if (timer >= STANDING_TIME) {
                if (start + teamsOnPage >= length && controlled) {
                    setVisible(false);
                } else {
                    double t = (timer - STANDING_TIME) * 1.0 / MOVING_TIME;
                    start -= ((2 * t * t * t - 3 * t * t) * teamsOnPage);
                }
            }

            int initY = plateHeight;

            drawHead(0, 0);

            setGraphics(graphics.create());
            graphics.clip(-plateHeight,
                    initY,
                    this.width + 2 * plateHeight,
                    plateHeight * teamsOnPage);

            int lastScore = -1;
            boolean bright = true;

            boolean odd = true;
            for (int i = standings.length - 1; i >= 0; i--) {
                odd = !odd;
                IOIPCMSTeamInfo teamInfo = (IOIPCMSTeamInfo)standings[i];
                if (teamInfo.getScore() != lastScore) {
                    lastScore = teamInfo.getScore();
                    bright = !bright;
                }
                int id = teamInfo.getId();
                double dp = dt * V;
                if (Math.abs(currentTeamPositions[id] - desiredTeamPositions[id]) < dp) {
                    currentTeamPositions[id] = desiredTeamPositions[id];
                } else {
                    if (desiredTeamPositions[id] < currentTeamPositions[id]) {
                        currentTeamPositions[id] -= dp;
                    } else {
                        currentTeamPositions[id] += dp;
                    }
                    if (currentTeamPositions[id] < start - 1 && desiredTeamPositions[id] > start - 1) {
                        currentTeamPositions[id] = start - 1;
                    }
                    if (currentTeamPositions[id] > start + teamsOnPage && desiredTeamPositions[id] < start + teamsOnPage) {
                        currentTeamPositions[id] = start + teamsOnPage;
                    }
                }
                double yy = currentTeamPositions[id] - start;
                if (yy > -1 && yy < teamsOnPage) {
                    drawFullTeamPane(teamInfo, 0, initY + (int) (yy * plateHeight), bright, odd);
                }
            }
        }
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return data.standingsData;
    }

    private void drawHead(int x, int y) {
        int problemWidth = problemWidth(contestData.problems.size());

        PlateStyle heading = BigStandingsStylesheet.heading;

        String headingText = !region.equals("all") ? region : CURRENT_STANDINGS;

        applyStyle(heading);
        drawRectangleWithText(headingText, x, y, rankWidth + nameWidth, plateHeight, PlateStyle.Alignment.CENTER);
        x += rankWidth + nameWidth;

        applyStyle(BigStandingsStylesheet.noProblem);
        drawRectangleWithText("\u03A3", x, y, totalWidth, plateHeight, PlateStyle.Alignment.CENTER);
        x += totalWidth;

        for (int i = 0; i < contestData.problems.size(); i++) {
            ProblemInfo problem = contestData.problems.get(i);
            drawProblemPane(problem, x, y, problemWidth, plateHeight);
            x += problemWidth;
        }

    }

    private void drawFullTeamPane(TeamInfo team, int x, int y, boolean bright, boolean odd) {
        PlateStyle rankStyle = getTeamRankColor(team);
        applyStyle(rankStyle);
        drawRectangleWithText("" + Math.max(team.getRank(), 1)// comment to hide global ranks
, x, y, rankWidth, plateHeight, PlateStyle.Alignment.CENTER, false, false);

        x += rankWidth;

        PlateStyle nameStyle = BigStandingsStylesheet.name;
        if (((IOIPCMSTeamInfo)team).delay != 0) {
            nameStyle = BigStandingsStylesheet.delay;
        }
        if (bright) {
            nameStyle = nameStyle.brighter();
        }
        String name = team.getShortName();
        applyStyle(nameStyle);
        if (odd) {
            setMaximumOpacity(maximumOpacity * .9);
        }
        drawRectangleWithText(name, x, y, nameWidth, plateHeight, PlateStyle.Alignment.LEFT);

        x += nameWidth;

        PlateStyle problemsColor = BigStandingsStylesheet.problems;
        if (bright) {
            problemsColor = problemsColor.brighter();
        }
        setBackgroundColor(problemsColor.background);
        drawRectangleWithText("" + ((IOIPCMSTeamInfo) team).score, x, y, totalWidth, plateHeight, PlateStyle.Alignment.CENTER);

        x += totalWidth;

        int problemWidth = problemWidth(contestData.getProblemsNumber());

        for (int i = 0; i < contestData.getProblemsNumber(); i++) {
            String status = team.getShortProblemState(i);
            if (status.length() == 0) status = ".";

            int score = status.equals("?") || status.equals(".") || status.equals("") ? 0 :
                    Integer.parseInt(status);

            PlateStyle statusColor =
                        status.startsWith("?") ? BigStandingsStylesheet.udProblem :
                                    status.startsWith(".") ? BigStandingsStylesheet.noProblem :
                                            PlateStyle.mix(BigStandingsStylesheet.ioiFull,
                                                    BigStandingsStylesheet.ioiZero,
                                                    1 - (1 - 1. * score / 100) * (1 - 1. * score / 100));
            if (team.isReallyUnknown(i)) {
                statusColor = BigStandingsStylesheet.udProblem;
            }
            if (bright && statusColor == BigStandingsStylesheet.noProblem) {
                applyStyle(statusColor.brighter());
            } else {
                applyStyle(statusColor);
            }

            if (odd) {
                setMaximumOpacity(maximumOpacity * .9);
            }

//            if (odd && status.length() == 0) {
//                setMaximumOpacity(maximumOpacity * .9);
//            }
//            if (odd && statusColor == BigStandingsStylesheet.noProblem) {
//                setMaximumOpacity(maximumOpacity * .9);
//            }


            boolean blinking = team.getLastRun(i) != null && (contestData.getCurrentTime() <= team.getLastRun(i).getLastUpdateTime() + blinkingTime);

            drawRectangleWithText(status, x, y, problemWidth, plateHeight, PlateStyle.Alignment.CENTER, blinking);

            x += problemWidth;
        }
    }

    private int problemWidth(int problemsNumber) {
        return (int) Math.round((width - rankWidth - nameWidth - totalWidth) * 1.0 / problemsNumber - 0);
    }

    public void alignBottom(int y) {
        baseY = y - teamsOnPage * plateHeight - plateHeight;
    }
}
