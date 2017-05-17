package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.backend.player.widgets.stylesheets.PlateStyle;
import ru.ifmo.acm.backend.player.widgets.stylesheets.StatisticsStylesheet;
import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.ProblemInfo;
import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;

import java.awt.*;
import java.util.List;

import static java.lang.Math.*;

public class StatisticsWidget extends Widget {
    private static final double V = 1e-3;
    private static final Color BACKGROUND = new Color(0x333344);

    private final int leftX;
    private final int bottomY;
    private final int plateHeight;
    private final int width;

    private final int problemWidth;
    private final Font font;

    ContestInfo info;
    private int[] solved;
    private int[] pending;
    private int[] wrong;
    private int[] submitted;

    public StatisticsWidget(int leftX, int bottomY, int plateHeight, int width, long updateWait) {
        super(updateWait);

        this.leftX = leftX;
        this.bottomY = bottomY;
        this.plateHeight = plateHeight;

        this.width = width;
        problemWidth = (int) round(PROBLEM_WIDTH * plateHeight);

        font = Font.decode("Open Sans " + (int) (plateHeight * 0.7));

        setVisibilityState(0);
        setVisible(false);
    }

    @Override
    protected void updateImpl(Data data) {
        calculateStatistics();
        setVisible(data.statisticsData.isVisible());
        lastUpdate = System.currentTimeMillis();
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return data.statisticsData;
    }

    @Override
    public void paintImpl(Graphics g, int screenWidth, int screenHeight) {
        update();
        updateVisibilityState();
        if (visibilityState == 0) return;

        if (info == null) return;
        int height = plateHeight * (info.problemNumber + 1);

        g = g.create();
        g.translate(leftX, bottomY - height);

        g.drawRect(0, 0, width, plateHeight, BACKGROUND, opacity, Graphics.RectangleType.SOLID);

        drawTextInRect(g, "Teams solved for each problem", 0, 0, -1, plateHeight, Graphics.Alignment.LEFT,
                font, StatisticsStylesheet.header, visibilityState, WidgetAnimation.NOT_ANIMATED);

        List<ProblemInfo> problems = info.problems;

        int y = plateHeight;

        int fullWidth = width - problemWidth;

        for (int i = 0; i < problems.size(); i++) {
            ProblemInfo problem = problems.get(i);

            PlateStyle style = StatisticsStylesheet.problemAlias;
            if (wrong[i] > 0) {
                style = StatisticsStylesheet.waProblem;
            }
            if (pending[i] > 0) {
                style = StatisticsStylesheet.udProblem;
            }
            if (solved[i] > 0) {
                style = StatisticsStylesheet.acProblem;
            }

            drawTextInRect(g, problem.letter, 0, y, problemWidth,
                    plateHeight, Graphics.Alignment.CENTER, font,
                    style, visibilityState,
                    WidgetAnimation.NOT_ANIMATED);


            int maxNum = info.teamNumber + 3;
            int[] num = new int[]{solved[i], pending[i], wrong[i]};
            double[] len = new double[]{solved[i], pending[i], wrong[i]};
            int k = 0;
            for (int j = 0; j < 3; j++) {
                if (len[j] > 0) {
                    k++;
                }
            }
            if (k > 0) {
                for (int j = 0; j < 3; j++) {
                    if (len[j] == 0) {
                        for (int t = 0; t < 3; t++) {
                            if (len[t] > 0) len[t] += 1.0 / k;
                        }
                    }
                }
                for (int j = 0; j < 3; j++) {
                    if (len[j] > 0) len[j] += 1;
                }
            }
            PlateStyle[] styles = new PlateStyle[]{StatisticsStylesheet.acProblem,
                    StatisticsStylesheet.udProblem, StatisticsStylesheet.waProblem};

            int x = problemWidth;
            for (int j = 0; j < 3; j++){
                if (num[j] > 0) {
                    int w = (int) (fullWidth * len[j] / maxNum);
                    String text = "" + num[j];

                    drawTextInRect(g, text, x, y,
                            w, plateHeight, len[j] < 5 ? Graphics.Alignment.CENTER : Graphics.Alignment.RIGHT, font,
                            styles[j],
                            visibilityState, false, WidgetAnimation.NOT_ANIMATED, false);

                    x += w;
                }
            }
            g.drawRect(x, y, width - x, plateHeight, BACKGROUND, opacity, Graphics.RectangleType.SOLID);
            y += plateHeight;
        }
    }


    public void calculateStatistics() {
        info = Preparation.eventsLoader.getContestData();

        if (info == null) return;
        solved = new int[info.getProblemsNumber()];
        pending = new int[info.getProblemsNumber()];
        wrong = new int[info.getProblemsNumber()];
        submitted = new int[info.getProblemsNumber()];

        for (TeamInfo teamInfo : info.getStandings()) {
            List<RunInfo>[] runs = teamInfo.getRuns();
            for (int problemId = 0; problemId < runs.length; problemId++) {
                List<RunInfo> runInfos = runs[problemId];
                if (runInfos.size() > 0) {
                    submitted[problemId]++;
                    for (RunInfo runInfo : runInfos) {
                        if (runInfo.isAccepted()) {
                            solved[problemId]++;
                            break;
                        }
                        if (!runInfo.isJudged()) {
                            pending[problemId]++;
                            break;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < info.problemNumber; i++) {
            wrong[i] = submitted[i] - solved[i] - pending[i];
        }
    }

}
