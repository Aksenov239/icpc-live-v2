package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.backend.player.TickPlayer;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;

import java.awt.*;

/**
 * @author: pashka
 */
public class BigStandingsWidget extends Widget {

    private static final int MOVING_TIME = 500;
    private final int PLATE_WIDTH;
    private final double PLATE_HEIGHT;
    private final double SPACE_VS_PLATE = 0.05;
    private final double SPACE_Y;
    private final int SPACE_X;
    private final int MOVING_HEIGHT;
    private static int STANDING_TIME = 5000;
    private static int TOP_PAGE_STANDING_TIME = 10000;
    public int PERIOD = STANDING_TIME + MOVING_TIME;
    public int LENGTH;
    private final int X1 = (int) (31 * TickPlayer.scale);
    private final int X2 = (int) (55 * TickPlayer.scale);
    private final int X3 = (int) (275 * TickPlayer.scale);
    private final int X4 = (int) (314 * TickPlayer.scale);
    private final double DX = 349 * TickPlayer.scale;
    private final int Y1 = (int) (65 * TickPlayer.scale);
    private final double DY = 35 * TickPlayer.scale;
    public final static int TEAMS_ON_PAGE = 12;
    public final Font FONT = Font.decode("Open Sans Italic " + (int) (22 * TickPlayer.scale));

    int timer;
    int start;
    final int X, Y, HEIGHT, WIDTH;
    final boolean controlled;

    private ContestInfo contestData;

    public BigStandingsWidget(int x, int y, int width, int height, long updateWait, boolean controlled) {
        last = System.currentTimeMillis();
        X = x;
        Y = y;
        WIDTH = width;
        HEIGHT = height;
        this.controlled = controlled;
        if (!controlled) {
            setOpacityState(1);
            setVisible(true);
        }

        PLATE_WIDTH = (int) (width * 0.9);
        SPACE_X = (width - PLATE_WIDTH) / 2;
        double total = (TEAMS_ON_PAGE + 1) * (1 + SPACE_VS_PLATE) + SPACE_VS_PLATE;
        PLATE_HEIGHT = height / total;
        SPACE_Y = PLATE_HEIGHT * SPACE_VS_PLATE;

        MOVING_HEIGHT = (int) (PLATE_HEIGHT * ((1 + SPACE_VS_PLATE) * TEAMS_ON_PAGE + SPACE_VS_PLATE));

        this.updateWait = updateWait;
    }

    private long updateWait;
    private long lastUpdate;

    public void setState(long type) {
        switch ((int) type) {
            case 0:
                LENGTH = Math.min(12, contestData.getTeamsNumber());
                start = 0;
                timer = -Integer.MAX_VALUE;
                break;
            case 1:
                TOP_PAGE_STANDING_TIME = 10000;
                STANDING_TIME = 10000;
                PERIOD = STANDING_TIME + MOVING_TIME;
                LENGTH = Math.min(24, contestData.getTeamsNumber());
                start = 0;
                timer = 0;
                break;
            case 2:
                TOP_PAGE_STANDING_TIME = 10000;
                STANDING_TIME = 5000;
                PERIOD = STANDING_TIME + MOVING_TIME;
                LENGTH = contestData.getTeamsNumber();
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

    public void update() {
        if (lastUpdate + updateWait < System.currentTimeMillis()) {
            Data data = Preparation.dataLoader.getDataBackend();
            if (data == null) {
                return;
            }
            if (data.standingsData.isStandingsVisible) {
                if (!isVisible() && contestData != null) {
                    //  lastVisibleChange = System.currentTimeMillis();
                    setState(data.standingsData.standingsType);
                }
            } else {
                setVisible(false);
            }
            lastUpdate = System.currentTimeMillis();
        }
    }

    @Override
    public void paint(Graphics2D g, int width, int height) {
        if (controlled) {
            update();
        }
        contestData = Preparation.eventsLoader.getContestData();
        if (contestData == null || contestData.getStandings() == null) return;
        if (LENGTH == 0)
            LENGTH = contestData.getTeamsNumber();
        int dt = changeOpacity();

        if (opacityState > 0) {
            if (isVisible()) {
                timer = timer + dt;
                if (timer >= PERIOD) {
                    timer -= PERIOD;
                    start += TEAMS_ON_PAGE;
                }
            }
            int dy = 0;
            if (timer >= STANDING_TIME) {
                if (start + TEAMS_ON_PAGE >= LENGTH) {
                    if (controlled) {
                        setVisible(false);
                    }
                } else {
                    double t = (timer - STANDING_TIME) * 1.0 / MOVING_TIME;
                    dy = (int) ((2 * t * t * t - 3 * t * t) * MOVING_HEIGHT);
                }
            }

            if (start < LENGTH) {
                drawTeams(g, SPACE_X, (int) (PLATE_HEIGHT + 2 * SPACE_Y + dy), contestData, start);
            }
            if (start + TEAMS_ON_PAGE < LENGTH) {
                if (start + TEAMS_ON_PAGE >= LENGTH)
                    start = -TEAMS_ON_PAGE;
                drawTeams(g, SPACE_X, (int) (PLATE_HEIGHT + 2 * SPACE_Y + dy + MOVING_HEIGHT), contestData, start + TEAMS_ON_PAGE);
            }
            drawHead(g, SPACE_X, (int) SPACE_Y, contestData.getProblemsNumber());
        } else {
            timer = -TOP_PAGE_STANDING_TIME;
            start = 0;
        }
    }

    private void drawTeams(Graphics2D g, int x, int y, ContestInfo contestData, int start) {
        for (int i = 0; i < TEAMS_ON_PAGE; i++) {
            if (start + i >= LENGTH)
                break;
            TeamInfo team = contestData.getStandings()[start + i];
            int dx = 0;
            int dy = (int) (i * (PLATE_HEIGHT + SPACE_Y));
            g.setFont(FONT);
            if (team != null && y + dy >= SPACE_Y) {
                drawFullTeamPane(g, team, x + dx, y + dy);
            }
        }
    }

    private static final double SPLIT_WIDTH = 0.005;
    private static final double RANK_WIDTH = 0.07;
    private static final double NAME_WIDTH = 0.4;
    private static final double TOTAL_WIDTH = 0.08;
    private static final double PENALTY_WIDTH = 0.08;

    private void drawHead(Graphics2D g, int x, int y, int problemsNumber) {
        g.setFont(Font.decode("Open Sans Italic " + (int) (PLATE_HEIGHT * 0.5)));
        drawTextInRect(g, "Rank", x, y, (int) (PLATE_WIDTH * RANK_WIDTH), (int) PLATE_HEIGHT,
                POSITION_CENTER, ADDITIONAL_COLOR, Color.white, opacityState);
        x += (int) (PLATE_WIDTH * (RANK_WIDTH + SPLIT_WIDTH));
        drawTextInRect(g, "Name", x, y, (int) (PLATE_WIDTH * NAME_WIDTH), (int) PLATE_HEIGHT,
                POSITION_CENTER, ADDITIONAL_COLOR, Color.white, opacityState);
        x += (int) (PLATE_WIDTH * (NAME_WIDTH + SPLIT_WIDTH));
        int PROBLEM_WIDTH = (int) ((PLATE_WIDTH - x - PLATE_WIDTH * (TOTAL_WIDTH + SPLIT_WIDTH + PENALTY_WIDTH)) / problemsNumber);
        for (int i = 0; i < problemsNumber; i++) {
            drawTextInRect(g, "" + (char) ('A' + i), x, y, PROBLEM_WIDTH, (int) PLATE_HEIGHT,
                    POSITION_CENTER, ADDITIONAL_COLOR, Color.white, opacityState);
            x += (int) (PLATE_WIDTH * SPLIT_WIDTH) + PROBLEM_WIDTH;
        }
        drawTextInRect(g, "Total", x, y, (int) (PLATE_WIDTH * TOTAL_WIDTH), (int) PLATE_HEIGHT,
                POSITION_CENTER, ADDITIONAL_COLOR, Color.white, opacityState);
        x += (int) (PLATE_WIDTH * (TOTAL_WIDTH + SPLIT_WIDTH));
        drawTextInRect(g, "Penalty", x, y, (int) (PLATE_WIDTH * PENALTY_WIDTH), (int) PLATE_HEIGHT,
                POSITION_CENTER, ADDITIONAL_COLOR, Color.white, opacityState);
    }

    private void drawFullTeamPane(Graphics2D g, TeamInfo team, int x, int y) {
        g.setFont(Font.decode("Open Sans Italic " + (int) (PLATE_HEIGHT * 0.7)));
        drawTextInRect(g, "" + Math.max(team.getRank(), 1), x, y,
                (int) (PLATE_WIDTH * RANK_WIDTH), (int) PLATE_HEIGHT, POSITION_CENTER, ADDITIONAL_COLOR, Color.white, opacityState);

        x += (int) (PLATE_WIDTH * (RANK_WIDTH + SPLIT_WIDTH));

        int nameWidth = g.getFontMetrics(Font.decode("Open Sans Italic " + (int) (PLATE_HEIGHT * 0.7))).stringWidth(team.getName());
        g.setFont(Font.decode("Open Sans Italic " + (int) (PLATE_HEIGHT * 0.7 * Math.min(NAME_WIDTH * PLATE_WIDTH / nameWidth / 1.1, 1))));
        drawTextInRect(g, team.getName(), x, y,
                (int) (PLATE_WIDTH * NAME_WIDTH), (int) PLATE_HEIGHT, POSITION_CENTER, ADDITIONAL_COLOR, Color.white, opacityState);

        x += (int) (PLATE_WIDTH * (NAME_WIDTH + SPLIT_WIDTH));

        g.setFont(Font.decode("Open Sans Italic " + (int) (PLATE_HEIGHT * 0.5)));
        java.util.List<RunInfo>[] runs = team.getRuns();
        int PROBLEM_WIDTH = (int) ((PLATE_WIDTH - x - PLATE_WIDTH * (TOTAL_WIDTH + SPLIT_WIDTH + PENALTY_WIDTH)) / runs.length);
        for (int i = 0; i < runs.length; i++) {
            int total = 0;
            String status = "";
            for (RunInfo run : runs[i]) {
                if ("AC".equals(run.getResult())) {
                    status = "AC";
                    break;
                }
                total++;
                status = run.getResult();
            }
            Color statusColor = status.equals("AC") ? Color.green :
                    status.equals("UD") ? Color.yellow :
                            total == 0 ? ADDITIONAL_COLOR.darker() : Color.red;
            String prefix = status.equals("AC") ? "+" :
                    status.equals("UD") ? "?" :
                            total == 0 ? "" : "-";
            prefix = "";
            drawTextInRect(g, prefix + (total != 0 ? total : ""), x, y,
                    PROBLEM_WIDTH, (int) PLATE_HEIGHT, POSITION_CENTER, statusColor, Color.black, opacityState);
            x += PROBLEM_WIDTH + (int) (PLATE_WIDTH * SPLIT_WIDTH);
        }

        g.setFont(Font.decode("Open Sans Italic " + (int) (PLATE_HEIGHT * 0.7)));
        drawTextInRect(g, "" + team.getSolvedProblemsNumber(), x, y, (int) (PLATE_WIDTH * TOTAL_WIDTH),
                (int) PLATE_HEIGHT, POSITION_CENTER, ADDITIONAL_COLOR, Color.white, opacityState);
        x += (int) (PLATE_WIDTH * (TOTAL_WIDTH + SPLIT_WIDTH));
        drawTextInRect(g, "" + team.getPenalty(), x, y, (int) (PLATE_WIDTH * PENALTY_WIDTH),
                (int) PLATE_HEIGHT, POSITION_CENTER, ADDITIONAL_COLOR, Color.white, opacityState);
    }
}
