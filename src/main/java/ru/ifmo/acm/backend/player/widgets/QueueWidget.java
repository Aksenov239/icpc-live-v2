package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.TeamInfo;
import ru.ifmo.acm.events.WF.WFEventsLoader;
import ru.ifmo.acm.events.WF.WFRunInfo;

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

    public QueueWidget(int baseX, int baseY, int plateHeight, long updateWait) {
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
    public void paintImpl(Graphics2D g, int width, int height) {
        update();

        if (info == null) return;

        int dt = updateVisibilityState();
        g = (Graphics2D) g.create();
        g.translate(baseX, baseY);
        g.clip(new Rectangle(-width, -height, 2 * width, height));
        g.setFont(font);

        for (WFRunInfo r : (WFRunInfo[]) info.getRuns()) {
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
                drawRun(g, 0, (int) (currentPositions[id] * (plateHeight + spaceY)), r);
            }
        }
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return data.queueData;
    }

    private void drawRun(Graphics2D g, int x, int y, WFRunInfo run) {
        TeamInfo team = info.getParticipant(run.getTeamId());
        String name = team.getShortName();
        String problem = info.problems.get(run.getProblemNumber()).letter;
        String result = run.getResult();

        Color mainColor = MAIN_COLOR;
        Color teamColor = MAIN_COLOR;
        Color resultColor = MAIN_COLOR;

        boolean inProgress = false;
        int progressWidth = 0;

        if (run.judged) {
            if (run.isAccepted()) {
                resultColor = teamColor = GREEN_COLOR;
            } else {
                resultColor = teamColor = RED_COLOR;
            }
        } else {
            inProgress = true;
            progressWidth = (int) Math.round(statusWidth * 1.0 * run.getPassedTestsNumber() / run.getTotalTestsNumber());
        }

        if (desiredPositions[run.getId()] > 0) {
            mainColor = mainColor.darker();
            teamColor = teamColor.darker();
            resultColor = resultColor.darker();
            return;
        }

        Color color = getTeamRankColor(team);

        drawTextInRect(g, "" + Math.max(team.getRank(), 1), x, y,
                rankWidth, plateHeight, POSITION_CENTER,
                color, Color.white, visibilityState, WidgetAnimation.HORIZONTAL_ANIMATED);

        x += rankWidth + spaceX;

        drawTextInRect(g, name, x, y,
                nameWidth, plateHeight, POSITION_LEFT,
                teamColor, Color.white, visibilityState, WidgetAnimation.HORIZONTAL_ANIMATED);

        x += nameWidth + spaceX;

        drawTextInRect(g, problem, x, y, problemWidth,
                plateHeight, POSITION_CENTER, teamColor, Color.white, visibilityState, WidgetAnimation.HORIZONTAL_ANIMATED);

        x += problemWidth + spaceX;

        if (run.getTime() > WFEventsLoader.FREEZE_TIME) {
            result = "?";
            resultColor = MAIN_COLOR;
            inProgress = false;
        }

        drawTextInRect(g, result, x, y, statusWidth,
                plateHeight, POSITION_CENTER, resultColor, Color.white, visibilityState, WidgetAnimation.UNFOLD_ANIMATED);

        if (inProgress) {
            drawRect(g, x, y, progressWidth, plateHeight, YELLOW_COLOR.brighter(), visibilityState);
        }

        if (run == info.firstSolvedRun()[run.getProblemNumber()]) {
            drawStar(g, x + statusWidth - STAR_SIZE, y + STAR_SIZE, STAR_SIZE);
        }

    }

    public void calculateQueue() {
        info = Preparation.eventsLoader.getContestData();

        List<WFRunInfo> firstToSolves = new ArrayList<>();
        List<WFRunInfo> queue = new ArrayList<>();

        for (WFRunInfo r : (WFRunInfo[]) info.getRuns()) {
            if (r == null)
                continue;

//            System.err.println(r.getTime() + " " + System.currentTimeMillis() + " " + (System.currentTimeMillis() - info.getStartTime()) + " " +
//                    info.getStartTime() + " " + (long)r.timestamp + " " + (r.timestamp * 1000 - info.getStartTime()));
            if (r == info.firstSolvedRun()[r.getProblemNumber()]) {
                if (r.timestamp * 1000 > System.currentTimeMillis() - FIRST_TO_SOLVE_WAIT_TIME / WFEventsLoader.SPEED) {
                    firstToSolves.add(r);
                }
            } else {
                if (r.timestamp * 1000 > System.currentTimeMillis() - WAIT_TIME / WFEventsLoader.SPEED) {
                    queue.add(r);
                }
            }
        }

        int extra = firstToSolves.size() + queue.size() - MAX_QUEUE_SIZE;
        if (extra > 0) {
            queue.clear();

            for (WFRunInfo r : (WFRunInfo[]) info.getRuns()) {
                if (r == null)
                    continue;
                if (r == info.firstSolvedRun()[r.getProblemNumber()]) {
                    continue;
                } else {
                    if (r.getLastUpdateTimestamp() > System.currentTimeMillis() - WAIT_TIME / WFEventsLoader.SPEED) {
                        if ((r.isJudged() || r.getTime() > WFEventsLoader.FREEZE_TIME) && extra > 0) {
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
        for (WFRunInfo r : queue) {
            desiredPositions[r.getId()] = pos - Y_SHIFT;
            pos += 1;
        }

        pos = -queue.size() - firstToSolves.size();
        if (queue.size() > 0) pos -= 0.5;
        for (WFRunInfo r : firstToSolves) {
            desiredPositions[r.getId()] = pos - Y_SHIFT;
            pos += 1;
        }
        Y_SHIFT = 0;
    }

}
