package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.datapassing.StandingsData;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;

import java.awt.*;
import java.util.Collection;

/**
 * @author: pashka
 */
public class BigStandingsWidget extends Widget {
    private static int STANDING_TIME = 5000;
    private static int TOP_PAGE_STANDING_TIME = 10000;
    private static final int MOVING_TIME = 500;
    private static final double SPACE_VS_PLATE = 0.1;
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
        double total = (TEAMS_ON_PAGE + 1) * (1 + SPACE_VS_PLATE) + SPACE_VS_PLATE;
        plateHeight = height / total;
        spaceY = (int) (plateHeight * SPACE_VS_PLATE);

        movingHeight = (int) (plateHeight * ((1 + SPACE_VS_PLATE) * TEAMS_ON_PAGE + SPACE_VS_PLATE));

        this.updateWait = updateWait;

        font = Font.decode("Open Sans " + (int) (plateHeight * 0.7));
    }

    public void setState(StandingsData.StandingsType type) {
        switch (type) {
            case ONE_PAGE:
                length = Math.min(12, contestData.getTeamsNumber());
                start = 0;
                timer = -Integer.MAX_VALUE;
                break;
            case TWO_PAGES:
                TOP_PAGE_STANDING_TIME = 10000;
                STANDING_TIME = 10000;
                PERIOD = STANDING_TIME + MOVING_TIME;
                length = Math.min(24, contestData.getTeamsNumber());
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

    public static long totalTime(long type, int teamNumber) {
        int pages = teamNumber / TEAMS_ON_PAGE;
        if (type == 0) {
            return Integer.MAX_VALUE;
        } else if (type == 1) {
            return 2 * STANDING_TIME + MOVING_TIME;
        } else {
            return (pages - 1) * (STANDING_TIME + MOVING_TIME) + TOP_PAGE_STANDING_TIME;
        }
    }

    protected void updateImpl(Data data) {
        if (data.standingsData.isStandingsVisible() && data.standingsData.isBig()) {
            if (!isVisible() && contestData != null) {
                //  lastVisibleChange = System.currentTimeMillis();
                setState(data.standingsData.getStandingsType());
            }
        } else {
            setVisible(false);
        }
        lastUpdate = System.currentTimeMillis();
    }

    @Override
    public void paintImpl(Graphics2D g, int width, int height) {
        update();
        g = (Graphics2D) g.create();
        g.translate(baseX, baseY);
        g.clip(new Rectangle(-10, 0, totalWidth + 10, totalHeight));
        if (controlled) {
            update();
        }
        contestData = Preparation.eventsLoader.getContestData();
        if (contestData == null || contestData.getStandings() == null) return;
        length = Math.min(contestData.getTeamsNumber(), contestData.getStandings().length);

        int dt = updateVisibilityState();

        if (visibilityState > 0) {
            if (isVisible()) {
                timer = timer + dt;
                if (timer >= PERIOD) {
                    timer -= PERIOD;
                    start += TEAMS_ON_PAGE;
                    if (start >= length && !controlled) {
                        start = 0;
                        timer = -TOP_PAGE_STANDING_TIME + STANDING_TIME;
                    }
                }
            }
            int dy = 0;
            if (timer >= STANDING_TIME) {
                if (start + TEAMS_ON_PAGE >= length && controlled) {
                    setVisible(false);
                } else {
                    double t = (timer - STANDING_TIME) * 1.0 / MOVING_TIME;
                    dy = (int) ((2 * t * t * t - 3 * t * t) * movingHeight);
                }
            }

            int initY = 5 * spaceY;
            if (start < length) {
                drawTeams(g, spaceX, (int) (plateHeight + initY + dy), contestData, start);
            }
            if (start + TEAMS_ON_PAGE < length || !controlled) {
                int nextPage = start + TEAMS_ON_PAGE < length ? start + TEAMS_ON_PAGE : 0;
                drawTeams(g, spaceX, (int) (plateHeight + initY + dy + movingHeight), contestData, nextPage);
            }
            drawHead(g, spaceX, 0, contestData.getProblemsNumber());
        } else {
            timer = -TOP_PAGE_STANDING_TIME;
            start = 0;
        }
    }

    private void drawTeams(Graphics2D g, int x, int y, ContestInfo contestData, int start) {
        for (int i = 0; i < TEAMS_ON_PAGE; i++) {
            if (start + i >= length)
                break;
            TeamInfo team = contestData.getStandings()[start + i];
            int dx = 0;
            int dy = (int) (i * (plateHeight + spaceY));
            if (team != null && y + dy >= spaceY) {
                drawFullTeamPane(g, team, x + dx, y + dy);
            }
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
            drawTextInRect(g, "" + (char) ('A' + i), x, y, PROBLEM_WIDTH, (int) plateHeight,
                    POSITION_CENTER, MAIN_COLOR, Color.white, visibilityState);
            x += (int) (plateWidth * SPLIT_WIDTH) + PROBLEM_WIDTH;
        }
    }

    private String getShortName(Graphics2D g, String fullName) {
        int fullWidth = g.getFontMetrics(font).stringWidth(fullName);
        double limit = NAME_WIDTH * plateWidth / 1.05;
        if (fullWidth <= limit) {
            return fullName;
        }
        for (int i = fullName.length() - 1; i >= 0; i--) {
            String currentName = fullName.substring(0, i) + "...";
            int currentWidth = g.getFontMetrics(font).stringWidth(currentName);
            if (currentWidth <= limit) {
                return currentName;
            }
        }
        return "";
    }

    private void drawFullTeamPane(Graphics2D g, TeamInfo team, int x, int y) {
        Font font = this.font;
        g.setFont(font);
        drawTextInRect(g, "" + Math.max(team.getRank(), 1), x, y,
                (int) (plateWidth * RANK_WIDTH), (int) plateHeight, POSITION_CENTER,
                ACCENT_COLOR, Color.white, visibilityState);

        x += (int) (plateWidth * (RANK_WIDTH + SPLIT_WIDTH));

        String name = getShortName(g, team.getShortName());
        drawTextInRect(g, name, x, y,
                (int) (plateWidth * NAME_WIDTH), (int) plateHeight, POSITION_LEFT,
                MAIN_COLOR, Color.white, visibilityState);

        x += (int) (plateWidth * (NAME_WIDTH + SPLIT_WIDTH));

        int PROBLEM_WIDTH = (int) ((plateWidth - x - plateWidth * (TOTAL_WIDTH + SPLIT_WIDTH + PENALTY_WIDTH)) / contestData.getProblemsNumber() - plateWidth * SPLIT_WIDTH);
        for (int i = 0; i < contestData.getProblemsNumber(); i++) {
            String status = team.getShortProblemState(i);
            Color statusColor = status.startsWith("+") ? GREEN_COLOR :
                    status.startsWith("?") ? YELLOW_COLOR :
                            status.startsWith("-") ? RED_COLOR :
                                    MAIN_COLOR;
            drawTextInRect(g, status, x, y,
                    PROBLEM_WIDTH, (int) plateHeight, POSITION_CENTER, statusColor, Color.WHITE, visibilityState);
            x += PROBLEM_WIDTH + (int) (plateWidth * SPLIT_WIDTH);
        }

        g.setFont(font);
        drawTextInRect(g, "" + team.getSolvedProblemsNumber(), x, y, (int) (plateWidth * TOTAL_WIDTH),
                (int) plateHeight, POSITION_CENTER, MAIN_COLOR, Color.white, visibilityState);
        x += (int) (plateWidth * (TOTAL_WIDTH + SPLIT_WIDTH));
        drawTextInRect(g, "" + team.getPenalty(), x, y, (int) (plateWidth * PENALTY_WIDTH),
                (int) plateHeight, POSITION_CENTER, MAIN_COLOR, Color.white, visibilityState);
    }
}
