package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;

import java.awt.*;

/**
 * @author: pashka
 */
public class BreakingNewsWidget extends VideoWidget {
    private int x;
    private int y;
    private int width;
    private int height;
    private int duration;

    public BreakingNewsWidget(int x, int y, int width, int height, double aspectRatio, int sleepTime, int duration) {
        super(x, y, width, (int) (height / aspectRatio), sleepTime, 0);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.duration = duration;
    }

    private String caption;
    private RunInfo run;
    private TeamInfo team;
    private boolean isLive;

    @Override
    protected void updateImpl(Data data) {
        if (lastUpdate + duration < System.currentTimeMillis()) {
            setVisible(false);
        } else {
            if (!data.breakingNewsData.isVisible) {
                setVisible(false);
                return;
            }
            if (isVisible())
                return;
            setVisible(true);

            int teamId = data.breakingNewsData.teamId;
            int problemId = data.breakingNewsData.problemId;

            team = Preparation.eventsLoader.getContestData().getParticipant(teamId);
            java.util.List<RunInfo> runs = team.getRuns()[problemId];
            run = runs.get(runs.size() - 1);
            for (RunInfo run1 : runs) {
                if (run1.isAccepted()) {
                    run = run1;
                }
            }

            String url;
            if (data.breakingNewsData.isLive) {
                url = TeamWidget.getUrl(team, data.breakingNewsData.infoType);
            } else {
                url = run.toString();
            }

            change(url);
            isLive = data.breakingNewsData.isLive;
        }

        lastUpdate = System.currentTimeMillis();
    }

    @Override
    public void paintImpl(Graphics2D g, int width, int height) {
        update();

        if (!isVisible()) {
            return;
        }
    }
}
