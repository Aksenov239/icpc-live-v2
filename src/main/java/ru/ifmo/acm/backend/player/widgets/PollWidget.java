package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.backend.player.widgets.stylesheets.PollStylesheet;
import ru.ifmo.acm.backend.player.widgets.stylesheets.StatisticsStylesheet;
import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.mainscreen.Polls.Poll;

import java.awt.*;
import java.util.Arrays;

/**
 * Created by Aksenov239 on 28.03.2017.
 */
public class PollWidget extends Widget {
    private long duration;
    private long lastVisibleChange;
    private int topTeams;

    private int DX;
    private int plateHeight;
    private int questionWidth;
    private int optionWidth;
    private int spaceY;
    private int spaceX;
    private Font font;
    private int minimalVoteWidth;

    public PollWidget(long updateWait, long duration, int topTeams, int DX, int plateHeight,
                      int questionWidth, int optionWidth, int minimalVoteWidth) {
        super(updateWait);
        this.duration = duration;
        this.topTeams = topTeams;
        this.DX = DX;
        this.questionWidth = questionWidth;
        this.plateHeight = plateHeight;
        this.optionWidth = optionWidth;

        font = Font.decode("Open Sans " + (int) (plateHeight * 0.7));

        spaceY = (int) (SPACE_Y * plateHeight);
        spaceX = (int) (SPACE_X * plateHeight);

        this.minimalVoteWidth = minimalVoteWidth;

        setVisibilityState(0);
        setVisible(false);
    }

    Poll pollToShow;

    @Override
    public void updateImpl(Data data) {
            if (data.pollData.isVisible ^ isVisible()) {
                lastVisibleChange = System.currentTimeMillis();
                setVisible(data.pollData.isVisible);
            }
            if (data.pollData.isVisible) {
                pollToShow = data.pollData.poll;
            }
    }

    @Override
    protected void paintImpl(Graphics g, int width, int height) {
        update();
        updateVisibilityState();

        if (pollToShow == null || visibilityState < 0.00001) {
            return;
        }

        Poll.Option[] options = pollToShow.getData();

        int total = options.length;
        if (pollToShow.getTeamOptions()) { // Then this seems to be the team poll
            Arrays.sort(options, (o1, o2) ->
                o1.votes == o2.votes ? o1.option.compareTo(o2.option) : o2.votes - o1.votes
            );
            for (total = 0; total < topTeams && options[total].votes >= 0; total++) { }
        } else {
            Arrays.sort(options, (o1, o2) -> o1.id - o2.id);
        }

        double[] percent = new double[total];
        int sum = 0;
        for (Poll.Option option : options) {
            sum += option.votes;
        }
        for (int i = 0; i < total; i++) {
            percent[i] = sum == 0 ? 0 : 1. * options[i].votes / sum;
        }

        int baseY = Widget.BASE_HEIGHT - 2 * CreepingLineWidget.HEIGHT - (total + 2) * (plateHeight + spaceY);
        int baseX = (Widget.BASE_WIDTH - DX) / 2 - questionWidth / 2;

        g = g.create();
        g.translate(baseX + DX, baseY);
        drawTextInRect(g, pollToShow.getQuestion(), 0, 0, questionWidth, plateHeight,
                Graphics.Alignment.CENTER, font, PollStylesheet.question,
                visibilityState, WidgetAnimation.UNFOLD_ANIMATED);
        int y = plateHeight + spaceY;
        drawTextInRect(g, pollToShow.getHashtag(), questionWidth / 2, y, -1, plateHeight,
                Graphics.Alignment.CENTER, font, PollStylesheet.hashtag,
                visibilityState, WidgetAnimation.UNFOLD_ANIMATED);
        y += plateHeight + spaceY;

        int total_percent = 100;
        double visibilityOption = 1. * optionWidth / questionWidth;
        for (int i = 0; i < total; i++) {
            drawTextInRect(g, options[i].option, 0, y, optionWidth, plateHeight,
                    Graphics.Alignment.CENTER, font, PollStylesheet.option,
                    Math.min(1, visibilityState / visibilityOption), WidgetAnimation.HORIZONTAL_ANIMATED);
            int voteWidth = (int) ((questionWidth - optionWidth - spaceX - minimalVoteWidth) * percent[i]);
            int now_percent = (int)Math.round(percent[i] * 100);
            total_percent -= now_percent;
            if (i == total - 1 && percent[i] != 0) {
                now_percent = total_percent;
            }
            drawTextInRect(g, now_percent + "%", optionWidth + spaceX, y,
                    minimalVoteWidth + voteWidth, plateHeight,
                    Graphics.Alignment.CENTER, font, PollStylesheet.votes,
                    (visibilityState - visibilityOption) / (1 - visibilityOption),
                    WidgetAnimation.HORIZONTAL_ANIMATED);
            y += plateHeight + spaceY;
        }
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return data.pollData;
    }
}
