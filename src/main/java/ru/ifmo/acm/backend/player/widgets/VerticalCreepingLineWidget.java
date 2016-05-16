package ru.ifmo.acm.backend.player.widgets;

import java.awt.*;


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
    public void paintImpl(Graphics2D gg, int width, int height) {
        update();
        Graphics2D g = (Graphics2D) gg.create(0, height - HEIGHT - MARGIN, width, HEIGHT);
        g.setFont(messageFont);
        g.setComposite(AlphaComposite.SrcOver.derive(1f));
        g.setColor(ADDITIONAL_COLOR);
        iterateLogo();

        drawTextInRect(g, currentLogo, 0, 0, LOGO_WIDTH, HEIGHT, POSITION_CENTER, ADDITIONAL_COLOR, Color.WHITE, logoVisible);

        g = (Graphics2D) g.create(LOGO_WIDTH, 0, width - LOGO_WIDTH, HEIGHT);
        g.setColor(MAIN_COLOR);
        g.fillRect(0, 0, width, HEIGHT);
        g.setComposite(AlphaComposite.SrcOver.derive((float) (1)));
        g.setFont(messageFont);
        g.setColor(Color.white);
        long time = System.currentTimeMillis();
        int dt = (int) (time - last);
        last = time;

        if (messagesQueue.size() > 0 && lastRotation + rotateTime < System.currentTimeMillis()) {
            messageNow = messageNext;
            if (messagesQueue.size() > 0) {

                messageNext = new Message(messagesQueue.poll(), g);
            } else {
                messageNext = new Message();
            }
            messageNext.position = HEIGHT;
            lastRotation = System.currentTimeMillis();
        }
        FontMetrics wh = g.getFontMetrics();

        if (messageNow.position + messageNow.heigth < 0) {
            inQueue.remove(messageNow.message);
            messageNow = new Message();
        } else {
            messageNow.position -= V * dt;
            drawTextToFit(g, messageNow.message, 0, messageNow.position, 0, 0, width - LOGO_WIDTH, HEIGHT);
        }
        if (messageNext.position + messageNext.heigth / 2 > HEIGHT / 2) {
            messageNext.position -= V * dt;
        }
        drawTextToFit(g, messageNext.message, 0, messageNext.position, 0, 0, width - LOGO_WIDTH, HEIGHT);
    }
}
