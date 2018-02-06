package org.icpclive.backend.player.widgets;

import org.icpclive.backend.Preparation;
import org.icpclive.backend.player.widgets.stylesheets.QueueStylesheet;
import org.icpclive.events.ContestInfo;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.events.TeamInfo;
import org.icpclive.events.EventsLoader;
import org.icpclive.events.RunInfo;

import java.awt.*;
import java.util.*;
import java.util.List;

public class QueueWidget extends Widget {

    private static final double V = 0.005;

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

    private final int videoWidth;
    private final int videoHeight;

    ContestInfo info;
    boolean showVerdict;

    NewBreakingNewsWidget breakingNews;
    private RunPlate breaking;

    class RunPlate {
        double currentPosition;
        double desiredPosition;
        double visibilityState;
        RunInfo runInfo;
        boolean visible;

        public RunPlate(RunInfo runInfo) {
            this.runInfo = runInfo;
        }
    }

    Map<Integer, RunPlate> plates = new HashMap<>();

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

        this.showVerdict = showVerdict;

        videoWidth = problemWidth + nameWidth + rankWidth + statusWidth;
        videoHeight = videoWidth * 9 / 16;

        breakingNews = new NewBreakingNewsWidget(updateWait, videoWidth, videoHeight);

        setVisibilityState(1);
        setVisible(true);
    }

    @Override
    public void paint(org.icpclive.backend.graphics.Graphics g, int width, int height, double scale) {
        super.paint(g, width, height, scale);
        breakingNews.paint(g, width, height, scale);
    }

    @Override
    public void paintImpl(org.icpclive.backend.graphics.Graphics g, int width, int height) {
        move(width, height, updateVisibilityState());
        g.clip(baseX - width, baseY - height, 2 * width, height);
        g.setFont(font);
        List<RunPlate> list = new ArrayList<>(plates.values());
        Collections.sort(list, (o1, o2) -> -Double.compare(o1.desiredPosition, o2.desiredPosition));
        for (RunPlate plate : list) {
            drawRun(g, baseX, baseY + (int) (plate.currentPosition * (plateHeight + spaceY)), plate);
        }
        if (breaking != null) {
            breakingNews.setPosition(baseX, baseY + (int) (breaking.currentPosition * (plateHeight + spaceY)) - videoHeight);
        }
    }


    @Override
    protected void move(int width, int height, int dt) {
        calculateQueue();
        if (info == null) return;

        for (RunInfo r : info.getRuns()) {
            if (r == null)
                continue;
            RunPlate plate = getRunPlate(r);

            double dp = dt * V;
            if (Math.abs(plate.currentPosition - plate.desiredPosition) < dp) {
                plate.currentPosition = plate.desiredPosition;
            } else {
                if (plate.desiredPosition < plate.currentPosition) {
                    plate.currentPosition -= dp;
                } else {
                    plate.currentPosition += dp;
                }

            }
            if (plate.visible) {
                plate.visibilityState = Math.min(plate.visibilityState + dt * 0.001, 1);
            } else {
                plate.visibilityState = Math.max(plate.visibilityState - dt * 0.001, 0);
            }
            if (plate.visibilityState == 0) {
                plates.remove(plate.runInfo.getId());
            }
        }

    }

    private RunPlate getRunPlate(RunInfo r) {
        RunPlate plate = plates.get(r.getId());
        if (plate == null) {
            plate = new RunPlate(r);
            plates.put(r.getId(), plate);
        }
        return plate;
    }

    private void drawRun(org.icpclive.backend.graphics.Graphics g, int x, int y, RunPlate plate) {

        boolean blinking = breakingNews.isVisible() && plate == breaking;

        RunInfo runInfo = plate.runInfo;
        TeamInfo team = info.getParticipant(runInfo.getTeamId());
        String name = team.getShortName();
        String problem = ContestInfo.problems.get(runInfo.getProblemNumber()).letter;
        String result = runInfo.getResult();

        PlateStyle teamColor = QueueStylesheet.name;
        PlateStyle resultColor = QueueStylesheet.udProblem;

        boolean inProgress = false;
        int progressWidth = 0;

        if (runInfo.isJudged()) {
            if (runInfo.isAccepted()) {
                resultColor = teamColor = QueueStylesheet.acProblem;
            } else {
                resultColor = teamColor = QueueStylesheet.waProblem;
            }
        } else {
            inProgress = true;
            progressWidth = (int) Math.round(statusWidth * runInfo.getPercentage());
        }

//        if (plate.desiredPosition > 0) {
//            return;
//        }

        PlateStyle color = getTeamRankColor(team);

        drawTextInRect(g, "" + Math.max(team.getRank(), 1), x, y,
                rankWidth, plateHeight, org.icpclive.backend.graphics.Graphics.Alignment.CENTER,
                font, color, visibilityState * plate.visibilityState,
                true, WidgetAnimation.NOT_ANIMATED, blinking);

        x += rankWidth + spaceX;

        drawTextInRect(g, name, x, y,
                nameWidth, plateHeight, org.icpclive.backend.graphics.Graphics.Alignment.LEFT,
                font, teamColor, visibilityState * plate.visibilityState,
                true, WidgetAnimation.NOT_ANIMATED, blinking);

        x += nameWidth + spaceX;

        drawTextInRect(g, problem, x, y, problemWidth,
                plateHeight, org.icpclive.backend.graphics.Graphics.Alignment.CENTER, font, teamColor, visibilityState * plate.visibilityState,
                true, WidgetAnimation.NOT_ANIMATED, blinking);

        if (showVerdict) {
            x += problemWidth + spaceX;

            if (runInfo.getTime() > ContestInfo.FREEZE_TIME) {
                result = "?";
                resultColor = QueueStylesheet.frozenProblem;
                inProgress = false;
            }

            drawTextInRect(g, result, x, y, statusWidth,
                    plateHeight, org.icpclive.backend.graphics.Graphics.Alignment.CENTER, font,
                    resultColor, visibilityState * plate.visibilityState,
                    true, WidgetAnimation.NOT_ANIMATED, blinking);

            if (inProgress) {
                g.drawRect(x, y, progressWidth, plateHeight, QueueStylesheet.udTests,
                        visibilityState * plate.visibilityState, org.icpclive.backend.graphics.Graphics.RectangleType.SOLID);
            }
            if (plate.runInfo == info.firstSolvedRun()[runInfo.getProblemNumber()]) {
                drawStar(g, x + statusWidth - STAR_SIZE, y + 2 * STAR_SIZE, STAR_SIZE);
            }
        } else {
            if (plate.runInfo == info.firstSolvedRun()[runInfo.getProblemNumber()]) {
                drawStar(g, x + problemWidth - STAR_SIZE, y + 2 * STAR_SIZE, STAR_SIZE);
            }
        }

    }

    private void calculateQueue() {
        info = Preparation.eventsLoader.getContestData();

        for (RunPlate plate : plates.values()) {
            plate.visible = false;
            plate.desiredPosition = 0;
        }

        RunPlate breaking = null;
        if (breakingNews.visibilityState > 0.01) {
            RunInfo run = breakingNews.getRun();
            breaking = this.breaking = getRunPlate(run);
        }
        List<RunPlate> firstToSolves = new ArrayList<>();
        List<RunPlate> queue = new ArrayList<>();

        for (RunInfo r : info.getRuns()) {
            if (r == null)
                continue;
            if (breaking != null && breaking.runInfo == r) {
                continue;
            }

//            if (r.getTimestamp() >= System.currentTimeMillis() / 1000) {
//                continue;
//            }

//            System.err.println(r.getTime() + " " + System.currentTimeMillis() + " " + (System.currentTimeMillis() - info.getStartTime()) + " " +
//                    info.getStartTime() + " " + (long)r.timestamp + " " + (r.timestamp * 1000 - info.getStartTime()));
            if (r == info.firstSolvedRun()[r.getProblemNumber()]) {
                //if (r.timestamp * 1000 > System.currentTimeMillis() - FIRST_TO_SOLVE_WAIT_TIME / WFEventsLoader.SPEED) {
                if (r.getLastUpdateTimestamp() > System.currentTimeMillis() - FIRST_TO_SOLVE_WAIT_TIME / EventsLoader.EMULATION_SPEED) {
                    firstToSolves.add(getRunPlate(r));
                }
            } else {
                //if (r.timestamp * 1000 > System.currentTimeMillis() - WAIT_TIME / WFEventsLoader.SPEED) {
                if (r.getLastUpdateTimestamp() >
                        System.currentTimeMillis() - WAIT_TIME / EventsLoader.EMULATION_SPEED ||
                        (!r.isJudged() && r.getTime() <= ContestInfo.FREEZE_TIME)) {
                    queue.add(getRunPlate(r));
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
                if (breaking != null && breaking.runInfo == r) {
                    continue;
                }

                if (r == info.firstSolvedRun()[r.getProblemNumber()]) {
                    continue;
                } else {
                    if (r.getLastUpdateTimestamp() >
                            System.currentTimeMillis() - WAIT_TIME / EventsLoader.EMULATION_SPEED ||
                            (!r.isJudged() && r.getTime() <= ContestInfo.FREEZE_TIME)) {
                        if ((r.isJudged() || r.getTime() > ContestInfo.FREEZE_TIME) && extra > 0) {
                            extra--;
                            continue;

                        }
                        queue.add(getRunPlate(r));
                    }
                }
            }
        }

        for (RunPlate plate : plates.values()) {
            plate.visible = false;
            plate.desiredPosition = 0;
        }
        double pos = -queue.size();
        for (RunPlate plate : queue) {
            plate.desiredPosition = pos - Y_SHIFT;
            plate.visible = true;
            pos += 1;
        }

        pos = -queue.size() - firstToSolves.size();
        if (queue.size() > 0) pos -= 0.5;
        for (RunPlate plate : firstToSolves) {
            plate.desiredPosition = pos - Y_SHIFT;
            plate.visible = true;
            pos += 1;
        }

        pos -= firstToSolves.size() + 1;
        if (firstToSolves.size() > 0) pos -= 0.5;
        if (breaking != null) {
            breaking.desiredPosition = pos - Y_SHIFT;
            breaking.visible = true;
        }

        Y_SHIFT = 0;
    }

    @Override
    protected void update() {
        super.update();
        breakingNews.update();
    }

    @Override
    protected void updateImpl(Data data) {
        setVisible(data.queueData.isQueueVisible());
        lastUpdate = System.currentTimeMillis();
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return data.queueData;
    }

}
