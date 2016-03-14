package ru.ifmo.acm.backend.player.widgets;

import java.awt.*;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.TeamInfo;

/**
 * @author: pashka
 */
public abstract class Widget {
    public final static double OPACITY = 1;

    public static final int BASE_WIDTH = 1280;
    public static final int BASE_HEIGHT = 720;

    public static final double MARGIN = 0.3;
    private static final double MARGIN_BOTTOM = 0.25;
    private boolean visible;

    // Colors used in graphics
    public final static Color MAIN_COLOR = new Color(0x193C5B);
    public final static Color ADDITIONAL_COLOR = new Color(0x4C83C3);
    public final static Color ACCENT_COLOR = new Color(0x881F1B);

    public static final Color GREEN_COLOR = new Color(0x1b8041);
    public static final Color YELLOW_COLOR = new Color(0xe0aa12).darker();
    public static final Color RED_COLOR = new Color(0x881f1b);

    // Medal colors
    public final static Color GOLD_COLOR = new Color(228, 200, 126);
    public final static Color GOLD_COLOR2 = new Color(238, 220, 151);
    public final static Color SILVER_COLOR = new Color(182, 180, 185);
    public final static Color SILVER_COLOR2 = new Color(205, 203, 206);
    public final static Color BRONZE_COLOR = new Color(180, 122, 124);
    public final static Color BRONZE_COLOR2 = new Color(194, 150, 146);

    // Rectangles rounding
    private static final int POINTS_IN_ROUND = 3;
    private static final double ROUND_RADIUS = 2;

    long last = 0;
    double opacity = 0;
    double textOpacity = 0;
    double visibilityState = 0;

    protected long updateWait;
    protected long lastUpdate;

    public Widget() {
        setVisible(false);
    }

    public Widget(long updateWait) {
        this();
        this.updateWait = updateWait;
    }

    protected abstract void paintImpl(Graphics2D g, int width, int height);

    public void paint(Graphics2D g, int width, int height, double scale) {
        if (scale != 1) {
            g = (Graphics2D) g.create();
            g.scale(scale, scale);
            width = (int) Math.round(width / scale);
            height = (int) Math.round(height / scale);
        }
        paintImpl(g, width, height);
    }

    private static final double V = 0.001;

    protected int updateVisibilityState() {
        long time = System.currentTimeMillis();
        if (last == 0) {
            visibilityState = visible ? 1 : 0;
        }
        int dt = last == 0 ? 0 : (int) (time - last);
        last = time;
        if (isVisible()) {
            setVisibilityState(Math.min(visibilityState + dt * V, 1));
        } else {
            setVisibilityState(Math.max(visibilityState - dt * V, 0));
        }
        return dt;
    }

    public void setVisibilityState(double visibilityState) {
        this.visibilityState = visibilityState;
        opacity = f(visibilityState * 2);
        textOpacity = f(visibilityState * 2 - 1);
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


        int[] xx = new int[POINTS_IN_ROUND * 4];
        int[] yy = new int[POINTS_IN_ROUND * 4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < POINTS_IN_ROUND; j++) {
                int t = i * POINTS_IN_ROUND + j;
                double a = Math.PI / 2 * j / (POINTS_IN_ROUND - 1);
                int dx = new int[]{0, 1, 0, -1}[i];
                int dy = new int[]{1, 0, -1, 0}[i];
                double baseX = (i == 0 || i == 3 ? x + ROUND_RADIUS : x + width - ROUND_RADIUS);
                double baseY = (i == 2 || i == 3 ? y + ROUND_RADIUS : y + height - ROUND_RADIUS);

                double tx = baseX + ROUND_RADIUS * (dx * Math.sin(a) - dy * Math.cos(a));
                double ty = baseY + ROUND_RADIUS * (dx * Math.cos(a) + dy * Math.sin(a));
                tx -= (ty - (y + height / 2)) * 0.2;
                xx[t] = (int) Math.round(tx);
                yy[t] = (int) Math.round(ty);
            }
        }
        g.fill(new Polygon(xx, yy, xx.length));
    }

    static final int POSITION_LEFT = 0;
    static final int POSITION_RIGHT = 1;
    static final int POSITION_CENTER = 2;

    void drawTextInRect(Graphics2D g, String text, int x, int y, int width, int height, int position, Color color, Color textColor, double state) {
        setVisibilityState(state);
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
        FontMetrics wh = g.getFontMetrics();
        float yy = (float) (y + 1.0 * (height - wh.getStringBounds(text, g).getHeight()) / 2) + wh.getAscent()
                - 0.03f * height;
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
        Color color = team.getRank() <= 4 ? GOLD_COLOR : team.getRank() <= 8 ? SILVER_COLOR : team.getRank() <= 12 ? BRONZE_COLOR : ACCENT_COLOR;
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

    protected void update() {
        if (lastUpdate + updateWait < System.currentTimeMillis()) {
            Data data = Preparation.dataLoader.getDataBackend();
            if (data == null) {
                return;
            }
            updateImpl(data);
            lastUpdate = System.currentTimeMillis();
        }
    }

    protected void updateImpl(Data data) {
    }
}
