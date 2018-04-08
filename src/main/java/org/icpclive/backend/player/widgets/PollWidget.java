package org.icpclive.backend.player.widgets;

import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.backend.player.widgets.stylesheets.PollStylesheet;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.webadmin.mainscreen.Polls.Poll;

import java.awt.*;
import java.util.Arrays;

/**
 * Created by Aksenov239 on 28.03.2017.
 */
public class PollWidget extends Widget {

    private final int leftX;
    private final int bottomY;

    private long duration;
    private long lastVisibleChange;
    private int topTeams;

    private int plateHeight;
    private int width;
    private int optionWidth;

    private Font font;
    private int minimalVoteWidth;
    private int total;
    private Poll.Option[] options;
    private double[] percent;

    public PollWidget(long updateWait, long duration, int topTeams, int DX, int plateHeight,
                      int width, int optionWidth, int minimalVoteWidth, int leftX, int bottomY) {
        super(updateWait);
        this.duration = duration;
        this.topTeams = topTeams;
        this.width = width;
        this.plateHeight = plateHeight;
        this.optionWidth = optionWidth;

        this.leftX = leftX;
        this.bottomY = bottomY;

        font = Font.decode(MAIN_FONT + " " + (int) (plateHeight * 0.7));

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
    protected void paintImpl(AbstractGraphics g, int screenWidth, int screenHeight) {
        update();
        updateVisibilityState();

        if (visibilityState == 0) return;

        calcPoll();

        int baseY = bottomY - (total + 1) * plateHeight;
        int baseX = leftX;

        g = g.create();
        g.translate(baseX, baseY);

        int y = 0;
        drawTextInRect(g, "Vote with hashtag " + pollToShow.getHashtag(), 0, y, width, plateHeight,
                PlateStyle.Alignment.RIGHT, font, PollStylesheet.hashtag,
                visibilityState, 1, WidgetAnimation.NOT_ANIMATED);

        drawTextInRect(g, pollToShow.getQuestion(), 0, 0, -1, plateHeight, PlateStyle.Alignment.LEFT,
                font, PollStylesheet.question, visibilityState, 1, WidgetAnimation.NOT_ANIMATED);

        y += plateHeight;

        double visibilityOption = 1. * optionWidth / this.width;
        for (int i = 0; i < total; i++) {
            drawTextInRect(g, options[i].option, 0, y, optionWidth, plateHeight,
                    PlateStyle.Alignment.CENTER, font, PollStylesheet.option,
                    Math.min(1, visibilityState / visibilityOption), 1, WidgetAnimation.NOT_ANIMATED);
            int voteWidth = (int) ((this.width - optionWidth - minimalVoteWidth) * percent[i]);
            int ww = (int)((minimalVoteWidth + voteWidth) * visibilityState);
            drawTextInRect(g, "" + options[i].votes, optionWidth, y,
                    ww, plateHeight,
                    PlateStyle.Alignment.CENTER, font, PollStylesheet.votes,
                    visibilityState,
                    1, WidgetAnimation.NOT_ANIMATED);
            g.drawRect(optionWidth + ww, y, this.width - (optionWidth + ww),
                    plateHeight, PollStylesheet.background, this.visibilityState, PlateStyle.RectangleType.SOLID);
            y += plateHeight;
        }
    }

    private void calcPoll() {
        if (pollToShow == null || visibilityState < 0.00001) {
            return;
        }

        options = pollToShow.getData();

        total = options.length;
        if (pollToShow.getTeamOptions()) { // Then this seems to be the team poll
            Arrays.sort(options, (o1, o2) ->
                    o1.votes == o2.votes ? o1.option.compareTo(o2.option) : o2.votes - o1.votes
            );
            for (total = 0; total < topTeams && options[total].votes >= 0; total++) { }
        }

        percent = new double[total];
        int sum = 0;
        for (Poll.Option option : options) {
            sum += option.votes;
        }
        for (int i = 0; i < total; i++) {
            percent[i] = sum == 0 ? 0 : 1. * options[i].votes / sum;
        }
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return data.pollData;
    }
}
