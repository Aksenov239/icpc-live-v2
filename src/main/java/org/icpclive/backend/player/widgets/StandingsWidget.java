package org.icpclive.backend.player.widgets;

import org.icpclive.backend.Preparation;
import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.datapassing.StandingsData;
import org.icpclive.events.ContestInfo;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.events.TeamInfo;

import java.awt.*;

/**
 * @author: pashka
 */
public class StandingsWidget extends Widget {

    private static final int MOVING_TIME = 500;
    private static int STANDING_TIME = 5000;
    private static int TOP_PAGE_STANDING_TIME = 10000;

    private static final double DX = 1;

    private final Font font;

    int baseX;
    int baseY;

    private final int plateHeight;
    private final int spaceY;
    private final int spaceX;

    private final int nameWidth;
    private final int rankWidth;
    private final int totalWidth;
    private final int penaltyWidth;

    private final int dx;
    private final int dy;

    public int PERIOD = STANDING_TIME + MOVING_TIME;
    public int LENGTH;

    public final static int TEAMS_ON_PAGE = 12;

    int timer;
    int start;

    private ContestInfo contestData;

    public StandingsWidget(int baseX, int baseY, int plateHeight, long updateWait) {
        super(updateWait);
        last = System.currentTimeMillis();

        this.baseX = baseX;
        this.baseY = baseY;
        this.plateHeight = plateHeight;

        spaceX = (int) Math.round(plateHeight * SPACE_X);
        spaceY = (int) Math.round(plateHeight * SPACE_Y);

        nameWidth = (int) Math.round(NAME_WIDTH * plateHeight);
        rankWidth = (int) Math.round(RANK_WIDTH * plateHeight);
        totalWidth = (int) Math.round(TOTAL_WIDTH * plateHeight);
        penaltyWidth = (int) Math.round(PENALTY_WIDTH * plateHeight);

        dx = (int) Math.round(DX * plateHeight) + nameWidth + rankWidth + totalWidth + penaltyWidth + 3 * spaceX;
        dy = plateHeight + spaceY;

        font = Font.decode(MAIN_FONT + " " + (int) (plateHeight * 0.7));
    }

    public void setState(StandingsData.StandingsType type) {
        switch (type) {
            case ONE_PAGE:
                LENGTH = Math.min(12, contestData.getTeamsNumber());
                start = 0;
                timer = -Integer.MAX_VALUE;
                break;
            case TWO_PAGES:
                TOP_PAGE_STANDING_TIME = 10000;
                STANDING_TIME = 10000;
                PERIOD = STANDING_TIME + MOVING_TIME;
                LENGTH = Math.min(24, contestData.getTeamsNumber());
                start = 0;
                timer = 0;
                break;
            case ALL_PAGES:
                TOP_PAGE_STANDING_TIME = 10000;
                STANDING_TIME = 5000;
                PERIOD = STANDING_TIME + MOVING_TIME;
                LENGTH = contestData.getTeamsNumber();
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
                return 2 * STANDING_TIME + MOVING_TIME;
            default:
                return (pages - 1) * (STANDING_TIME + MOVING_TIME) + TOP_PAGE_STANDING_TIME;
        }
    }

    protected void updateImpl(Data data) {
        if (data.standingsData.isStandingsVisible() && !data.standingsData.isBig()) {
            if (!isVisible() && contestData != null) {
                //  lastVisibleChange = System.currentTimeMillis();
                setState(data.standingsData.standingsType);
            }
        } else {
            setVisible(false);
        }
    }

    @Override
    public void paintImpl(AbstractGraphics g, int width, int height) {
        update();

        contestData = Preparation.eventsLoader.getContestData();
        if (contestData == null || contestData.getStandings() == null) return;
        if (LENGTH == 0)
            LENGTH = contestData.getTeamsNumber();
        int dt = updateVisibilityState();

        if (visibilityState > 0) {
            if (isVisible()) {
                timer = timer + dt;
                if (timer >= PERIOD) {
                    timer -= PERIOD;
                    start += TEAMS_ON_PAGE;
                }
            }
            int dx = 0;
            if (timer >= STANDING_TIME) {
                if (start + TEAMS_ON_PAGE >= LENGTH) {
                    setVisible(false);
                } else {
                    double t = (timer - STANDING_TIME) * 1.0 / MOVING_TIME;
                    dx = (int) ((2 * t * t * t - 3 * t * t) * width);
                }
            }

            int x = baseX;
            int y = baseY;

            if (start < LENGTH) {
                drawStandings(g, x + dx, y, contestData, start);
            }
            if (start + TEAMS_ON_PAGE < LENGTH) {
                drawStandings(g, x + dx + width, y, contestData, start + TEAMS_ON_PAGE);
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

    private void drawStandings(AbstractGraphics g, int x, int y, ContestInfo contestData, int start) {
        for (int i = 0; i < TEAMS_ON_PAGE; i++) {
            if (start + i >= LENGTH)
                break;
            TeamInfo team = contestData.getStandings()[start + i];
            g.setFont(font);
            if (team != null)
                drawTeamPane(g, team, x + dx * (i / 4), y + dy * (i % 4), plateHeight,
                        visibilityState);
        }
    }

    public void alignBottom(int y) {
        baseY = y - 4 * dy;
    }
}
