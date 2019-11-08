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
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import static java.lang.Math.rint;
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
    protected static final double PROBLEM_WIDTH = 1.2;
    protected static final double STATUS_WIDTH = 1.6;
    protected static final double TIME_WIDTH = 2;

    protected static final int STAR_SIZE = 5;

    private boolean visible;

    // Colors used in graphics

    protected static final long BLINKING_PERIOD = 1500;
    protected int sleepTime;

    protected static Color mergeColors(Color first, Color second, double v) {
        int rgb = 0;
        for (int i = 0; i < 3; i++) {
            rgb |= (int)(((first.getRGB() >> (8 * i)) & 255) * (1 - v) +
                    ((second.getRGB() >> (8 * i)) & 255) * v) << (8 * i);
        }
        return new Color(rgb);
    }

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

    int dt = 40;

    protected void paintImpl(AbstractGraphics g, int width, int height) {
        setGraphics(g.create());
        dt = updateVisibilityState();
        update();
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

    protected AbstractGraphics graphics;
    protected Font font;
    private Color backgroundColor = Color.black;
    private Color textColor = Color.white;
    protected double maximumOpacity;

    public void setFont(Font font) {
        this.font = font;
        if (graphics != null) graphics.setFont(font);
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        if (graphics != null) graphics.setFillColor(backgroundColor, maximumOpacity);
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
        if (graphics != null) graphics.setTextColor(textColor);
    }

    public void setMaximumOpacity(double maximumOpacity) {
        this.maximumOpacity = maximumOpacity;
        graphics.setFillColor(backgroundColor, maximumOpacity);
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

    protected void drawGradientRectangleWithText(String text, int x, int y, int width, int height, PlateStyle.Alignment alignment, Color colorLeft, Color colorRight) {
        graphics.drawGradientRect(x, y, width, height, opacity, colorLeft, colorRight);
        double textOpacity = getTextOpacity(visibilityState);
        if (text == null) {
            text = "NULL";
        }
        if (textOpacity == 0) return;
        setTextOpacity(textOpacity);
        drawTextThatFits(text, x, y, width, height, alignment, true);
    }

    protected void drawRectangleWithText(String text, int x, int y, int width, int height, PlateStyle.Alignment alignment) {
        drawRectangleWithText(text, x, y, width, height, alignment, false);
    }

    protected void drawRectangleWithText(String text, int x, int y, int width, int height,
                                         PlateStyle.Alignment alignment, boolean blinking) {
        drawRectangleWithText(text, x, y, width, height, alignment, blinking, true);
    }

    protected void drawRectangleWithText(String text, int x, int y, int width, int height,
                                         PlateStyle.Alignment alignment, boolean blinking, boolean scaleText) {
        drawRectangle(x, y, width, height);
        double textOpacity = getTextOpacity(visibilityState);
        if (text == null) {
            text = "NULL";
        }
        if (blinking) {
            double v = getBlinkingState();
            textOpacity *= v;
        }
        if (textOpacity == 0) return;
        setTextOpacity(textOpacity);
        drawTextThatFits(text, x, y, width, height, alignment, scaleText);
    }

    protected void setTextOpacity(double textOpacity) {
        graphics.setTextColor(textColor, textOpacity);
    }

    protected void drawText(String text, int x, int y, double opacity) {
        graphics.drawString(text, x, y, font, textColor, opacity);
    }

    protected void drawText(String text, int x, int y) {
        graphics.drawString(text, x, y, font, textColor, textOpacity);
    }

    protected void drawTextThatFits(String text, int x, int y, int width, int height, PlateStyle.Alignment alignment, boolean scaleText) {
        graphics.drawTextThatFits(text, x, y, width, height, alignment, MARGIN, scaleText);
    }

    protected void drawRectangle(int x, int y, int width, int height) {
        double opacity = getOpacity(visibilityState) * maximumOpacity;
        if (opacity == 0) return;
        graphics.setFillColor(backgroundColor, opacity);
        graphics.drawRect(x, y, width, height);
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
        setTextOpacity(textOpacity);
        drawTextThatFits(problem.letter, x, y, width, height, PlateStyle.Alignment.CENTER, true);
    }

    protected double getBlinkingState() {
        double v = (System.currentTimeMillis() % BLINKING_PERIOD) * 1.0 / BLINKING_PERIOD;
        v = Math.abs(v * 2 - 1);
        return v;
    }

    protected static int getTeamPaneWidth(int height) {
        int rankWidth = (int) round(height * RANK_WIDTH);
        int nameWidth = (int) round(height * NAME_WIDTH);
        int totalWidth = (int) round(height * TOTAL_WIDTH);
        int penaltyWidth = (int) round(height * PENALTY_WIDTH);
        return rankWidth + nameWidth + totalWidth + penaltyWidth;
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
        drawTextInRect(g, "" + Math.max(team.getRank(), 1), x, y, rankWidth, height, PlateStyle.Alignment.CENTER, font, color, state, 1, false, WidgetAnimation.UNFOLD_ANIMATED, false);
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

            if (lastTimestamp + correspondingData.delay < System.currentTimeMillis()) {
                currentData = data;
            }
            ;
            updateImpl(currentData);
            lastUpdate = System.currentTimeMillis();
        }
    }

    protected CachedData getCorrespondingData(Data data) {
        return null;
    }

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

    protected void drawStar(int x, int y, int size, double opacity) {
        int[] xx = new int[10];
        int[] yy = new int[10];
        double[] d = {size, size * 2};
        for (int i = 0; i < 10; i++) {
            xx[i] = (int) (x + Math.sin(Math.PI * i / 5) * d[i % 2]);
            yy[i] = (int) (y + Math.cos(Math.PI * i / 5) * d[i % 2]);
        }
        graphics.fillPolygon(xx, yy, STAR_COLOR, opacity);
    }

    public static int getStringWidth(Font font, String string) {
        return (int) font.getStringBounds(string, new FontRenderContext(new AffineTransform(), true, true)).getWidth();
    }

    public String[] split(String text, Font font, int width) {
        if (text.isEmpty()) return new String[0];
        String[] tokens = text.split(" ");
        ArrayList<String> division = new ArrayList<>();

        int l = 0;
        while (l < tokens.length) {
            String current = tokens[l];
            int r = l + 1;
            while (r < tokens.length) {
                current += " " + tokens[r];
                if (getStringWidth(font, current) > width) {
                    break;
                }
                r++;
            }

            current = tokens[l++];
            while (l < r) {
                current += " " + tokens[l++];
            }
            division.add(current);
        }
        return division.toArray(new String[0]);
    }

        }
