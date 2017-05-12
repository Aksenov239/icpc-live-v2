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
import java.util.*;
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
    boolean showVerdict;

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

        setVisibilityState(1);
        setVisible(true);
    }

    @Override
    protected void updateImpl(Data data) {
        setVisible(data.queueData.isQueueVisible());
        lastUpdate = System.currentTimeMillis();
    }


    @Override
    public void paintImpl(Graphics g, int width, int height) {
        move(width, height, updateVisibilityState());
        g.clip(baseX - width, baseY - height, 2 * width, height);
        g.setFont(font);
        List<RunPlate> list = new ArrayList<>(plates.values());
        Collections.sort(list, (o1, o2) -> -Double.compare(o1.desiredPosition, o2.desiredPosition));
        for (RunPlate plate : list) {
            drawRun(g, baseX, baseY + (int) (plate.currentPosition * (plateHeight + spaceY)), plate);
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
                plate.visibilityState = Math.min(plate.visibilityState + dt * 0.002, 1);
            } else {
                plate.visibilityState = Math.max(plate.visibilityState - dt * 0.002, 0);
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

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return data.queueData;
    }

    private void drawRun(Graphics g, int x, int y, RunPlate plate) {
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
                rankWidth, plateHeight, Graphics.Alignment.CENTER,
                font, color, visibilityState * plate.visibilityState);

        x += rankWidth + spaceX;

        drawTextInRect(g, name, x, y,
                nameWidth, plateHeight, Graphics.Alignment.LEFT,
                font, teamColor, visibilityState * plate.visibilityState);

        x += nameWidth + spaceX;

        drawTextInRect(g, problem, x, y, problemWidth,
                plateHeight, Graphics.Alignment.CENTER, font, teamColor, visibilityState * plate.visibilityState);

        if (showVerdict) {
            x += problemWidth + spaceX;

            if (runInfo.getTime() > ContestInfo.FREEZE_TIME) {
                result = "?";
                resultColor = QueueStylesheet.frozenProblem;
                inProgress = false;
            }

            drawTextInRect(g, result, x, y, statusWidth,
                    plateHeight, Graphics.Alignment.CENTER, font, resultColor, visibilityState * plate.visibilityState);

            if (inProgress) {
                g.drawRect(x, y, progressWidth, plateHeight, QueueStylesheet.udTests, visibilityState * plate.visibilityState, Graphics.RectangleType.SOLID);
            }
            if (plate.runInfo == info.firstSolvedRun()[runInfo.getProblemNumber()]) {
                g.drawStar(x + statusWidth - STAR_SIZE, y + STAR_SIZE, STAR_SIZE);
            }
        } else {
            if (plate.runInfo == info.firstSolvedRun()[runInfo.getProblemNumber()]) {
                g.drawStar(x + problemWidth - STAR_SIZE, y + STAR_SIZE, STAR_SIZE);
            }
        }

    }

    private void calculateQueue() {
        info = Preparation.eventsLoader.getContestData();

        List<RunPlate> firstToSolves = new ArrayList<>();
        List<RunPlate> queue = new ArrayList<>();

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
                    firstToSolves.add(getRunPlate(r));
                }
            } else {
                //if (r.timestamp * 1000 > System.currentTimeMillis() - WAIT_TIME / WFEventsLoader.SPEED) {
                if (r.getLastUpdateTimestamp() > System.currentTimeMillis() - WAIT_TIME / EventsLoader.EMULATION_SPEED) {
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

                if (r == info.firstSolvedRun()[r.getProblemNumber()]) {
                    continue;
                } else {
                    if (r.getLastUpdateTimestamp() > System.currentTimeMillis() - WAIT_TIME / EventsLoader.EMULATION_SPEED) {
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
        Y_SHIFT = 0;
    }

}
