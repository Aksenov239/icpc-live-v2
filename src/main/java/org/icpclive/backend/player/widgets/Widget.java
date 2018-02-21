package org.icpclive.backend.player.widgets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.backend.Preparation;
import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.player.widgets.stylesheets.TeamPaneStylesheet;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.events.ProblemInfo;
import org.icpclive.events.TeamInfo;

import java.awt.*;

import static java.lang.Math.round;

/**
 * @author: pashka
 */
public abstract class Widget {
    protected static final String MAIN_FONT = "PassagewayICPCLive";
    private static final int PAUSE_AFTER_ERROR = 50;

    protected final Logger log = LogManager.getLogger(getClass());

    public static final int BASE_WIDTH = 1920;
    public static final int BASE_HEIGHT = 1080;

    protected static final double MARGIN = 0.3;

    protected static final double SPACE_Y = 0;
    protected static final double SPACE_X = 0;

    protected static final double NAME_WIDTH = 7;
    protected static final double RANK_WIDTH = 1.5;
    protected static final double TOTAL_WIDTH = 1.3;
    protected static final double PENALTY_WIDTH = 2.0;
    protected static final double PROBLEM_WIDTH = 1;
    protected static final double STATUS_WIDTH = 1.6;

    protected static final int STAR_SIZE = 5;

    protected static final int BIG_SPACE_COUNT = 3;

    private boolean visible;

    // Colors used in graphics

    protected static final Color GREEN_COLOR = new Color(0x1b8041);
    protected static final Color YELLOW_COLOR = new Color(0xa59e0c);
    protected static final Color RED_COLOR = new Color(0x881f1b);
    protected static final long BLINKING_PERIOD = 1000;

    protected static Color mergeColors(Color first, Color second) {
        int rgb = 0;
        for (int i = 0; i < 3; i++) {
            rgb |= ((((first.getRGB() >> (8 * i)) & 255) * 2 +
                    ((second.getRGB() >> (8 * i)) & 255)) / 3) << (8 * i);
        }
        return new Color(rgb);
    }

    protected static final Color YELLOW_GREEN_COLOR = mergeColors(YELLOW_COLOR, GREEN_COLOR);
    protected static final Color YELLOW_RED_COLOR = mergeColors(YELLOW_COLOR, RED_COLOR);

    // Medal colors

    protected final static Color GOLD_COLOR = new Color(0xD4AF37);
    protected final static Color SILVER_COLOR = new Color(0x9090a0);
    protected final static Color BRONZE_COLOR = new Color(0xCD7F32);

    protected final static Color STAR_COLOR = new Color(0xFFFFA0);

    protected long last = 0;
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

    protected void paintImpl(AbstractGraphics g, int width, int height) {
        setGraphics(g.create());
    }

    public void paint(AbstractGraphics g, int width, int height) {
        paint(g, width, height, 1);
    }

    private int pauseAfterError;

    public void paint(AbstractGraphics g, int width, int height, double scale) {
        if (pauseAfterError > 0) {
            pauseAfterError--;
            return;
        }
        if (Preparation.eventsLoader.getContestData() == null) return;
        try {
            g.reset();
            g = g.create();
            g.clip(0, 0, width, height);
            paintImpl(g, width, height);
        } catch (Exception e) {
            log.error("Failed to paint " + this.getClass().toString(), e);
            pauseAfterError = PAUSE_AFTER_ERROR;
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

    protected AbstractGraphics graphics;
    protected Font font;
    private Color backgroundColor;
    private Color textColor;
    private PlateStyle.Alignment alignment;
    protected double maximumOpacity;

    public void setFont(Font font) {
        this.font = font;
        if (graphics != null) graphics.setFont(font);
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        if (graphics != null) graphics.setFillColor(backgroundColor);
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
        if (graphics != null) graphics.setTextColor(textColor);
    }

    public void setAlignment(PlateStyle.Alignment alignment) {
        this.alignment = alignment;
    }

    public void setMaximumOpacity(double maximumOpacity) {
        this.maximumOpacity = maximumOpacity;
    }

    public void setGraphics(AbstractGraphics graphics) {
        if (graphics != null) this.graphics = graphics;
        graphics.setFillColor(backgroundColor);
        graphics.setTextColor(textColor);
        graphics.setFont(font);
    }

    @Deprecated
    protected void drawTextInRect(AbstractGraphics gg, String text, int x, int y, int width, int height,
                                  PlateStyle.Alignment alignment, Font font, PlateStyle plateStyle,
                                  double visibilityState) {
        drawTextInRect(gg, text, x, y, width, height, alignment, font, plateStyle, visibilityState, true);
    }

    @Deprecated
    protected void drawTextInRect(AbstractGraphics gg, String text, int x, int y, int width, int height,
                                  PlateStyle.Alignment alignment, Font font, PlateStyle plateStyle,
                                  double visibilityState, double maximumOpacity, WidgetAnimation widgetAnimation) {
        drawTextInRect(gg, text, x, y, width, height, alignment, font, plateStyle, visibilityState,
                maximumOpacity, true, widgetAnimation, false);
    }

    @Deprecated
    protected void drawTextInRect(AbstractGraphics gg, String text, int x, int y, int width, int height,
                                  PlateStyle.Alignment alignment, Font font, PlateStyle plateStyle,
                                  double visibilityState, boolean scale) {
        drawTextInRect(gg, text, x, y, width, height, alignment, font, plateStyle,
                visibilityState, scale, WidgetAnimation.NOT_ANIMATED, false);
    }

    @Deprecated
    protected void drawTextInRect(AbstractGraphics g, String text, int x, int y, int width, int height, PlateStyle.Alignment alignment,
                                  Font font, PlateStyle plateStyle,
                                  double visibilityState, boolean scale,
                                  WidgetAnimation widgetAnimation, boolean isBlinking) {
        drawTextInRect(g, text, x, y, width, height, alignment, font, plateStyle, visibilityState, 1, scale, widgetAnimation, isBlinking);
    }

    @Deprecated
    protected void drawTextInRect(AbstractGraphics g, String text, int x, int y, int width, int height, PlateStyle.Alignment alignment,
                                  Font font, PlateStyle plateStyle,
                                  double visibilityState, double maximumOpacity, boolean scale,
                                  WidgetAnimation widgetAnimation, boolean isBlinking) {
        double opacity = getOpacity(visibilityState) * maximumOpacity;
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
            double v = getBlinkingState();
            textOpacity *= v;
        }

        g.drawTextInRect(text, x, y, width, height, alignment, font, plateStyle,
                opacity, textOpacity, MARGIN, scale);
    }

    protected void drawRectangleWithText(String text, int x, int y, int width, int height, PlateStyle.Alignment alignment) {
        drawRectangleWithText(text, x, y, width, height, alignment, false);
    }
    protected void drawRectangleWithText(String text, int x, int y, int width, int height, PlateStyle.Alignment alignment, boolean blinking) {
        double opacity = getOpacity(visibilityState) * maximumOpacity;
        double textOpacity = getTextOpacity(visibilityState);
        if (text == null) {
            text = "NULL";
        }
        if (opacity == 0) return;
        if (blinking) {
            double v = getBlinkingState();
            textOpacity *= v;
        }
        graphics.setFillColor(backgroundColor, opacity);
        graphics.drawRect(x, y, width, height);
        graphics.setTextColor(textColor, textOpacity);
        graphics.drawTextThatFits(text, x, y, width, height, alignment, MARGIN);
    }


    protected void drawTextToFit(AbstractGraphics g, String text, double X, double Y, int x, int y, int width, int height, Font font, Color color) {
        g.drawTextThatFits(text, (int) X, (int) Y, width, height, PlateStyle.Alignment.CENTER, MARGIN);
    }

    protected void drawProblemPane(ProblemInfo problem, int x, int y, int width, int height) {
        drawProblemPane(problem, x, y, width, height, false);
    }

    protected void drawProblemPane(ProblemInfo problem, int x, int y, int width, int height, boolean blinking) {
        double opacity = getOpacity(visibilityState) * maximumOpacity;
        double textOpacity = getTextOpacity(visibilityState);
        if (blinking) {
            double v = getBlinkingState();
            textOpacity *= v;
        }
        if (opacity == 0) return;
        graphics.setFillColor(backgroundColor, opacity);
        graphics.drawRect(x, y, width, height - 5);
        graphics.setFillColor(problem.color, opacity);
        graphics.drawRect(x, y + height - 5, width, 5);
        graphics.setTextColor(textColor, textOpacity);
        graphics.drawTextThatFits(problem.letter, x, y, width, height, PlateStyle.Alignment.CENTER, MARGIN);
    }

    private double getBlinkingState() {
        double v = (System.currentTimeMillis() % BLINKING_PERIOD) * 1.0 / BLINKING_PERIOD;
        v = Math.abs(v * 2 - 1);
        return v;
    }

    protected void drawTeamPane(AbstractGraphics g, TeamInfo team, int x, int y, int height, double state,
                                double rank_width, double name_width, double total_width, double penalty_width) {

        PlateStyle color = getTeamRankColor(team);
        if (team.getSolvedProblemsNumber() == 0) color = TeamPaneStylesheet.zero;
        Font font = Font.decode(MAIN_FONT + " " + (int) round(height * 0.7));
        int rankWidth = (int) round(height * rank_width);
        int nameWidth = (int) round(height * name_width);
        int totalWidth = (int) round(height * total_width);
        int penaltyWidth = (int) round(height * penalty_width);
        int spaceX = (int) round(height * SPACE_X);
        drawTextInRect(g, "" + Math.max(team.getRank(), 1), x, y, rankWidth, height, PlateStyle.Alignment.CENTER, font, color, state, 1, WidgetAnimation.UNFOLD_ANIMATED);
        x += rankWidth + spaceX;
        drawTextInRect(g, team.getShortName(), x, y, nameWidth, height, PlateStyle.Alignment.LEFT, font, TeamPaneStylesheet.name, state, 1, WidgetAnimation.UNFOLD_ANIMATED);
        x += nameWidth + spaceX;
        drawTextInRect(g, "" + team.getSolvedProblemsNumber(), x, y, totalWidth, height, PlateStyle.Alignment.CENTER, font, TeamPaneStylesheet.problems, state, 1, WidgetAnimation.UNFOLD_ANIMATED);
        x += totalWidth + spaceX;
        drawTextInRect(g, "" + team.getPenalty(), x, y, penaltyWidth, height, PlateStyle.Alignment.CENTER, font, TeamPaneStylesheet.penalty, state, 1, WidgetAnimation.UNFOLD_ANIMATED);
    }

    protected void drawTeamPane(AbstractGraphics g, TeamInfo team, int x, int y, int height, double state) {
        drawTeamPane(g, team, x, y, height, state, RANK_WIDTH, NAME_WIDTH, TOTAL_WIDTH, PENALTY_WIDTH);
    }

    private long lastChangeTimestamp;
    private long lastTimestamp;
    private Data currentData;

    protected void applyStyle(PlateStyle style) {
        setBackgroundColor(style.background);
        setTextColor(style.text);
        setMaximumOpacity(style.opacity);
    }

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
            ;
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

    protected void drawStar(AbstractGraphics g, int x, int y, int size) {
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
