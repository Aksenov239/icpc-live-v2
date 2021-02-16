package org.icpclive.backend.player.widgets;

import org.icpclive.events.ProblemInfo;
import org.icpclive.backend.Preparation;
import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.backend.player.widgets.stylesheets.StatisticsStylesheet;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.events.ContestInfo;
import org.icpclive.events.RunInfo;
import org.icpclive.events.TeamInfo;

import java.awt.*;
import java.util.List;

import static java.lang.Math.*;

public class StatisticsWidget extends Widget {
    public static final String HEADER = "STATISTICS";

    private final int leftX;
    private final int bottomY;
    private final int plateHeight;
    private final int width;

    private final int problemWidth;

    ContestInfo info;
    private int[] solved;
    private int[] pending;
    private int[] wrong;

    public StatisticsWidget(int leftX, int bottomY, int plateHeight, int width, long updateWait) {
        super(updateWait);

        this.leftX = leftX;
        this.bottomY = bottomY;
        this.plateHeight = plateHeight;

        this.width = width;
        problemWidth = (int) round(PROBLEM_WIDTH * plateHeight);

        setFont(Font.decode(MAIN_FONT + " "  + (int) (plateHeight * 0.7)));

        setVisibilityState(0);
        setVisible(false);
    }

    @Override
    protected void updateImpl(Data data) {
        if (data.statisticsData.isVisible()) {
            calculateStatistics();
        }
        setVisible(data.statisticsData.isVisible());
        lastUpdate = System.currentTimeMillis();
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return data.statisticsData;
    }

    @Override
    public void paintImpl(AbstractGraphics g, int screenWidth, int screenHeight) {
        super.paintImpl(g, screenWidth, screenHeight);

        if (visibilityState == 0) return;

        if (info == null) return;
        int height = plateHeight * (info.problemNumber + 1);


        graphics.translate(leftX, bottomY - height);

        int headerWidth = 228;

        applyStyle(StatisticsStylesheet.problemAlias);
        drawRectangle(headerWidth, 0, width - headerWidth, plateHeight);

        applyStyle(StatisticsStylesheet.header);
        drawRectangleWithText(HEADER, 0, 0, headerWidth, plateHeight, PlateStyle.Alignment.CENTER);

        List<ProblemInfo> problems = info.problems;

        int y = plateHeight;

        int fullWidth = width - problemWidth;

        for (int i = 0; i < problems.size(); i++) {
            ProblemInfo problem = problems.get(i);

            PlateStyle style = StatisticsStylesheet.problemAlias;
//            if (wrong[i] > 0) {
//                style = StatisticsStylesheet.waProblem;
//            }
//            if (pending[i] > 0) {
//                style = StatisticsStylesheet.udProblem;
//            }
//            if (solved[i] > 0) {
//                style = StatisticsStylesheet.acProblem;
//            }

            applyStyle(style);
            drawProblemPane(problem, 0, y, problemWidth, plateHeight);

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

                    applyStyle(styles[j]);
                    drawRectangleWithText(text, x, y, w, plateHeight,
                            len[j] < 5 ? PlateStyle.Alignment.CENTER : PlateStyle.Alignment.RIGHT, false, false);
                    x += w;
                }
            }
            applyStyle(StatisticsStylesheet.problemAlias);
            drawRectangle(x, y, width - x, plateHeight);
            y += plateHeight;
        }
    }


    public void calculateStatistics() {
        info = Preparation.eventsLoader.getContestData();

        if (info == null) return;
        solved = new int[info.getProblemsNumber()];
        pending = new int[info.getProblemsNumber()];
        wrong = new int[info.getProblemsNumber()];
        int[] submitted = new int[info.getProblemsNumber()];

        for (TeamInfo teamInfo : info.getStandings()) {
            List<? extends RunInfo>[] runs = teamInfo.getRuns();
            for (int problemId = 0; problemId < runs.length; problemId++) {
                List<? extends RunInfo> runInfos = runs[problemId];
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
