package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.backend.player.TickPlayer;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.TeamInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author: pashka
 */
public class StandingsWidget extends Widget {

    private static final int MOVING_TIME = 500;
    private final int PLATE_WIDTH = (int) (326 * TickPlayer.scale);
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

    private final BufferedImage image;
    //double opacity;
    //long last;
    int timer;
    int start;

    private ContestInfo contestData;

    public StandingsWidget(long updateWait) {
        BufferedImage image;
        try {
            image = ImageIO.read(new File("pics/standings.png"));
        } catch (IOException e) {
            image = null;
        }
        this.image = image;
        last = System.currentTimeMillis();

        this.updateWait = updateWait;
    }

    private long updateWait;
    private long lastUpdate;
    // private long lastVisibleChange = Long.MAX_VALUE / 2;

    public void setState(long type) {
        switch ((int) type) {
            case 0:
                LENGTH = 12;
                start = 0;
                timer = -Integer.MAX_VALUE;
                break;
            case 1:
                TOP_PAGE_STANDING_TIME = 10000;
                STANDING_TIME = 10000;
                PERIOD = STANDING_TIME + MOVING_TIME;
                LENGTH = 24;
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
                if (!isVisible()) {
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
        update();
//        standings = StandingsLoader.getLoaded();
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
            int dx = 0;
            if (timer >= STANDING_TIME) {
                if (start + TEAMS_ON_PAGE >= LENGTH) {
                    setVisible(false);
                } else {
                    double t = (timer - STANDING_TIME) * 1.0 / MOVING_TIME;
                    dx = (int) ((2 * t * t * t - 3 * t * t) * width);
                }
            }
            int x = (int) ((width - (DX + DX + PLATE_WIDTH)) / 2);
            int y = (int) (height - 32 * TickPlayer.scale - 4.5 * DY);
//            g.setComposite(AlphaComposite.SrcOver.derive((float) opacity));
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

    private void drawStandings(Graphics2D g, int x, int y, ContestInfo contestData, int start) {
        for (int i = 0; i < TEAMS_ON_PAGE; i++) {
            if (start + i >= LENGTH)
                break;
            TeamInfo team = contestData.getStandings()[start + i];
            int dx = (int) (DX * (i / 4));
            int dy = (int) (DY * (i % 4));
            g.setFont(FONT);
            if (team != null)
                drawTeamPane(g, team, x + dx, y + dy, PLATE_WIDTH, opacityState);
        }
    }
}
