package org.icpclive.backend.player.widgets;

import com.jogamp.opengl.GLAutoDrawable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.backend.Preparation;
import org.icpclive.backend.player.widgets.stylesheets.TeamPaneStylesheet;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.events.TeamInfo;

import java.awt.*;

import static java.lang.Math.abs;
import static java.lang.Math.round;

/**
 * @author: pashka
 */
public abstract class Widget {
    protected final Logger log = LogManager.getLogger(getClass());

    public static final int BASE_WIDTH = 1920;
    public static final int BASE_HEIGHT = 1080;

    public static final double MARGIN = 0.3;
//    protected static final double SPACE_Y = 0.1;
    protected static final double SPACE_Y = 0;
//    protected static final double SPACE_X = 0.05;
    protected static final double SPACE_X = 0;
    protected static final double NAME_WIDTH = 6;
    protected static final double RANK_WIDTH = 1.6;
    protected static final double TOTAL_WIDTH = 1.3;
    protected static final double PENALTY_WIDTH = 2.0;
    protected static final double PROBLEM_WIDTH = 1.2;
    protected static final double STATUS_WIDTH = 2;
    protected static final int STAR_SIZE = 5;
    static final int BIG_SPACE_COUNT = 3;

    private boolean visible;

    // Colors used in graphics

    public static final Color GREEN_COLOR = new Color(0x1b8041);
    public static final Color YELLOW_COLOR = new Color(0xa59e0c);
    public static final Color RED_COLOR = new Color(0x881f1b);
    private long BLINKING_PERIOD = 1000;

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
    private static final int POINTS_IN_ROUND = 1;
    private static final double ROUND_RADIUS = 0;

    private long last = 0;
    protected double opacity = 0;
    protected double textOpacity = 0;
    protected double visibilityState = 0;

    protected long updateWait;
    protected long lastUpdate;

    public Widget() {
        setVisible(false);
    }

    public Widget(long updateWait) {
        this();
        this.updateWait = updateWait;
    }

    protected void move(int width, int height, int dt) {
    }

    protected abstract void paintImpl(org.icpclive.backend.graphics.Graphics g, int width, int height);


    protected void paintImpl(GLAutoDrawable drawable, int width, int height) {

    }

    public void paint(org.icpclive.backend.graphics.Graphics g, int width, int height) {
        paint(g, width, height, 1);
    }

    public void paint(org.icpclive.backend.graphics.Graphics g, int width, int height, double scale) {
        if (Preparation.eventsLoader.getContestData() == null) return;
        try {
            g.reset();
            g = g.create();
            g.clip(0, 0, width, height);
            paintImpl(g, width, height);
        } catch (Exception e) {
            log.error("Failed to paint " + this.getClass().toString(), e);
            e.printStackTrace();
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
        return 40;//dt;
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

    public static final int POSITION_LEFT = 0;
    public static final int POSITION_RIGHT = 1;
    public static final int POSITION_CENTER = 2;

    private double lastBlinkingOpacity = 0;
    private long lastBlinkingOpacityUpdate = 0;
    private double blinkingValue = 0.20;

    protected void drawTextInRect(org.icpclive.backend.graphics.Graphics gg, String text, int x, int y, int width, int height,
                        org.icpclive.backend.graphics.Graphics.Alignment alignment, Font font, PlateStyle plateStyle,
                        double visibilityState) {
        drawTextInRect(gg, text, x, y, width, height, alignment, font, plateStyle, visibilityState, true);
    }

    protected void drawTextInRect(org.icpclive.backend.graphics.Graphics gg, String text, int x, int y, int width, int height,
                        org.icpclive.backend.graphics.Graphics.Alignment alignment, Font font, PlateStyle plateStyle,
                        double visibilityState, WidgetAnimation widgetAnimation) {
        drawTextInRect(gg, text, x, y, width, height, alignment, font, plateStyle, visibilityState, true, widgetAnimation, false);
    }

    protected void drawTextInRect(org.icpclive.backend.graphics.Graphics gg, String text, int x, int y, int width, int height,
                        org.icpclive.backend.graphics.Graphics.Alignment alignment, Font font, PlateStyle plateStyle,
                        double visibilityState, boolean scale) {
        drawTextInRect(gg, text, x, y, width, height, alignment, font, plateStyle, visibilityState, scale, WidgetAnimation.NOT_ANIMATED, false);
    }

    protected void drawTextInRect(org.icpclive.backend.graphics.Graphics g, String text, int x, int y, int width, int height, org.icpclive.backend.graphics.Graphics.Alignment alignment,
                        Font font, PlateStyle plateStyle,
                        double visibilityState, boolean scale,
                        WidgetAnimation widgetAnimation, boolean isBlinking) {
        double opacity = getOpacity(visibilityState);
        double textOpacity = getTextOpacity(visibilityState);
        if (text == null) {
            text = "NULL";
        }

        if (opacity == 0) return;

        if (widgetAnimation.isHorizontalAnimated) {
            width = (int) round(width * visibilityState);
        }
        if (widgetAnimation.isVerticalAnimated) {
            height = (int) round(height * visibilityState);
        }

//        if (widgetAnimation != WidgetAnimation.UNFOLD_ANIMATED) {
//            opacity = 1;
//        }

        if (isBlinking) {
            double v = (System.currentTimeMillis() % BLINKING_PERIOD) * 1.0 / BLINKING_PERIOD;
            v = Math.abs(v * 2 - 1);
            textOpacity *= v;
        }

        g.drawRectWithText(text, x, y, width, height, alignment, font, plateStyle,
                opacity, textOpacity, MARGIN, scale);
    }

    protected void drawTextToFit(org.icpclive.backend.graphics.Graphics g, String text, double X, double Y, int x, int y, int width, int height, Font font, Color color) {
       // Graphics gg = g.create(x, y, width, height);
        g.drawTextThatFits(text, (int) X, (int) Y, width, height, font, color, MARGIN);
    }

    protected void drawTeamPane(org.icpclive.backend.graphics.Graphics g, TeamInfo team, int x, int y, int height, double state,
                      double rank_width, double name_width, double total_width, double penalty_width) {

        PlateStyle color = getTeamRankColor(team);
        if (team.getSolvedProblemsNumber() == 0) color = TeamPaneStylesheet.zero;
        Font font = Font.decode("Open Sans " + (int) round(height * 0.7));
        int rankWidth = (int) round(height * rank_width);
        int nameWidth = (int) round(height * name_width);
        int totalWidth = (int) round(height * total_width);
        int penaltyWidth = (int) round(height * penalty_width);
        int spaceX = (int) round(height * SPACE_X);
        drawTextInRect(g, "" + Math.max(team.getRank(), 1), x, y, rankWidth, height, org.icpclive.backend.graphics.Graphics.Alignment.CENTER, font, color, state, WidgetAnimation.UNFOLD_ANIMATED);
        x += rankWidth + spaceX;
        drawTextInRect(g, team.getShortName(), x, y, nameWidth, height, org.icpclive.backend.graphics.Graphics.Alignment.LEFT, font, TeamPaneStylesheet.name, state, WidgetAnimation.UNFOLD_ANIMATED);
        x += nameWidth + spaceX;
        drawTextInRect(g, "" + team.getSolvedProblemsNumber(), x, y, totalWidth, height, org.icpclive.backend.graphics.Graphics.Alignment.CENTER, font, TeamPaneStylesheet.problems, state, WidgetAnimation.UNFOLD_ANIMATED);
        x += totalWidth + spaceX;
        drawTextInRect(g, "" + team.getPenalty(), x, y, penaltyWidth, height, org.icpclive.backend.graphics.Graphics.Alignment.CENTER, font, TeamPaneStylesheet.penalty, state, WidgetAnimation.UNFOLD_ANIMATED);
    }

    protected void drawTeamPane(org.icpclive.backend.graphics.Graphics g, TeamInfo team, int x, int y, int height, double state) {
        drawTeamPane(g, team, x, y, height, state, RANK_WIDTH, NAME_WIDTH, TOTAL_WIDTH, PENALTY_WIDTH);
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
            };
            updateImpl(currentData);
            lastUpdate = System.currentTimeMillis();
        }
    }

    protected abstract CachedData getCorrespondingData(Data data);

    protected void updateImpl(Data data) {
    }

    protected PlateStyle getTeamRankColor(TeamInfo team) {
        PlateStyle color = TeamPaneStylesheet.none;
        if (team.getSolvedProblemsNumber() > 0 &&
                team.getRank() <= TeamPaneStylesheet.goldPlaces + TeamPaneStylesheet.silverPlaces +
                        TeamPaneStylesheet.bronzePlaces) {
            color = team.getRank() <= TeamPaneStylesheet.goldPlaces ? TeamPaneStylesheet.gold :
                    team.getRank() <= TeamPaneStylesheet.silverPlaces + TeamPaneStylesheet.goldPlaces ? TeamPaneStylesheet.silver :
                            TeamPaneStylesheet.bronze;
        }
        return color;
    }

    protected void drawStar(org.icpclive.backend.graphics.Graphics g, int x, int y, int size) {
        int[] xx = new int[10];
        int[] yy = new int[10];
        double[] d = {size, size * 2};
        for (int i = 0; i < 10; i++) {
            xx[i] = (int) (x + Math.sin(Math.PI * i / 5) * d[i % 2]);
            yy[i] = (int) (y + Math.cos(Math.PI * i / 5) * d[i % 2]);
        }
        g.fillPolygon(xx, yy, STAR_COLOR);
    }
}
