package org.icpclive.backend.player.widgets;

import org.icpclive.backend.Preparation;
import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.player.widgets.stylesheets.CreepingLineStylesheet;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.datapassing.Data;
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
        LOGO_V = 2. / logoChangeTime;
        setVisible(true);
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
    private double logoOpacity = 1;
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
                    logoOpacity -= dt * LOGO_V;
                    logoOpacity = Math.max(logoOpacity, 0);
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
                if (lastLogoRotation + (time + logoChangeTime) / 2 >= System.currentTimeMillis() || logoOpacity < 1) {
                    logoOpacity += dt * LOGO_V;
                    logoOpacity = Math.min(logoOpacity, 1);
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
    private final int STANDINGS_SIZE = 32;
    private final int STANDINGS_PAGE = 4;
    private final double percent = 0.8;

    private void drawInfo(Message message, boolean next, int width, int height) {
        if (STANDINGS_MESSAGE.equals(message.message)) {
            standings = Preparation.eventsLoader.getContestData().getStandings();
            int dx = width / STANDINGS_PAGE;
            int start = next ? nextStandingsPosition : nowStandingsPosition;
            for (int i = 0; i < STANDINGS_PAGE && start + i < standings.length; i++) {
                drawTeamPane(graphics, standings[start + i], dx * i + 5, (int) message.position + 5,
                        (int) (percent * height), visibilityState);
            }
        } else {
            setFont(messageFont);
            setTextOpacity(textOpacity);
            applyStyle(CreepingLineStylesheet.main);
            graphics.drawTextThatFits(message.message, 0, (int) message.position, width, height, PlateStyle.Alignment.LEFT, MARGIN, true);
        }
    }

    public void drawLogo(String currentLogo) {
        if (currentLogo.equals(CLOCK)) {
            long time = Preparation.eventsLoader.getContestData().getCurrentTime();
            currentLogo = getTimeString(Math.abs(time));
        }

        if (Preparation.eventsLoader.getContestData().getCurrentTime() <= 0) {
            applyStyle(CreepingLineStylesheet.logoBefore);
        } else {
            applyStyle(CreepingLineStylesheet.logo);
        }
        drawRectangle(0, 0, LOGO_WIDTH, HEIGHT);
        setTextOpacity(logoOpacity * textOpacity);
        drawTextThatFits(currentLogo, 0, 0, LOGO_WIDTH, HEIGHT, PlateStyle.Alignment.CENTER, true);
    }

    @Override
    public void updateImpl(Data data) {
        super.updateImpl(data);
        setVisible(data.creepingLineData.isVisible);
    }

    @Override
    public void paintImpl(AbstractGraphics gg, int width, int height) {
        super.paintImpl(gg, width, height);

        if (visibilityState == 0) return;

        setGraphics(graphics.create(0, BASE_HEIGHT - HEIGHT - BOTTOM, BASE_WIDTH, HEIGHT));
        setMaximumOpacity(1);
//        setVisibilityState(1);
        setFont(messageFont);
        iterateLogo();

        drawLogo(currentLogo);

        graphics.translate(LOGO_WIDTH, 0);
        applyStyle(CreepingLineStylesheet.main);
        drawRectangle(0, 0, BASE_WIDTH - LOGO_WIDTH, HEIGHT);

        if (messagesQueue.size() > 0 && lastRotation + rotateTime < System.currentTimeMillis()) {
//        if (lastRotation + rotateTime < System.currentTimeMillis()) {
            messageNow = messageNext;
            nowStandingsPosition = nextStandingsPosition;
            nextStandingsPosition += STANDINGS_PAGE;
            if (STANDINGS_MESSAGE.equals(messageNow.message) &&
                    nextStandingsPosition < Math.min(STANDINGS_SIZE, standings == null ? STANDINGS_SIZE : standings.length)) {
                messageNext = new Message(STANDINGS_MESSAGE, graphics, messageFont); // next message is still standings
            } else {
                if (messagesQueue.size() > 0) {
                    messageNext = new Message(messagesQueue.poll(), graphics, messageFont);
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
            drawInfo(messageNow, false, width - LOGO_WIDTH, HEIGHT);
        }
        if (messageNext.position + messageNext.heigth / 2 > HEIGHT / 2) {
            messageNext.position -= V * dt;
        }
        drawInfo(messageNext, true, width - LOGO_WIDTH, HEIGHT);
    }
}
