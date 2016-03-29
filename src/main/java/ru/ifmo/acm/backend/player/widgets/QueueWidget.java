package ru.ifmo.acm.backend.player.widgets;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.TeamInfo;
import ru.ifmo.acm.events.WF.WFEventsLoader;
import ru.ifmo.acm.events.WF.WFRunInfo;

public class QueueWidget extends Widget {

    private static final double V = 0.01;

    public static final int WAIT_TIME = 6000;
    public static final int FIRST_TO_SOLVE_WAIT_TIME = 12000;

    private static final double SPACE_Y = 0.1;
    private static final double SPACE_X = 0.05;
    private static final double NAME_WIDTH = 6;
    private static final double RANK_WIDTH = 1.6;
    private static final double PROBLEM_WIDTH = 1;
    private static final double STATUS_WIDTH = 2;

    private static final int STAR_SIZE = 5;

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
        lastUpdate = System.currentTimeMillis();
    }


    @Override
    public void paintImpl(Graphics2D g, int width, int height) {
        update();

        int dt = updateVisibilityState();
        g = (Graphics2D) g.create();
        g.translate(baseX, baseY);
        g.clip(new Rectangle(-width, -height, 2 * width, height));


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
//        g.drawString("" + z, 0, -200);

//        if (yShift > 0) {
//            yShift = (int) (yShift * 0.8);
//        }
//
//        g.setFont(Font.decode("Open Sans Italic " + 20));
//        drawTextInRect(g, "Current queue size: " + queue.size(), 50, 50, 400, 20, POSITION_CENTER, Color.RED,
//                Color.BLACK, opacity);
//        for (int i = 0; i < queue.size(); i++) {
//            WFRunInfo wfr = (WFRunInfo) queue.get(i);
//
//            TeamInfo teamId = info.getParticipant(wfr.getTeamId());
//            Color teamNameColor = Color.BLUE;
//
//            String rank = String.valueOf(teamId.getRank());
//            String teamName = teamId.getShortName();
//            int problemNumber = wfr.getProblemNumber();
//            String problemName = "" + (char) ('A' + problemNumber);
//            String status = "";
//            if (wfr.judged) {
//                if (wfr.isAccepted()) {
//                    drawRect(g, 50 + 466, 50 + (HEIGHT * (i + 1)) + yShift, 100, HEIGHT, Color.GREEN, opacity);
//                    status = "AC";
//                    teamNameColor = Color.GREEN;
//                } else {
//                    drawRect(g, 50 + 466, 50 + (HEIGHT * (i + 1)) + yShift, 100, HEIGHT, Color.RED, opacity);
//                    status = wfr.getResult();
//                }
//            } else {
//                drawRect(g, 50 + 466, 50 + (HEIGHT * (i + 1)) + yShift, 100, HEIGHT, Color.LIGHT_GRAY, opacity);
//                drawRect(g, 50 + 466, 50 + (HEIGHT * (i + 1)) + yShift,
//                        (int) (100 * 1.0 * wfr.getPassedTestsNumber() / wfr.getTotalTestsNumber()), HEIGHT,
//                        Color.YELLOW, opacity);
//                status = String.valueOf(wfr.getPassedTestsNumber());
//            }
//
//            drawTextInRect(g, rank, 50, 50 + (HEIGHT * (i + 1)) + yShift, 30, HEIGHT, POSITION_CENTER, Color.GRAY,
//                    Color.WHITE, opacity);
//            drawTextInRect(g, teamName, 50 + 32, 50 + (HEIGHT * (i + 1)) + yShift, 400, HEIGHT, POSITION_CENTER,
//                    teamNameColor, Color.WHITE, opacity);
//            drawTextInRect(g, problemName, 50 + 434, 50 + (HEIGHT * (i + 1)) + yShift, 30, HEIGHT, POSITION_CENTER,
//                    Color.DARK_GRAY, Color.WHITE, opacity);
//
//            drawTextInRect(g, status, 50 + 466, 50 + (HEIGHT * (i + 1)) + yShift, 100, HEIGHT, POSITION_CENTER,
//                    new Color(0, 0, 0, 0), Color.WHITE, opacity);
//
//        }

    }

    private void drawRun(Graphics2D g, int x, int y, WFRunInfo run) {
        g.setFont(font);
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
                color, Color.white, visibilityState);

        x += rankWidth + spaceX;

        drawTextInRect(g, name, x, y,
                nameWidth, plateHeight, POSITION_LEFT,
                teamColor, Color.white, visibilityState);

        x += nameWidth + spaceX;

        drawTextInRect(g, problem, x, y, problemWidth,
                plateHeight, POSITION_CENTER, teamColor, Color.white, visibilityState);

        x += problemWidth + spaceX;

        if (run.getTime() > WFEventsLoader.FREEZE_TIME) {
            return;
        }

        drawTextInRect(g, result, x, y, statusWidth,
                plateHeight, POSITION_CENTER, resultColor, Color.white, visibilityState);

        if (inProgress) {
            drawRect(g, x, y, progressWidth, plateHeight, YELLOW_COLOR.brighter(), visibilityState);
        }

        if (run == info.firstSolvedRun()[run.getProblemNumber()]) {
            drawStar(g, x + statusWidth, y, STAR_SIZE);
        }

    }

    public void calculateQueue() {
        info = Preparation.eventsLoader.getContestData();

        List<WFRunInfo> firstToSolves = new ArrayList<>();
        List<WFRunInfo> queue = new ArrayList<>();

        for (WFRunInfo r : (WFRunInfo[]) info.getRuns()) {
            if (r == null)
                continue;
            if (r == info.firstSolvedRun()[r.getProblemNumber()]) {
                if (r.getLastUpdateTimestamp() > System.currentTimeMillis() - FIRST_TO_SOLVE_WAIT_TIME) {
                    firstToSolves.add(r);
                }
            } else {
                if (r.getLastUpdateTimestamp() > System.currentTimeMillis() - WAIT_TIME) {
                    queue.add(r);
                }
            }
        }

        Arrays.fill(desiredPositions, 1);
        double pos = -queue.size();
        for (WFRunInfo r : queue) {
            desiredPositions[r.getId()] = pos;
            pos += 1;
        }

        pos = -queue.size() - firstToSolves.size();
        if (queue.size() > 0) pos -= 0.5;
        for (WFRunInfo r : firstToSolves) {
            desiredPositions[r.getId()] = pos;
            pos += 1;
        }

    }

}
