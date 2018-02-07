package org.icpclive.backend.player.widgets;

import org.icpclive.backend.Preparation;
import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.player.widgets.stylesheets.CreepingLineStylesheet;
import org.icpclive.events.TeamInfo;

import java.awt.*;

import static org.icpclive.backend.player.widgets.old.ClockWidget.getTimeString;

/**
 * @author: pashka
 */
public class VerticalCreepingLineWidget extends CreepingLineWidget {

    private static final String CLOCK = "#Clock#";
    private static final String STANDINGS_MESSAGE = "#Standings#";

    private long rotateTime;
    private long lastRotation;
    private long logoTime;
    private long clockTime;
    private long lastLogoRotation;
    // public String[] logos;
    private String currentLogo = "";
    private long logoChangeTime;

    public VerticalCreepingLineWidget(long updateWait, long rotateTime, String logo, long logoTime, long clockTime, long logoChangeTime) {
        super(updateWait);
        this.rotateTime = rotateTime;
        messageNow.position = HEIGHT;
        last = System.currentTimeMillis();
        // this.logos = logo.split(";");
        lastLogoRotation = System.currentTimeMillis();
        this.logoTime = logoTime;
        this.clockTime = clockTime;
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
        long time = currentLogo.equals(CLOCK) ? this.clockTime : this.logoTime;
        switch (logoState) {
            case 0:
                if (lastLogoRotation + (time - logoChangeTime) / 2 < System.currentTimeMillis()) {
                    logoState = 1;
                }
                break;
            case 1:
                if (lastLogoRotation + time / 2 >= System.currentTimeMillis()) {
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
                if (lastLogoRotation + (time + logoChangeTime) / 2 >= System.currentTimeMillis() || logoVisible < 1) {
                    logoVisible += dt * LOGO_V;
                    logoVisible = Math.min(logoVisible, 1);
                } else {
                    logoState = 4;
                }
                break;
            case 4:
//                System.err.println(lastLogoRotation + " " + logoTime + " " + System.currentTimeMillis() );
                if (lastLogoRotation + time < System.currentTimeMillis()) {
                    lastLogoRotation = System.currentTimeMillis();
                    logoState = 0;
                }
        }
    }

    private int nowStandingsPosition = 0;
    private int nextStandingsPosition = 0;
    private TeamInfo[] standings;
    private final int STANDINGS_SIZE = 12;
    private final int STANDINGS_PAGE = 4;
    private final double percent = 0.87;

    private void drawInfo(AbstractGraphics g, Message message, boolean next, int width, int height) {
        if (!STANDINGS_MESSAGE.equals(message.message)) {
            drawTextToFit(g, message.message, 0, message.position, 0, 0, width, height,
                    messageFont, CreepingLineStylesheet.main.text);
            return;
        }
        standings = Preparation.eventsLoader.getContestData().getStandings();
        int dx = width / STANDINGS_PAGE;
        int start = next ? nextStandingsPosition : nowStandingsPosition;
        for (int i = 0; i < STANDINGS_PAGE && start + i < standings.length; i++) {
            drawTeamPane(g, standings[start + i], dx * i + 5, (int) message.position + 5,
                    (int) (percent * height), 1);
        }
    }

    public void drawLogo(AbstractGraphics g, String currentLogo) {
        if (currentLogo.equals(CLOCK)) {
            long time = Preparation.eventsLoader.getContestData().getCurrentTime() / 1000;
            currentLogo = getTimeString(Math.abs(time));
        }

        drawTextInRect(g, currentLogo, 0, 0, LOGO_WIDTH, HEIGHT, AbstractGraphics.Alignment.CENTER,
                messageFont, CreepingLineStylesheet.logo, logoVisible);
    }

    @Override
    public void paintImpl(AbstractGraphics gg, int width, int height) {
        update();
        AbstractGraphics g = gg.create(0, BASE_HEIGHT - HEIGHT - MARGIN, BASE_WIDTH, HEIGHT);
        g.setFont(messageFont);
        iterateLogo();

        drawLogo(g, currentLogo);

        g = g.create(LOGO_WIDTH, 0, BASE_WIDTH - LOGO_WIDTH, HEIGHT);
        //g.clip();
        g.drawRect(0, 0, BASE_WIDTH - LOGO_WIDTH, HEIGHT, CreepingLineStylesheet.main.background, 1, AbstractGraphics.RectangleType.SOLID);
        long time = System.currentTimeMillis();
        int dt = (int) (time - last);
        last = time;

        if (messagesQueue.size() > 0 && lastRotation + rotateTime < System.currentTimeMillis()) {
//        if (lastRotation + rotateTime < System.currentTimeMillis()) {
            messageNow = messageNext;
            nowStandingsPosition = nextStandingsPosition;
            nextStandingsPosition += STANDINGS_PAGE;
            if (STANDINGS_MESSAGE.equals(messageNow.message) &&
                    nextStandingsPosition < Math.min(STANDINGS_SIZE, standings == null ? STANDINGS_SIZE : standings.length)) {
                messageNext = new Message(STANDINGS_MESSAGE, g, messageFont); // next message is still standings
            } else {
                if (messagesQueue.size() > 0) {
                    messageNext = new Message(messagesQueue.poll(), g, messageFont);
                    inQueue.remove(messageNext.message);
                    if (STANDINGS_MESSAGE.equals(messageNext.message)) {
                        nextStandingsPosition = 0;
                        standings = Preparation.eventsLoader.getContestData().getStandings();
                    }
                } else {
                    messageNext = new Message();
                }
            }
            messageNext.position = HEIGHT;
            lastRotation = System.currentTimeMillis();
        }

        if (messageNow.position + messageNow.heigth < 0) {
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
