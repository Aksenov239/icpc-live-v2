package ru.ifmo.acm.backend.player.widgets;

import java.awt.*;
import ru.ifmo.acm.events.TeamInfo;

/**
 * @author: pashka
 */
public abstract class Widget {

    public final static double OPACITY = 1;

    public static final double MARGIN = 0.2;
    private static final double MARGIN_BOTTOM = 0.25;
    private boolean visible;

    public final static Color MAIN_COLOR = Color.decode("0x193C5B");
    public final static Color ADDITIONAL_COLOR = Color.decode("0x4C83C3");
    public final static Color ACCENT_COLOR = Color.decode("0x871E1B");

    public final static Color GOLD_COLOR = new Color(228, 200, 126);
    public final static Color GOLD_COLOR2 = new Color(238, 220, 151);
    public final static Color SILVER_COLOR = new Color(182, 180, 185);
    public final static Color SILVER_COLOR2 = new Color(205, 203, 206);
    public final static Color BRONZE_COLOR = new Color(180, 122, 124);
    public final static Color BRONZE_COLOR2 = new Color(194, 150, 146);


    long last = 0;
    double opacity = 1;
    double textOpacity = 1;
    double opacityState = 1;

    public abstract void paint(Graphics2D g, int width, int height);

    private static final double V = 0.001;

    protected int changeOpacity() {
        long time = System.currentTimeMillis();
        if (last == 0) {
            opacityState = visible ? 1 : 0;
        }
        int dt = last == 0 ? 0 : (int) (time - last);
        last = time;
        if (isVisible()) {
            setOpacityState(Math.min(opacityState + dt * V, 1));
        } else {
            setOpacityState(Math.max(opacityState - dt * V, 0));
        }
        return dt;
    }

    public void setOpacityState(double opacityState) {
        this.opacityState = opacityState;
        opacity = f(opacityState * 2);
        textOpacity = f(opacityState * 2 - 1);
    }

    protected double f(double x) {
        if (x < 0) return 0;
        if (x > 1) return 1;
        return 3 * x * x - 2 * x * x * x;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    void drawRect(Graphics2D g, int x, int y, int width, int height, Color color, double opacity) {
        g.setComposite(AlphaComposite.SrcOver.derive(1f))   ;
        g.setColor(color);

        int hh = (int) (height * opacity);
        y += (height - hh) / 2;
        height = hh;

        int dx = (int) (0.1 * height);
        int[] xx = new int[]{x - dx, x + width - dx, x + width + dx, x + dx};
        int[] yy = new int[]{y + height, y + height, y, y};
        g.fill(new Polygon(xx, yy, 4));
    }

    static final int POSITION_LEFT = 0;
    static final int POSITION_RIGHT = 1;
    static final int POSITION_CENTER = 2;

    void drawTextInRect(Graphics2D g, String text, int x, int y, int width, int height, int position, Color color, Color textColor, double state) {
        setOpacityState(state);
        if (text == null) {
            text = "NULL";
        }
        if (width == -1) {
            int w = g.getFontMetrics().stringWidth(text);
            width = (int) (w + 2.5 * height * MARGIN);
            if (position == POSITION_CENTER) {
                x -= width / 2;
            } else if (position == POSITION_RIGHT) {
                x -= width;
            }
        }
        if (opacity == 0) return;
        drawRect(g, x, y, width, height, color, opacity);
        g.setComposite(AlphaComposite.SrcOver.derive((float) (textOpacity)));
        g.setColor(textColor);
        float yy = (float) (y + (height * (1 - MARGIN_BOTTOM)));
        if (position == POSITION_LEFT) {
            float xx = x + (float) (height * MARGIN);
            g.drawString(text, xx, yy);
        } else if (position == POSITION_CENTER) {
            int w = g.getFontMetrics().stringWidth(text);
            float xx = x + (width - w) / 2;
            g.drawString(text, xx, yy);
        } else {
            int w = g.getFontMetrics().stringWidth(text);
            float xx = x + width - w - (float) (1.5 * height * MARGIN);
            g.drawString(text, xx, yy);
        }
    }

    private static final double SPLIT = 0.005;
    public static final double RANK_WIDTH = 0.11;
    public static final double NAME_WIDTH = 0.60;
    public static final double SOLVED_WIDTH = 0.10;
    public static final double PENALTY_WIDTH = 0.17;

    void drawTeamPane(Graphics2D g, TeamInfo team, int x, int y, int width, double state) {
        Color color = team.getRank() <= 3 ? GOLD_COLOR : team.getRank() <= 7 ? SILVER_COLOR : team.getRank() <= 12 ? BRONZE_COLOR : ACCENT_COLOR;
        if (team.getSolvedProblemsNumber() == 0) color = ACCENT_COLOR;
        int height = (int) (width * 0.1);
        g.setFont(Font.decode("Open Sans Italic " + (int) (height * 0.7)));
        drawTextInRect(g, "" + Math.max(team.getRank(), 1), x, y, (int) (width * RANK_WIDTH), height, POSITION_CENTER, color, Color.WHITE, state);
        x += (int) (width * (RANK_WIDTH + SPLIT));
        drawTextInRect(g, team.getShortName(), x, y, (int) (width * NAME_WIDTH), height, POSITION_LEFT, MAIN_COLOR, Color.WHITE, state);
        x += (int) (width * (NAME_WIDTH + SPLIT));
        drawTextInRect(g, "" + team.getSolvedProblemsNumber(), x, y, (int) (width * SOLVED_WIDTH), height, POSITION_CENTER, ADDITIONAL_COLOR, Color.WHITE, state);
        x += (int) (width * (SOLVED_WIDTH + SPLIT));
        drawTextInRect(g, "" + team.getPenalty(), x, y, (int) (width * PENALTY_WIDTH), height, POSITION_CENTER, ADDITIONAL_COLOR, Color.WHITE, state);
    }


}
