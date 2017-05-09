package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.backend.player.widgets.stylesheets.PlateStyle;
import ru.ifmo.acm.backend.player.widgets.stylesheets.QueueStylesheet;
import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.TeamInfo;
import ru.ifmo.acm.events.EventsLoader;
import ru.ifmo.acm.events.RunInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueueWidget extends Widget {

    private static final double V = 0.01;

    public static final int WAIT_TIME = 60000;
    public static final int FIRST_TO_SOLVE_WAIT_TIME = 120000;
    private static final int MAX_QUEUE_SIZE = 16;

    public static double Y_SHIFT;

    private final int baseX;
    private final int baseY;
    private final int plateHeight;
    private final int spaceY;
    private final int spaceX;

    private final int nameWidth;
    private final int rankWidth;
    private final int problemWidth;
    private final int statusWidth;
    private final Font font;

    ContestInfo info;
    double[] currentPositions;
    double[] desiredPositions;
    boolean showVerdict;

    public QueueWidget(int baseX, int baseY, int plateHeight, long updateWait, boolean showVerdict) {
        super(updateWait);

        this.baseX = baseX;
        this.baseY = baseY;
        this.plateHeight = plateHeight;

        spaceX = (int) Math.round(plateHeight * SPACE_X);
        spaceY = (int) Math.round(plateHeight * SPACE_Y);

        nameWidth = (int) Math.round(NAME_WIDTH * plateHeight);
        rankWidth = (int) Math.round(RANK_WIDTH * plateHeight);
        problemWidth = (int) Math.round(PROBLEM_WIDTH * plateHeight);
        statusWidth = (int) Math.round(STATUS_WIDTH * plateHeight);

        font = Font.decode("Open Sans " + (int) (plateHeight * 0.7));

        currentPositions = new double[1000000];
        desiredPositions = new double[1000000];
        this.showVerdict = showVerdict;

        setVisibilityState(1);
        setVisible(true);
    }

    @Override
    protected void updateImpl(Data data) {
        calculateQueue();
        setVisible(data.queueData.isQueueVisible());
        lastUpdate = System.currentTimeMillis();
    }


    @Override
    public void paintImpl(Graphics g, int width, int height) {
        update();

        if (info == null) return;

        int dt = updateVisibilityState();
        g.clip(baseX - width, baseY - height, 2 * width, height);
        g.setFont(font);

        for (RunInfo r : info.getRuns()) {
            if (r == null)
                continue;
            int id = r.getId();

            double dp = dt * V;
            if (Math.abs(currentPositions[id] - desiredPositions[id]) < dp) {
                currentPositions[id] = desiredPositions[id];
            } else {
                if (desiredPositions[id] < currentPositions[id]) {
                    currentPositions[id] -= dp;
                } else {
                    currentPositions[id] += dp;
                }
            }
            if (currentPositions[id] < 0) {
                drawRun(g, baseX, baseY + (int) (currentPositions[id] * (plateHeight + spaceY)), r);
            }
        }
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return data.queueData;
    }

    private void drawRun(Graphics g, int x, int y, RunInfo run) {
        TeamInfo team = info.getParticipant(run.getTeamId());
        String name = team.getShortName();
        String problem = info.problems.get(run.getProblemNumber()).letter;
        String result = run.getResult();

        PlateStyle teamColor = QueueStylesheet.name;
        PlateStyle resultColor = QueueStylesheet.udProblem;

        boolean inProgress = false;
        int progressWidth = 0;

        if (run.isJudged()) {
            if (run.isAccepted()) {
                resultColor = teamColor = QueueStylesheet.acProblem;
            } else {
                resultColor = teamColor = QueueStylesheet.waProblem;
            }
        } else {
            inProgress = true;
            progressWidth = (int) Math.round(statusWidth * run.getPercentage());
        }

        if (desiredPositions[run.getId()] > 0) {
            teamColor = teamColor.darker();
            resultColor = resultColor.darker();
            return;
        }

        PlateStyle color = getTeamRankColor(team);

        drawTextInRect(g, "" + Math.max(team.getRank(), 1), x, y,
                rankWidth, plateHeight, Graphics.Alignment.CENTER,
                font, color, visibilityState, WidgetAnimation.HORIZONTAL_ANIMATED);

        x += rankWidth + spaceX;

        drawTextInRect(g, name, x, y,
                nameWidth, plateHeight, Graphics.Alignment.LEFT,
                font, teamColor, visibilityState, WidgetAnimation.HORIZONTAL_ANIMATED);

        x += nameWidth + spaceX;

        drawTextInRect(g, problem, x, y, problemWidth,
                plateHeight, Graphics.Alignment.CENTER, font, teamColor, visibilityState, WidgetAnimation.HORIZONTAL_ANIMATED);

        if (showVerdict) {
            x += problemWidth + spaceX;

            if (run.getTime() > ContestInfo.FREEZE_TIME) {
                result = "?";
                resultColor = QueueStylesheet.frozenProblem;
                inProgress = false;
            }

            drawTextInRect(g, result, x, y, statusWidth,
                    plateHeight, Graphics.Alignment.CENTER, font, resultColor, visibilityState, WidgetAnimation.UNFOLD_ANIMATED);

            if (inProgress) {
                g.drawRect(x, y, progressWidth, plateHeight, QueueStylesheet.udTests, visibilityState, Graphics.RectangleType.SOLID);
            }
            if (run == info.firstSolvedRun()[run.getProblemNumber()]) {
                g.drawStar(x + statusWidth - STAR_SIZE, y + STAR_SIZE, STAR_SIZE);
            }
        } else {
            if (run == info.firstSolvedRun()[run.getProblemNumber()]) {
                g.drawStar(x + problemWidth - STAR_SIZE, y + STAR_SIZE, STAR_SIZE);
            }
        }

    }

    public void calculateQueue() {
        info = Preparation.eventsLoader.getContestData();

        List<RunInfo> firstToSolves = new ArrayList<>();
        List<RunInfo> queue = new ArrayList<>();

        for (RunInfo r : info.getRuns()) {
            if (r == null)
                continue;

            if (r.getTimestamp() >= System.currentTimeMillis() / 1000) {
                continue;
            }

//            System.err.println(r.getTime() + " " + System.currentTimeMillis() + " " + (System.currentTimeMillis() - info.getStartTime()) + " " +
//                    info.getStartTime() + " " + (long)r.timestamp + " " + (r.timestamp * 1000 - info.getStartTime()));
            if (r == info.firstSolvedRun()[r.getProblemNumber()]) {
                //if (r.timestamp * 1000 > System.currentTimeMillis() - FIRST_TO_SOLVE_WAIT_TIME / WFEventsLoader.SPEED) {
                if (r.getLastUpdateTimestamp() > System.currentTimeMillis() - FIRST_TO_SOLVE_WAIT_TIME / EventsLoader.EMULATION_SPEED) {
                    firstToSolves.add(r);
                }
            } else {
                //if (r.timestamp * 1000 > System.currentTimeMillis() - WAIT_TIME / WFEventsLoader.SPEED) {
                if (r.getLastUpdateTimestamp() > System.currentTimeMillis() - WAIT_TIME / EventsLoader.EMULATION_SPEED) {
                    queue.add(r);
                }
            }
        }

        int extra = firstToSolves.size() + queue.size() - MAX_QUEUE_SIZE;
        if (extra > 0) {
            queue.clear();

            for (RunInfo r : info.getRuns()) {
                if (r == null)
                    continue;
                if (r.getTimestamp() >= System.currentTimeMillis() / 1000) {
                    continue;
                }

                if (r == info.firstSolvedRun()[r.getProblemNumber()]) {
                    continue;
                } else {
                    if (r.getLastUpdateTimestamp() > System.currentTimeMillis() - WAIT_TIME / EventsLoader.EMULATION_SPEED) {
                        if ((r.isJudged() || r.getTime() > ContestInfo.FREEZE_TIME) && extra > 0) {
                            extra--;
                            continue;

                        }
                        queue.add(r);
                    }
                }
            }
        }

        Arrays.fill(desiredPositions, 1);
        double pos = -queue.size();
        for (RunInfo r : queue) {
            desiredPositions[r.getId()] = pos - Y_SHIFT;
            pos += 1;
        }

        pos = -queue.size() - firstToSolves.size();
        if (queue.size() > 0) pos -= 0.5;
        for (RunInfo r : firstToSolves) {
            desiredPositions[r.getId()] = pos - Y_SHIFT;
            pos += 1;
        }
        Y_SHIFT = 0;
    }

}
