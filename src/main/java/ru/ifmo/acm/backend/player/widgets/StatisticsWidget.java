package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.ProblemInfo;
import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;

import java.awt.*;
import java.util.List;

public class StatisticsWidget extends Widget {

    private static final double V = 0.01;

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

    public StatisticsWidget(int baseX, int baseY, int plateHeight, int width, long updateWait) {
        super(updateWait);

        this.baseX = baseX;
        this.baseY = baseY;
        this.plateHeight = plateHeight;
        this.margin = 0;//(int) (plateHeight * MARGIN * 2);

        spaceX = (int) Math.round(plateHeight * SPACE_X);
        spaceY = (int) Math.round(plateHeight * SPACE_Y);

        this.width = width;
        problemWidth = (int) Math.round(PROBLEM_WIDTH * plateHeight);

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
    public void paintImpl(Graphics2D g, int width, int height) {
        update();

        if (info == null) return;

        int dt = updateVisibilityState();
        g = (Graphics2D) g.create();
        g.translate(baseX, baseY);
        g.setFont(font);

        drawTextInRect(g, "Statistics", 0, 0, -1, plateHeight, POSITION_LEFT,
                ACCENT_COLOR, Color.white, visibilityState);

        int y = plateHeight + spaceY * BIG_SPACE_COUNT;

        int fullWidth = this.width - problemWidth - spaceX;

        List<ProblemInfo> problems = info.problems;
        for (int problemId = 0; problemId < problems.size(); problemId++) {
            ProblemInfo problem = problems.get(problemId);
            int x = 0;

            drawTextInRect(g, problem.letter, x, y, problemWidth,
                    plateHeight, POSITION_CENTER, MAIN_COLOR, Color.white, visibilityState);

            x += problemWidth + spaceX;

            if (solved[problemId] > 0) {
                int w = fullWidth * solved[problemId] / info.teamNumber;
                String text = solved[problemId] < 2 ? "" : "" + solved[problemId];
                drawTextInRect(g, text, x, y,
                        w, plateHeight, POSITION_CENTER, GREEN_COLOR, Color.white,
                        visibilityState, false, false);
                x += w + spaceX;
            }
            if (pending[problemId] > 0) {
                int w = fullWidth * pending[problemId] / info.teamNumber;
                String text = pending[problemId] < 2 ? "" : "" + pending[problemId];
                drawTextInRect(g, text, x, y,
                        w, plateHeight, POSITION_CENTER, YELLOW_COLOR, Color.white,
                        visibilityState, false, false);
                x += w + spaceX;
            }
            int wrong = submitted[problemId] - pending[problemId] - solved[problemId];
            if (wrong > 0) {
                int w = fullWidth * wrong / info.teamNumber;
                String text = wrong < 2 ? "" : "" + wrong;
                drawTextInRect(g, text, x, y,
                        w, plateHeight, POSITION_CENTER, RED_COLOR, Color.white,
                        visibilityState, false, false);
                x += w + spaceX;
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
                        }
                    }
                }
            }
        }
    }

}
