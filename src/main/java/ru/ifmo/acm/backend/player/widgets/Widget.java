package ru.ifmo.acm.backend.player.widgets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.TeamInfo;

import java.awt.*;
import java.awt.geom.AffineTransform;

import static java.lang.Math.abs;
import static java.lang.Math.round;
import static java.lang.Math.scalb;

/**
 * @author: pashka
 */
public abstract class Widget {
    protected final Logger log = LogManager.getLogger(getClass());

    public final static double OPACITY = 1;

    public static final int BASE_WIDTH = 1920;
    public static final int BASE_HEIGHT = 1080;

    public static final double MARGIN = 0.3;
    protected static final double SPACE_Y = 0.1;
    protected static final double SPACE_X = 0.05;
    protected static final double NAME_WIDTH = 6;
    protected static final double RANK_WIDTH = 1.6;
    protected static final double TOTAL_WIDTH = 1.3;
    protected static final double PENALTY_WIDTH = 2.0;
    protected static final double PROBLEM_WIDTH = 1.2;
    protected static final double STATUS_WIDTH = 2;
    protected static final int STAR_SIZE = 5;
    static final int BIG_SPACE_COUNT = 6;

    private boolean visible;

    // Colors used in graphics
    public final static Color MAIN_COLOR = new Color(0x193C5B);
    //    public final static Color ADDITIONAL_COLOR = new Color(0x4C83C3);
    public final static Color ADDITIONAL_COLOR = new Color(0x3C6373);
    public final static Color ACCENT_COLOR = new Color(0x881F1B);

    public static final Color GREEN_COLOR = new Color(0x1b8041);
    //    public static final Color YELLOW_COLOR = new Color(0xD4AF37);
    public static final Color YELLOW_COLOR = new Color(0xa59e0c);
    public static final Color RED_COLOR = new Color(0x881f1b);


    private static Color mergeColors(Color first, Color second) {
        int rgb = 0;
        for (int i = 0; i < 3; i++) {
            rgb |= ((((first.getRGB() >> (8 * i)) & 255) * 2 +
                    ((second.getRGB() >> (8 * i)) & 255)) / 3) << (8 * i);
        }
        return new Color(rgb);
    }

    public static final Color YELLOW_GREEN_COLOR = mergeColors(YELLOW_COLOR, GREEN_COLOR);
    public static final Color YELLOW_RED_COLOR = mergeColors(YELLOW_COLOR, RED_COLOR);

    // Medal colors

    //        public final static Color GOLD_COLOR = new Color(228, 200, 126);
    public final static Color GOLD_COLOR = new Color(0xD4AF37);
    public final static Color SILVER_COLOR = new Color(0x9090a0);
    public final static Color BRONZE_COLOR = new Color(0xCD7F32);

    public final static Color STAR_COLOR = new Color(0xFFFFA0);
//    public final static Color GOLD_COLOR2 = new Color(238, 220, 151);
//    public final static Color SILVER_COLOR = new Color(182, 180, 185);
//    public final static Color SILVER_COLOR2 = new Color(205, 203, 206);
//    public final static Color BRONZE_COLOR = new Color(180, 122, 124);
//    public final static Color BRONZE_COLOR2 = new Color(194, 150, 146);

    // Rectangles rounding
    private static final int POINTS_IN_ROUND = 3;
    private static final double ROUND_RADIUS = 4;

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
        if (Preparation.eventsLoader.getContestData() == null) return;
        if (scale != 1) {
            g = (Graphics2D) g.create();
            g.scale(scale, scale);
            width = (int) round(width / scale);
            height = (int) round(height / scale);
        }
        try {
            paintImpl(g, width, height);
        } catch (Exception e) {
            log.error("Failed to paint " + this.getClass().toString(), e);
        }
    }

    protected static final double V = 0.001;

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

    public double getOpacity(double visibilityState) {
        return f(visibilityState * 2);
    }

    public double getTextOpacity(double visibilityState) {
        return f(visibilityState * 2 - 1);
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

    void drawRect(Graphics2D g, int x, int y, int width, int height, Color color, double opacity, boolean italic) {
        g.setComposite(AlphaComposite.SrcOver.derive(1f));
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
                if (italic) tx -= (ty - (y + height / 2)) * 0.2;
                xx[t] = (int) round(tx);
                yy[t] = (int) round(ty);
            }
        }
        g.fill(new Polygon(xx, yy, xx.length));
    }

    void drawRect(Graphics2D g, int x, int y, int width, int height, Color color, double opacity) {
        drawRect(g, x, y, width, height, color, opacity, false);
    }

    static final int POSITION_LEFT = 0;
    static final int POSITION_RIGHT = 1;
    static final int POSITION_CENTER = 2;

    private double lastBlinkingOpacity = 0;
    private long lastBlinkingOpacityUpdate = 0;
    private double blinkingValue = 0.20;

    void drawTextInRect(Graphics2D gg, String text, int x, int y, int width, int height, int position, Color color, Color textColor, double visibilityState) {
        drawTextInRect(gg, text, x, y, width, height, position, color, textColor, visibilityState, false, true);
    }

    void drawTextInRect(Graphics2D gg, String text, int x, int y, int width, int height, int position, Color color, Color textColor, double visibilityState, WidgetAnimation widgetAnimation) {
        drawTextInRect(gg, text, x, y, width, height, position, color, textColor, visibilityState, false, true, widgetAnimation, false);
    }

    void drawTextInRect(Graphics2D gg, String text, int x, int y, int width, int height, int position, Color color, Color textColor, double visibilityState, boolean italic, boolean scale) {
        drawTextInRect(gg, text, x, y, width, height, position, color, textColor, visibilityState, italic, scale, WidgetAnimation.NOT_ANIMATED, false);
    }

    void drawTextInRect(Graphics2D gg, String text, int x, int y, int width, int height, int position, Color color, Color textColor,
                        double visibilityState, boolean isBlinking) {
        drawTextInRect(gg, text, x, y, width, height, position,
                color, textColor, visibilityState, false, true,
                WidgetAnimation.NOT_ANIMATED, isBlinking);
    }


    void drawTextInRect(Graphics2D gg, String text, int x, int y, int width, int height, int position,
                        Color color, Color textColor,
                        double visibilityState, boolean italic, boolean scale,
                        WidgetAnimation widgetAnimation, boolean isBlinking) {
        Graphics2D g = (Graphics2D) gg.create();
        double opacity = getOpacity(visibilityState);
        double textOpacity = getTextOpacity(visibilityState);
        if (text == null) {
            text = "NULL";
        }

        int textWidth = g.getFontMetrics().stringWidth(text);
        double textScale = 1;

        double margin = height * MARGIN;

        if (width == -1) {
            width = (int) (textWidth + 2 * margin);
            if (position == POSITION_CENTER) {
                x -= width / 2;
            } else if (position == POSITION_RIGHT) {
                x -= width;
            }
        } else if (scale) {
            int maxTextWidth = (int) (width - 2 * margin);
            if (textWidth > maxTextWidth) {
                textScale = 1.0 * maxTextWidth / textWidth;
            }
        }

        if (opacity == 0) return;

        if (widgetAnimation.isHorizontalAnimated) {
            width = (int) round(width * visibilityState);
        }
        if (widgetAnimation.isVerticalAnimated) {
            height = (int) round(height * visibilityState);
        }

        if (widgetAnimation == WidgetAnimation.NOT_ANIMATED) {
            opacity = 1;
        }

        drawRect(g, x, y, width, height, color, opacity, italic);

        if (isBlinking) {
            if (System.currentTimeMillis() - lastBlinkingOpacityUpdate > 10) {
                lastBlinkingOpacity += blinkingValue;
                if (abs(lastBlinkingOpacity - 1) < 1e-9 || lastBlinkingOpacity < 1e-9) {
                    blinkingValue *= -1;
                }
                lastBlinkingOpacityUpdate = System.currentTimeMillis();
            }

            textOpacity = lastBlinkingOpacity;
        }

        g.setComposite(AlphaComposite.SrcOver.derive((float) (textOpacity)));
        g.setColor(textColor);

        FontMetrics wh = g.getFontMetrics();

//        if (wh.getStringBounds(text, g).getWidth() > width * 0.95) {
//        Font adjustedFont = adjustFont(g, text, width, height, 0.95);
//        g.setFont(adjustedFont);
//        wh = g.getFontMetrics();
//        }

        float yy = (float) (y + 1.0 * (height - wh.getStringBounds(text, g).getHeight()) / 2) + wh.getAscent()
                - 0.03f * height;
        float xx;
        if (position == POSITION_LEFT) {
            xx = (float) (x + margin);
        } else if (position == POSITION_CENTER) {
            xx = (float) (x + (width - textWidth * textScale) / 2);
        } else {
            xx = (float) (x + width - textWidth * textScale - margin);
        }
        AffineTransform transform = g.getTransform();
        transform.concatenate(AffineTransform.getTranslateInstance(xx, yy));
        transform.concatenate(AffineTransform.getScaleInstance(textScale, 1));
        g.setTransform(transform);
//        g.translate(xx, yy);
//        g.getTransform().concatenate();
        g.drawString(text, 0, 0);
        g.dispose();
    }

    void drawTextToFit(Graphics2D gg, String text, double X, double Y, int x, int y, int width, int height) {
        Graphics2D g = (Graphics2D) gg.create(x, y, width, height);
        FontMetrics wh = g.getFontMetrics();
        int textWidth = g.getFontMetrics().stringWidth(text);
        double textScale = 1;

        double margin = height * MARGIN;

        int maxTextWidth = (int) (width - 2 * margin);
        if (textWidth > maxTextWidth) {
            textScale = 1. * maxTextWidth / textWidth;
        }

        float yy = (float) Y + wh.getAscent() - 0.03f * height;
        float xx = (float) (X + margin);

        AffineTransform transform = g.getTransform();
        transform.concatenate(AffineTransform.getTranslateInstance(xx, yy));
        transform.concatenate(AffineTransform.getScaleInstance(textScale, 1));
        g.setTransform(transform);
        g.drawString(text, 0, 0);
    }

    void drawTeamPane(Graphics2D g, TeamInfo team, int x, int y, int height, double state) {
        Color color = getTeamRankColor(team);
        if (team.getSolvedProblemsNumber() == 0) color = ACCENT_COLOR;
        g.setFont(Font.decode("Open Sans " + (int) round(height * 0.7)));
        int rankWidth = (int) round(height * RANK_WIDTH);
        int nameWidth = (int) round(height * NAME_WIDTH);
        int totalWidth = (int) round(height * TOTAL_WIDTH);
        int penaltyWidth = (int) round(height * PENALTY_WIDTH);
        int spaceX = (int) round(height * SPACE_X);
        drawTextInRect(g, "" + Math.max(team.getRank(), 1), x, y, rankWidth, height, POSITION_CENTER, color, Color.WHITE, state, WidgetAnimation.UNFOLD_ANIMATED);
        x += rankWidth + spaceX;
        drawTextInRect(g, team.getShortName(), x, y, nameWidth, height, POSITION_LEFT, MAIN_COLOR, Color.WHITE, state, WidgetAnimation.UNFOLD_ANIMATED);
        x += nameWidth + spaceX;
        drawTextInRect(g, "" + team.getSolvedProblemsNumber(), x, y, totalWidth, height, POSITION_CENTER, ADDITIONAL_COLOR, Color.WHITE, state, WidgetAnimation.UNFOLD_ANIMATED);
        x += totalWidth + spaceX;
        drawTextInRect(g, "" + team.getPenalty(), x, y, penaltyWidth, height, POSITION_CENTER, ADDITIONAL_COLOR, Color.WHITE, state);
    }

    long lastChangeTimestamp;
    long lastTimestamp;
    Data currentData;

    protected void update() {

        if (lastUpdate + updateWait < System.currentTimeMillis()) {
            Data data = Preparation.dataLoader.getDataBackend();

            if (data == null)
                return;

            CachedData correspondingData = getCorrespondingData(data);

            if (correspondingData == null) {
                return;
            }

            if (correspondingData.timestamp != lastChangeTimestamp) {
                lastChangeTimestamp = correspondingData.timestamp;
                lastTimestamp = System.currentTimeMillis();
            }

            if (lastChangeTimestamp + correspondingData.delay < System.currentTimeMillis()) {
                currentData = data;
            }
            updateImpl(currentData);
            lastUpdate = System.currentTimeMillis();
        }
    }

    protected abstract CachedData getCorrespondingData(Data data);

    protected void updateImpl(Data data) {
    }

    protected Color getTeamRankColor(TeamInfo team) {
        Color color = ADDITIONAL_COLOR;
        if (team.getSolvedProblemsNumber() > 0 && team.getRank() <= 12) {
            color = team.getRank() <= 4 ? GOLD_COLOR :
                    team.getRank() <= 8 ? SILVER_COLOR : BRONZE_COLOR;
        }
        return color;
    }

    protected void drawStar(Graphics2D g, int x, int y, int size) {
        g.setColor(STAR_COLOR);
        int[] xx = new int[10];
        int[] yy = new int[10];
        double[] d = {size, size * 2};
        for (int i = 0; i < 10; i++) {
            xx[i] = (int) (x + Math.sin(Math.PI * i / 5) * d[i % 2]);
            yy[i] = (int) (y + Math.cos(Math.PI * i / 5) * d[i % 2]);
        }
        g.fillPolygon(new Polygon(xx, yy, 10));
    }
}
