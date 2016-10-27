package ru.ifmo.acm.backend.player.widgets;

import java.awt.*;

import ru.ifmo.acm.backend.player.widgets.stylesheets.*;
import ru.ifmo.acm.backend.graphics.Graphics;
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

    @Override
    public void paintImpl(Graphics gg, int width, int height) {
        update();
        Graphics g = gg.create(0, height - HEIGHT - MARGIN, width, HEIGHT);
        g.setFont(messageFont);
        iterateLogo();

        drawTextInRect(g, currentLogo, 0, 0, LOGO_WIDTH, HEIGHT, Graphics.Alignment.CENTER,
                messageFont, CreepingLineStylesheet.logo, logoVisible);

        g = g.create(LOGO_WIDTH, 0, width - LOGO_WIDTH, HEIGHT);
        g.drawRect(0, 0, width, HEIGHT, CreepingLineStylesheet.main.background, 1, Graphics.RectangleType.SOLID);
        long time = System.currentTimeMillis();
        int dt = (int) (time - last);
        last = time;

        if (messagesQueue.size() > 0 && lastRotation + rotateTime < System.currentTimeMillis()) {
            messageNow = messageNext;
            if (messagesQueue.size() > 0) {
                messageNext = new Message(messagesQueue.poll(), g, messageFont);
            } else {
                messageNext = new Message();
            }
            messageNext.position = HEIGHT;
            lastRotation = System.currentTimeMillis();
        }

        if (messageNow.position + messageNow.heigth < 0) {
            inQueue.remove(messageNow.message);
            messageNow = new Message();
        } else {
            messageNow.position -= V * dt;
            drawTextToFit(g, messageNow.message, 0, messageNow.position, 0, 0, width - LOGO_WIDTH, HEIGHT,
                    messageFont, CreepingLineStylesheet.main.text);
        }
        if (messageNext.position + messageNext.heigth / 2 > HEIGHT / 2) {
            messageNext.position -= V * dt;
        }
        drawTextToFit(g, messageNext.message, 0, messageNext.position, 0, 0, width - LOGO_WIDTH, HEIGHT,
                messageFont, CreepingLineStylesheet.main.text);
    }
}
