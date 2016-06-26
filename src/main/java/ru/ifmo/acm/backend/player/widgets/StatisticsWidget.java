package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
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

    public static double Y_SHIFT;

    private final int baseX;
    private final int baseY;
    private final int plateHeight;
    private final int width;
    private final int spaceY;
    private final int spaceX;

    private final int problemWidth;
    private final Font font;
    private final int margin;

    ContestInfo info;
    int[] solved;
    int[] pending;
    int[] submitted;

    private double problemsVisibilityState = 0;
    private double statisticVisibilityState = 0;

    public StatisticsWidget(int baseX, int baseY, int plateHeight, int width, long updateWait) {
        super(updateWait);

        this.baseX = baseX;
        this.baseY = baseY;
        this.plateHeight = plateHeight;
        this.margin = 0;//(int) (plateHeight * MARGIN * 2);

        spaceX = (int) round(plateHeight * SPACE_X);
        spaceY = (int) round(plateHeight * SPACE_Y);

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
    protected int updateVisibilityState() {
        int dt = super.updateVisibilityState();
        double EPS = V * 0.01;
        if (isVisible()) {
            problemsVisibilityState = min(problemsVisibilityState + dt * V, 1);
            if (abs(problemsVisibilityState - 1) < EPS) {
                statisticVisibilityState = min(statisticVisibilityState + dt * V, 1);
            }
        } else {
            statisticVisibilityState = Math.max(statisticVisibilityState - dt * V, 0);
            if (abs(statisticVisibilityState) < EPS) {
                problemsVisibilityState = Math.max(problemsVisibilityState - dt * V, 0);
            }
        }

        return dt;
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return data.statisticsData;
    }

    @Override
    public void paintImpl(Graphics2D g, int width, int height) {
        update();

        int dt = updateVisibilityState();

        if (info == null) return;

        g = (Graphics2D) g.create();
        g.translate(baseX, baseY);
        g.setFont(font);

        drawTextInRect(g, "Statistics", 0, 0, -1, plateHeight, POSITION_LEFT,
                StatisticsStylesheet.header.background, StatisticsStylesheet.header.text, visibilityState, WidgetAnimation.VERTICAL_ANIMATED);

        int fullWidth = this.width - problemWidth - spaceX;

        List<ProblemInfo> problems = info.problems;

        int y = plateHeight + spaceY * BIG_SPACE_COUNT;
        double timePerProblem = 1.0 / problems.size();
        for (int problemId = 0; problemId < problems.size(); problemId++) {
            ProblemInfo problem = problems.get(problemId);
            double tmp = max(0, problemsVisibilityState - problemId * timePerProblem);
            tmp = tmp > timePerProblem ? 1 : tmp * problems.size();

            drawTextInRect(g, problem.letter, 0, y, problemWidth,
                    plateHeight, POSITION_CENTER, StatisticsStylesheet.problemAlias.background, StatisticsStylesheet.problemAlias.text, tmp, WidgetAnimation.VERTICAL_ANIMATED);

            y += plateHeight + spaceY;
        }


        y = plateHeight + spaceY * BIG_SPACE_COUNT;
        for (int problemId = 0; problemId < problems.size(); problemId++) {
            int x = problemWidth + spaceX;
            int wrong = submitted[problemId] - solved[problemId] - pending[problemId];
            int totalW = fullWidth * solved[problemId] / info.teamNumber +
                    fullWidth * pending[problemId] / info.teamNumber +
                    fullWidth * wrong / info.teamNumber;

            int shownWidth = (int) ceil(statisticVisibilityState * totalW);

            if (solved[problemId] > 0) {
                int w = fullWidth * solved[problemId] / info.teamNumber;
                String text = solved[problemId] < 2 ? "" : "" + solved[problemId];

                double visState = 1.0 * min(shownWidth, w) / w;
                drawTextInRect(g, text, x, y,
                        w, plateHeight, POSITION_CENTER, StatisticsStylesheet.acProblem.background, StatisticsStylesheet.acProblem.text,
                        visState, false, false, WidgetAnimation.HORIZONTAL_ANIMATED, false);

                shownWidth = max(0, shownWidth - w);
                x += w + spaceX;
            }

            if (pending[problemId] > 0) {
                int w = fullWidth * pending[problemId] / info.teamNumber;
                String text = pending[problemId] < 2 ? "" : "" + pending[problemId];

                double visState = 1.0 * min(shownWidth, w) / w;
                drawTextInRect(g, text, x, y,
                        w, plateHeight, POSITION_CENTER, StatisticsStylesheet.udProblem.background, StatisticsStylesheet.udProblem.text,
                        visState, false, false, WidgetAnimation.HORIZONTAL_ANIMATED, false);

                shownWidth = max(0, shownWidth - w);
                x += w + spaceX;
            }

            if (wrong > 0) {
                int w = fullWidth * wrong / info.teamNumber;
                String text = wrong < 2 ? "" : "" + wrong;
                double visState = 1.0 * min(shownWidth, w) / w;
                drawTextInRect(g, text, x, y,
                        w, plateHeight, POSITION_CENTER, StatisticsStylesheet.waProblem.background, StatisticsStylesheet.waProblem.text,
                        visState, false, false, WidgetAnimation.HORIZONTAL_ANIMATED, false);
            }

            y += plateHeight + spaceY;
        }
    }


    public void calculateStatistics() {
        info = Preparation.eventsLoader.getContestData();

        if (info == null) return;
        solved = new int[info.getProblemsNumber()];
        pending = new int[info.getProblemsNumber()];
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
    }

}
