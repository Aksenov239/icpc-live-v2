package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.datapassing.StandingsData;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.ProblemInfo;
import ru.ifmo.acm.events.TeamInfo;
import ru.ifmo.acm.events.WF.WFContestInfo;

import java.awt.*;

/**
 * @author: pashka
 */
public class BigStandingsWidget extends Widget {
    private static final double V = 0.01;
    private static int STANDING_TIME = 5000;
    private static int TOP_PAGE_STANDING_TIME = 10000;
    private static final int MOVING_TIME = 500;
    private static final double SPACE_VS_PLATE = 0.1;
    private static final int BIG_SPACE_COUNT = 6;
    public static int PERIOD = STANDING_TIME + MOVING_TIME;
    public final static int TEAMS_ON_PAGE = 16;

    private final int plateWidth;
    private final double plateHeight;
    private final int spaceY;
    private final int spaceX;
    private final int movingHeight;
    public int length;

    private final Font font;

    int timer;
    int start;
    final int baseX, baseY, totalHeight, totalWidth;
    final boolean controlled;

    private ContestInfo contestData;

    private StandingsData.OptimismLevel optimismLevel = StandingsData.OptimismLevel.NORMAL;

    public BigStandingsWidget(int x, int y, int width, int height, long updateWait, boolean controlled) {
        super(updateWait);
        last = System.currentTimeMillis();
        baseX = x;
        baseY = y;
        totalWidth = width;
        totalHeight = height;
        this.controlled = controlled;
        if (!controlled) {
            setVisibilityState(1);
            setVisible(true);
        }

        plateWidth = width;
        spaceX = 0;
        double total = (TEAMS_ON_PAGE + 1) * (1 + SPACE_VS_PLATE) + (BIG_SPACE_COUNT - 1) * SPACE_VS_PLATE;
        plateHeight = height / total;
        spaceY = (int) (plateHeight * SPACE_VS_PLATE);

        //totalHeight = (int) plateHeight * (TEAMS_ON_PAGE + 1) + TEAMS_ON_PAGE * paceY;

        movingHeight = (int) (plateHeight * ((1 + SPACE_VS_PLATE) * TEAMS_ON_PAGE + SPACE_VS_PLATE));

        this.updateWait = updateWait;

        font = Font.decode("Open Sans " + (int) (plateHeight * 0.7));
    }

    public void setState(StandingsData.StandingsType type) {
        switch (type) {
            case ONE_PAGE:
                length = Math.min(16, contestData.getTeamsNumber());
                start = 0;
                timer = -Integer.MAX_VALUE;
                break;
            case TWO_PAGES:
                TOP_PAGE_STANDING_TIME = 10000;
                STANDING_TIME = 10000;
                PERIOD = STANDING_TIME + MOVING_TIME;
                length = Math.min(32, contestData.getTeamsNumber());
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
        int pages = teamNumber / TEAMS_ON_PAGE;
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
//        System.err.println("UPDATE " + data.standingsData.isStandingsVisible() + " " + data.standingsData.isBig());
        if (data.standingsData.isStandingsVisible() && data.standingsData.isBig()) {
            if (lastChange != data.standingsData.getStandingsTimestamp()) {
                if (!isVisible()) {
                    //  lastVisibleChange = System.currentTimeMillis();
                    setState(data.standingsData.getStandingsType());
                }
            }
        } else {
            setVisible(false);
        }
        lastChange = data.standingsData.getStandingsTimestamp();
        optimismLevel = data.standingsData.optimismLevel;
    }

    double[] currentTeamPositions;
    double[] desiredTeamPositions;

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

        if (!isVisible() && visibilityState == 0)
            return;

        g = (Graphics2D) g.create();
        g.translate(baseX, baseY);
        g.clip(new Rectangle(-10, 0, totalWidth + 10, totalHeight));
        TeamInfo[] standings;
        if (contestData instanceof WFContestInfo) {
            standings = ((WFContestInfo) contestData).getStandings(optimismLevel);
        } else {
            standings = contestData.getStandings();
        }

        if (contestData == null || standings == null) return;
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
//                    System.err.println("Old: " + start);
                    start += TEAMS_ON_PAGE;
//                    System.err.println("New: " + start);
                    if (start >= length && !controlled) {
                        start = 0;
                        timer = -TOP_PAGE_STANDING_TIME + STANDING_TIME;
                    }
                }
            }
            double start = this.start;
            if (timer >= STANDING_TIME) {
//                System.err.println(start + " " + length);
                if (start + TEAMS_ON_PAGE >= length && controlled) {
                    setVisible(false);
                } else {
                    double t = (timer - STANDING_TIME) * 1.0 / MOVING_TIME;
                    start -= ((2 * t * t * t - 3 * t * t) * TEAMS_ON_PAGE);
                }
            }

            drawHead(g, spaceX, 0, contestData.getProblemsNumber());
            g = (Graphics2D) g.create();
            int initY = (int) (plateHeight + BIG_SPACE_COUNT * spaceY);
            g.clip(new Rectangle(0, initY, totalWidth, totalHeight - initY));

            int lastProblems = -1;
            boolean bright = true;
            //TeamInfo[] standings = contestData.getStandings();
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
                    if (currentTeamPositions[id] > start + TEAMS_ON_PAGE && desiredTeamPositions[id] < start + TEAMS_ON_PAGE) {
                        currentTeamPositions[id] = start + TEAMS_ON_PAGE;
                    }
                }
                double yy = currentTeamPositions[id] - start;
                if (yy > -1 && yy < TEAMS_ON_PAGE) {
                    drawFullTeamPane(g, teamInfo, spaceX, initY + (int) (yy * (plateHeight + spaceY)), bright);
                }
            }

//            if (start < length) {
//                drawTeams(g, spaceX, (int) (plateHeight + initY + currentStart), contestData, start);
//            }
//            if (start + TEAMS_ON_PAGE < length || !controlled) {
//                int nextPage = start + TEAMS_ON_PAGE < length ? start + TEAMS_ON_PAGE : 0;
//                drawTeams(g, spaceX, (int) (plateHeight + initY + currentStart + movingHeight), contestData, nextPage);
//            }
        } else {
            timer = -TOP_PAGE_STANDING_TIME;
            start = 0;
        }
    }

    private static final double SPLIT_WIDTH = 0.005;
    private static final double RANK_WIDTH = 0.04;
    private static final double NAME_WIDTH = 0.15;
    private static final double TOTAL_WIDTH = 0.04;
    private static final double PENALTY_WIDTH = 0.08;

    private void drawHead(Graphics2D g, int x, int y, int problemsNumber) {
        g.setFont(font);
        drawTextInRect(g, "Current Standings", x, y,
                (int) (plateWidth * (RANK_WIDTH + NAME_WIDTH + SPLIT_WIDTH)),
                (int) plateHeight,
                POSITION_CENTER, ACCENT_COLOR, Color.white, visibilityState);
        x += (int) (plateWidth * (RANK_WIDTH + NAME_WIDTH + 2 * SPLIT_WIDTH));
        int PROBLEM_WIDTH = (int) ((plateWidth - x - plateWidth * (TOTAL_WIDTH + SPLIT_WIDTH + PENALTY_WIDTH)) / problemsNumber - plateWidth * SPLIT_WIDTH);
        for (int i = 0; i < problemsNumber; i++) {
            ProblemInfo problem = contestData.problems.get(i);
//            drawTextInRect(g, problem.letter, x, y, PROBLEM_WIDTH, (int) plateHeight,
//                    POSITION_CENTER, problem.color, textColor(problem.color), visibilityState);
            drawTextInRect(g, problem.letter, x, y, PROBLEM_WIDTH, (int) plateHeight,
                    POSITION_CENTER, MAIN_COLOR, Color.white, visibilityState);
            x += (int) (plateWidth * SPLIT_WIDTH) + PROBLEM_WIDTH;
        }
    }

    private Color textColor(Color color) {
        double c = (0.299*color.getRed() + 0.587*color.getGreen() + 0.114*color.getBlue());
        if (c > 200) {
            return Color.black;
        } else {
            return Color.white;
        }
    }

    private void drawFullTeamPane(Graphics2D g, TeamInfo team, int x, int y, boolean bright) {

        Color mainColor = MAIN_COLOR;
        if (bright) mainColor = mainColor.brighter();

        Font font = this.font;
        g.setFont(font);
        drawTextInRect(g, "" + Math.max(team.getRank(), 1), x, y,
                (int) (plateWidth * RANK_WIDTH), (int) plateHeight, POSITION_CENTER,
                ACCENT_COLOR, Color.white, visibilityState);

        x += (int) (plateWidth * (RANK_WIDTH + SPLIT_WIDTH));

        String name = team.getShortName();//getShortName(g, team.getShortName());
        drawTextInRect(g, name, x, y,
                (int) (plateWidth * NAME_WIDTH), (int) plateHeight, POSITION_LEFT,
                mainColor, Color.white, visibilityState);

        x += (int) (plateWidth * (NAME_WIDTH + SPLIT_WIDTH));

        int PROBLEM_WIDTH = (int) ((plateWidth - x - plateWidth * (TOTAL_WIDTH + SPLIT_WIDTH + PENALTY_WIDTH)) / contestData.getProblemsNumber() - plateWidth * SPLIT_WIDTH);
        for (int i = 0; i < contestData.getProblemsNumber(); i++) {
            String status = team.getShortProblemState(i);
            Color statusColor = status.startsWith("+") ? GREEN_COLOR :
                    status.startsWith("?") ? YELLOW_COLOR :
                            status.startsWith("-") ? RED_COLOR :
                                    MAIN_COLOR;
            if (bright) statusColor = statusColor.brighter();

            if (status.startsWith("-")) status = "\u2212" + status.substring(1);
            drawTextInRect(g, status, x, y,
                    PROBLEM_WIDTH, (int) plateHeight, POSITION_CENTER, statusColor, Color.WHITE, visibilityState);
            x += PROBLEM_WIDTH + (int) (plateWidth * SPLIT_WIDTH);
        }

        g.setFont(font);
        drawTextInRect(g, "" + team.getSolvedProblemsNumber(), x, y, (int) (plateWidth * TOTAL_WIDTH),
                (int) plateHeight, POSITION_CENTER, mainColor, Color.white, visibilityState);
        x += (int) (plateWidth * (TOTAL_WIDTH + SPLIT_WIDTH));
        drawTextInRect(g, "" + team.getPenalty(), x, y, (int) (plateWidth * PENALTY_WIDTH),
                (int) plateHeight, POSITION_CENTER, mainColor, Color.white, visibilityState);
    }
}
