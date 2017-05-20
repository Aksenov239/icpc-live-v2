package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.backend.player.widgets.stylesheets.CreepingLineStylesheet;
import ru.ifmo.acm.backend.player.widgets.stylesheets.TeamPaneStylesheet;
import ru.ifmo.acm.backend.player.widgets.stylesheets.PlateStyle;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.TeamInfo;

import java.awt.*;

import static ru.ifmo.acm.backend.player.widgets.ClockWidget.getTimeString;

/**
 * @author: pashka
 */
public class VerticalCreepingLineWidget extends CreepingLineWidget {
    public long rotateTime;
    public long lastRotation;
    public long logoTime;
    public long lastLogoRotation;
    // public String[] logos;
    public String currentLogo = "";
    public long logoChangeTime;

    public VerticalCreepingLineWidget(long updateWait, long rotateTime, String logo, long logoTime, long logoChangeTime) {
        super(updateWait);
        this.rotateTime = rotateTime;
        messageNow.position = HEIGHT;
        last = System.currentTimeMillis();
        // this.logos = logo.split(";");
        lastLogoRotation = System.currentTimeMillis();
        this.logoTime = logoTime;
        this.logoChangeTime = logoChangeTime;
        LOGO_V = 1. / logoChangeTime;
    }

    public int getHeightToDraw(Graphics2D g, String text, int y) {
        FontMetrics fm = g.getFontMetrics();
        return (int) Math.ceil(y + fm.getAscent());
    }

    private static final double V = 0.05;
    private final double LOGO_V;

    Message messageNow = new Message();
    Message messageNext = new Message();
    private static final int LOGO_WIDTH = 180;
    private double logoVisible = 1;
    private int logoState = 4;
    private long lastLogoIteration;

    public void iterateLogo() {
        long dt = System.currentTimeMillis() - lastLogoIteration;
        lastLogoIteration = System.currentTimeMillis();
//        System.err.println((System.currentTimeMillis() - lastLogoRotation) + " " + logoTime / 2 + " " + logoChangeTime / 2 + " " + logoState);
        switch (logoState) {
            case 0:
                if (lastLogoRotation + (logoTime - logoChangeTime) / 2 < System.currentTimeMillis()) {
                    logoState = 1;
                }
                break;
            case 1:
                if (lastLogoRotation + logoTime / 2 >= System.currentTimeMillis()) {
                    logoVisible -= dt * LOGO_V;
                    logoVisible = Math.max(logoVisible, 0);
                } else {
                    logoState = 2;
                }
                break;
            case 2:
                // currentLogo = (currentLogo + 1) % logos.length;
                if (logoQueue.size() > 0) {
                    currentLogo = logoQueue.poll();
                    inLogoQueue.remove(currentLogo);
                } else {
                    currentLogo = "";
                }
                logoState = 3;
                break;
            case 3:
                if (lastLogoRotation + (logoTime + logoChangeTime) / 2 >= System.currentTimeMillis() || logoVisible < 1) {
                    logoVisible += dt * LOGO_V;
                    logoVisible = Math.min(logoVisible, 1);
                } else {
                    logoState = 4;
                }
                break;
            case 4:
//                System.err.println(lastLogoRotation + " " + logoTime + " " + System.currentTimeMillis() );
                if (lastLogoRotation + logoTime < System.currentTimeMillis()) {
                    lastLogoRotation = System.currentTimeMillis();
                    logoState = 0;
                }
        }
    }

    int nextStandingsPosition = 0;
    TeamInfo[] standings;
    private final String STANDINGS_MESSAGE = "#Standings#";
    private final int STANDINGS_SIZE = 12;
    private final int STANDINGS_PAGE = 4;


    private final double SPACE_WIDTH = 0.02;
    private final double RANK_WIDTH = 0.1;
    private final double SMALL_PLATE_DY = 5.5;
    private final int SMALL_PLATE_HEIGHT = (int)(0.85 * HEIGHT);
    private final double NAME_WIDTH = 0.50;
    private final double TOTAL_WIDTH = 0.1;
    private final double PENALTY_WIDTH = 0.2;

    private void drawSmallTeamPane(Graphics g, TeamInfo team, int width, int height, int x, int y) {
        int spaceWidth = (int) (SPACE_WIDTH * width);
        int rankWidth = (int) (RANK_WIDTH * width);
        int nameWidth = (int) (NAME_WIDTH * width);
        int totalWidth = (int) (TOTAL_WIDTH * width);
        int penaltyWidth = (int) (PENALTY_WIDTH * width);

        // small rectangle with rank
        PlateStyle color = getTeamRankColor(team);

        x += spaceWidth;
        drawTextInRect(g, "" + Math.max(team.getRank(), 1), x, (int)(y + SMALL_PLATE_DY), rankWidth, SMALL_PLATE_HEIGHT,
                Graphics.Alignment.CENTER, messageFont, color, 1, WidgetAnimation.UNFOLD_ANIMATED);
        x += rankWidth + spaceWidth;

        // team name
        drawTextInRect(g, team.getShortName(), x, (int) (y + SMALL_PLATE_DY), nameWidth, SMALL_PLATE_HEIGHT,
                Graphics.Alignment.LEFT, messageFont, TeamPaneStylesheet.penalty, 1, WidgetAnimation.UNFOLD_ANIMATED);

        x += nameWidth + spaceWidth;

        // total
        drawTextInRect(g, "" + team.getSolvedProblemsNumber(), x, (int) (y + SMALL_PLATE_DY), totalWidth, SMALL_PLATE_HEIGHT,
                Graphics.Alignment.CENTER, messageFont, TeamPaneStylesheet.penalty, 1, WidgetAnimation.UNFOLD_ANIMATED);
        x += totalWidth + spaceWidth;

        // penalty
        drawTextInRect(g, "" + team.getPenalty(), x, (int) (y + SMALL_PLATE_DY), penaltyWidth, SMALL_PLATE_HEIGHT,
                Graphics.Alignment.CENTER, messageFont, TeamPaneStylesheet.penalty, 1, WidgetAnimation.UNFOLD_ANIMATED);
    }

    private void drawInfo(Graphics g, Message message, boolean next, int width, int height) {
        if (!STANDINGS_MESSAGE.equals(message.message)) {
            drawTextToFit(g, message.message, 0, message.position, 0, 0, width, height,
                    messageFont, CreepingLineStylesheet.main.text);
            return;
        }
        int dx = width / STANDINGS_PAGE;
        int start = next ? nextStandingsPosition : nextStandingsPosition - STANDINGS_PAGE;
        for (int i = 0; i < STANDINGS_PAGE && start + i < standings.length; i++) {
            drawSmallTeamPane(g, standings[start + i], dx, height, dx * i, (int)message.position);
        }
    }

    public void drawLogo(Graphics g, String currentLogo) {
        if (currentLogo.equals("#Clock#")) {
            long time = Preparation.eventsLoader.getContestData().getCurrentTime() / 1000;
            currentLogo = getTimeString(Math.abs(time));
        }

        drawTextInRect(g, currentLogo, 0, 0, LOGO_WIDTH, HEIGHT, Graphics.Alignment.CENTER,
                messageFont, CreepingLineStylesheet.logo, logoVisible);
    }

    @Override
    public void paintImpl(Graphics gg, int width, int height) {
        update();
        Graphics g = gg.create(0, BASE_HEIGHT - HEIGHT - MARGIN, BASE_WIDTH, HEIGHT);
        g.setFont(messageFont);
        iterateLogo();

        drawLogo(g, currentLogo);

        g = g.create(LOGO_WIDTH, 0, BASE_WIDTH - LOGO_WIDTH, HEIGHT);
        //g.clip();
        g.drawRect(0, 0, BASE_WIDTH - LOGO_WIDTH, HEIGHT, CreepingLineStylesheet.main.background, 1, Graphics.RectangleType.SOLID);
        long time = System.currentTimeMillis();
        int dt = (int) (time - last);
        last = time;

        if (messagesQueue.size() > 0 && lastRotation + rotateTime < System.currentTimeMillis()) {
//        if (lastRotation + rotateTime < System.currentTimeMillis()) {
            messageNow = messageNext;
            nextStandingsPosition += STANDINGS_PAGE;
            if (STANDINGS_MESSAGE.equals(messageNow.message) &&
                    nextStandingsPosition < Math.min(STANDINGS_SIZE, standings == null ? STANDINGS_SIZE : standings.length)) {
                messageNext = new Message(STANDINGS_MESSAGE, g, messageFont); // next message is still standings
            } else {
                if (messagesQueue.size() > 0) {
                    messageNext = new Message(messagesQueue.poll(), g, messageFont);
                    if (STANDINGS_MESSAGE.equals(messageNext.message)) {
                        nextStandingsPosition = 0;
                        standings = Preparation.eventsLoader.getContestData().getStandings();
                    }
                } else {
                    messageNext = new Message();
                }
//                if (STANDINGS_MESSAGE.equals(messageNext.message)) {
//                    messageNext = new Message("H!", g, messageFont);
//                } else {
//                    messageNext = new Message(STANDINGS_MESSAGE, g, messageFont);
//                }
                if (STANDINGS_MESSAGE.equals(messageNext.message)) {
//                    nextStandingsPosition = 0;
//                    standings = Preparation.eventsLoader.getContestData().getStandings();
                }
            }
            messageNext.position = HEIGHT;
            lastRotation = System.currentTimeMillis();
        }

        if (messageNow.position + messageNow.heigth < 0) {
            inQueue.remove(messageNow.message);
            messageNow = new Message();
        } else {
            messageNow.position -= V * dt;
            drawInfo(g, messageNow, false, width - LOGO_WIDTH, HEIGHT);
//            drawTextToFit(g, messageNow.message, 0, messageNow.position, 0, 0, width - LOGO_WIDTH, HEIGHT,
//                    messageFont, CreepingLineStylesheet.main.text);
        }
        if (messageNext.position + messageNext.heigth / 2 > HEIGHT / 2) {
            messageNext.position -= V * dt;
        }
        drawInfo(g, messageNext, true, width - LOGO_WIDTH, HEIGHT);

//        drawTextToFit(g, messageNext.message, 0, messageNext.position, 0, 0, width - LOGO_WIDTH, HEIGHT,
//                messageFont, CreepingLineStylesheet.main.text);
    }
}
