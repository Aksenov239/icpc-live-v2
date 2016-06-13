package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.datapassing.StandingsData;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.ProblemInfo;
import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;
import ru.ifmo.acm.events.WF.WFContestInfo;
import ru.ifmo.acm.events.WF.WFEventsLoader;
import ru.ifmo.acm.events.WF.WFRunInfo;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import ru.ifmo.acm.backend.player.widgets.stylesheets.*;

/**
 * @author: pashka
 */
public class BigStandingsWidget extends Widget {
    private static final double V = 0.01;
    private static final int STAR_SIZE = 5;
    private static int STANDING_TIME = 5000;
    private static int TOP_PAGE_STANDING_TIME = 10000;
    private static final int MOVING_TIME = 500;
    public static int PERIOD = STANDING_TIME + MOVING_TIME;

    protected static final double TOTAL_WIDTH = 1.8;
    protected static final double PENALTY_WIDTH = 2.4;

    private final int plateHeight;
    private final int spaceY;
    private final int spaceX;

    private final int nameWidth;
    private final int rankWidth;
    private final int totalWidth;
    private final int penaltyWidth;

    public int length;

    private final Font font;

    int timer;
    int start;
    final int baseX;
    int baseY;
    final int width;
    final boolean controlled;
    final int teamsOnPage;

    private ContestInfo contestData;
    private StandingsData.OptimismLevel optimismLevel = StandingsData.OptimismLevel.NORMAL;

    double[] currentTeamPositions;
    double[] desiredTeamPositions;

    long blinkingTime;

    public BigStandingsWidget(int baseX, int baseY, int width, int plateHeight, long updateWait, int teamsOnPage, boolean controlled) {
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

        spaceX = (int) Math.round(plateHeight * SPACE_X);
        spaceY = (int) Math.round(plateHeight * SPACE_Y);

        nameWidth = (int) Math.round(NAME_WIDTH * plateHeight);
        rankWidth = (int) Math.round(RANK_WIDTH * plateHeight);
        totalWidth = (int) Math.round(TOTAL_WIDTH * plateHeight);
        penaltyWidth = (int) Math.round(PENALTY_WIDTH * plateHeight);

        this.updateWait = updateWait;

        font = Font.decode("Open Sans " + (int) (plateHeight * 0.7));

        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/mainscreen.properties"));
        } catch (IOException e) {
            log.error("error", e);
        }
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
        if (data.standingsData.isStandingsVisible() && data.standingsData.isBig()) {
            if (lastChange != data.standingsData.getStandingsTimestamp()) {
                if (!isVisible()) {
                    setState(data.standingsData.getStandingsType());
                }
            }
        } else {
            setVisible(false);
        }
        lastChange = data.standingsData.getStandingsTimestamp();
        optimismLevel = data.standingsData.optimismLevel;
    }

    List<Point> stars = new ArrayList<>();

    @Override
    public void paintImpl(Graphics2D g, int width, int height) {
        contestData = Preparation.eventsLoader.getContestData();
        if (contestData == null) {
            return;
        }

        if (controlled) {
            update();
        }

        int dt = updateVisibilityState();

        if (!isVisible() && visibilityState == 0) {
            currentTeamPositions = null;
            return;
        }

        g = (Graphics2D) g.create();
        g.translate(baseX, baseY);

        TeamInfo[] standings;
        if (contestData instanceof WFContestInfo) {
            standings = ((WFContestInfo) contestData).getStandings(optimismLevel);
        } else {
            standings = contestData.getStandings();
        }


        if (contestData == null || standings == null) return;

        RunInfo[] firstSolved = new RunInfo[contestData.problemNumber];
        for (TeamInfo team : standings) {
            for (int p = 0; p < firstSolved.length; p++) {
                for (RunInfo run : team.getRuns()[p]) {
                    if ("AC".equals(run.getResult()) &&
                            (firstSolved[p] == null || run.getTime() < firstSolved[p].getTime())) {
                        firstSolved[p] = run;
                        break;
                    }
                }
            }
        }

        length = Math.min(contestData.getTeamsNumber(), standings.length);

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

            drawHead(g, spaceX, 0, firstSolved);
            g = (Graphics2D) g.create();
            int initY = plateHeight + BIG_SPACE_COUNT * spaceY;
            g.clip(new Rectangle(
                    -plateHeight,
                    initY - STAR_SIZE * 2,
                    this.width + 2 * plateHeight,
                    (spaceY + plateHeight) * teamsOnPage + STAR_SIZE * 2));

            int lastProblems = -1;
            boolean bright = true;

            stars.clear();

            for (int i = standings.length - 1; i >= 0; i--) {
                TeamInfo teamInfo = standings[i];
                if (teamInfo.getSolvedProblemsNumber() != lastProblems) {
                    lastProblems = teamInfo.getSolvedProblemsNumber();
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
                    drawFullTeamPane(g, teamInfo, spaceX, initY + (int) (yy * (plateHeight + spaceY)), bright, firstSolved);
                }
            }

            for (Point star : stars) {
                drawStar(g, star.x, star.y, STAR_SIZE);
            }

        } else {
            timer = -TOP_PAGE_STANDING_TIME;
            start = 0;
        }
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return data.standingsData;
    }

    private void drawHead(Graphics2D g, int x, int y, RunInfo[] firstSolved) {
        g.setFont(font);
        int problemWidth = problemWidth(firstSolved.length);

        PlateStyle heading = BigStandingsStylesheet.heading;

        String headingText = "Current Standings";

        if (contestData.getCurrentTime() > WFEventsLoader.FREEZE_TIME) {
            if (optimismLevel == StandingsData.OptimismLevel.OPTIMISTIC) {
                heading = BigStandingsStylesheet.optimisticHeading;
                headingText = "Optimistic Standings";
            } else {
                heading = BigStandingsStylesheet.frozenHeading;
                headingText = "Frozen Standings";
            }
        }

        drawTextInRect(g, headingText, x, y,
                rankWidth + nameWidth + spaceX, plateHeight,
                POSITION_CENTER, heading.background, heading.text, visibilityState, WidgetAnimation.UNFOLD_ANIMATED);
        x += rankWidth + nameWidth + 2 * spaceX;
        for (int i = 0; i < firstSolved.length; i++) {
            ProblemInfo problem = contestData.problems.get(i);
            PlateStyle color = (contestData.firstSolvedRun()[i] == null) ?
                    ((firstSolved[i] != null) ? BigStandingsStylesheet.udProblem : BigStandingsStylesheet.noProblem) :
                    BigStandingsStylesheet.acProblem;
            drawTextInRect(g, problem.letter, x, y, problemWidth, plateHeight,
                    POSITION_CENTER, color.background, color.text, visibilityState, WidgetAnimation.UNFOLD_ANIMATED);
            x += problemWidth + spaceX;
        }
    }

    private void drawFullTeamPane(Graphics2D g, TeamInfo team, int x, int y, boolean bright, RunInfo[] firstSolved) {
        Font font = this.font;
        g.setFont(font);
        Color color = getTeamRankColor(team);
        drawTextInRect(g, "" + Math.max(team.getRank(), 1), x, y,
                rankWidth, plateHeight, POSITION_CENTER,
                color, Color.white, visibilityState, WidgetAnimation.UNFOLD_ANIMATED);

        x += rankWidth + spaceX;

        PlateStyle nameStyle = BigStandingsStylesheet.name;
        if (bright) {
            nameStyle = nameStyle.brighter();
        }
        String name = team.getShortName();//getShortName(g, teamId.getShortName());
        drawTextInRect(g, name, x, y,
                nameWidth, plateHeight, POSITION_LEFT,
                nameStyle.background, nameStyle.text, visibilityState, WidgetAnimation.UNFOLD_ANIMATED);

        x += nameWidth + spaceX;

        int problemWidth = problemWidth(contestData.getProblemsNumber());

        for (int i = 0; i < contestData.getProblemsNumber(); i++) {
            String status = team.getShortProblemState(i);
            PlateStyle statusColor =
                    status.startsWith("+") ? BigStandingsStylesheet.acProblem :
                            status.startsWith("?") ? BigStandingsStylesheet.udProblem :
                                    status.startsWith("-") ? BigStandingsStylesheet.waProblem :
                                            BigStandingsStylesheet.noProblem;
            if (team.isReallyUnknown(i)) {
                statusColor = BigStandingsStylesheet.udProblem;
//                if (optimismLevel == StandingsData.OptimismLevel.OPTIMISTIC) {
//                    statusColor = YELLOW_GREEN_COLOR;
//                } else {
//                    statusColor = YELLOW_RED_COLOR;
//                }
            }
//            if (bright && statusColor == MAIN_COLOR) statusColor = statusColor.brighter();

            if(bright && statusColor == BigStandingsStylesheet.noProblem) {
                statusColor = statusColor.brighter();
            }

            if (status.startsWith("-")) status = "\u2212" + status.substring(1);
            boolean isBlinking = team.getLastRun(i) != null && (System.currentTimeMillis() - ((WFRunInfo) team.getLastRun(i)).timestamp * 1000) < blinkingTime;
            drawTextInRect(g, status, x, y,
                    problemWidth, plateHeight, POSITION_CENTER, statusColor.background, statusColor.text, visibilityState, false, true, WidgetAnimation.UNFOLD_ANIMATED, isBlinking);

            RunInfo firstSolvedRun = firstSolved[i];
            if (firstSolvedRun != null && firstSolvedRun.getTeamId() == team.getId() && visibilityState >= 0.5) {
                stars.add(new Point(x + problemWidth - STAR_SIZE, y + STAR_SIZE));
            }
            x += problemWidth + spaceX;
        }

        g.setFont(font);
        PlateStyle problemsColor = BigStandingsStylesheet.problems;
        if (bright) {
            problemsColor = problemsColor.brighter();
        }
        drawTextInRect(g, "" + team.getSolvedProblemsNumber(), x, y, totalWidth,
                plateHeight, POSITION_CENTER, problemsColor.background, problemsColor.text, visibilityState, WidgetAnimation.UNFOLD_ANIMATED);
        x += totalWidth + spaceX;
        PlateStyle penaltyColor = BigStandingsStylesheet.penalty;
        if (bright) {
            penaltyColor = penaltyColor.brighter();
        }
        drawTextInRect(g, "" + team.getPenalty(), x, y, penaltyWidth,
                plateHeight, POSITION_CENTER, penaltyColor.background, penaltyColor.text, visibilityState, WidgetAnimation.UNFOLD_ANIMATED);
    }

    private int problemWidth(int problemsNumber) {
        return (int) Math.round((width - rankWidth - nameWidth - totalWidth - penaltyWidth - 3 * spaceX) * 1.0 / problemsNumber - spaceX);
    }

    public void alignBottom(int y) {
        baseY = y - teamsOnPage * (plateHeight + spaceY) - BIG_SPACE_COUNT * spaceY - plateHeight;
    }
}
