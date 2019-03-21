package org.icpclive.backend.player.widgets;

import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.player.PlayerInImage;
import org.icpclive.backend.player.widgets.stylesheets.PictureStylesheet;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.datapassing.VideoData;

import java.awt.*;

/**
 * Created by Meepo on 1/27/2019.
 */
public class VideoWidget extends Widget {
    private int rightX;
    private int captionWidth;
    private int captionY;
    private int rowHeight;

    private Font font;

    private volatile boolean asynchronousInitialization;

    private PlayerInImage player;
    private String caption;
    private String[] text;

    public VideoWidget(long updateWait, int rightX, int captionWidth, int captionY, int rowHeight) {
        super(updateWait);
        this.rightX = rightX;
        this.captionWidth = captionWidth;
        this.captionY = captionY;
        this.rowHeight = rowHeight;

        font = Font.decode(MAIN_FONT + " " + (int) Math.round(0.7 * rowHeight));
    }

    @Override
    public void updateImpl(Data data) {
        VideoData videoData = data.videoData;
//        System.err.println(isVisible() + " " + videoData.timestamp + " " + videoData.video + " " +
//                videoData.video + " " + player + " " + (player == null ? "false" : player.getPlayer().isPlaying()));
        if (videoData.video != null) {
            if (player == null) {
                if (asynchronousInitialization) {
                    return;
                }
                asynchronousInitialization = true;
                new Thread(() -> {
                    player = new PlayerInImage(-1, -1, null, videoData.video.getPath(), false);
                    asynchronousInitialization = false;
                }).start();
                caption = videoData.video.getCaption();
            } else {
                if (player.getPlayer().isPlaying()) {
                    setVisible(true);
                } else {
                    System.err.println("IS NOT PLAYING");
                    setVisible(false);
                }
            }
        } else {
            setVisible(false);
            if (player != null && visibilityState == 0) {
                player.stop();
                player = null;
            }
        }
    }

    @Override
    public void paintImpl(AbstractGraphics g, int width, int height) {
        super.paintImpl(g, width, height);

        if (visibilityState == 0) {
            return;
        }
        player.setVolume((int) (visibilityState * 100));

        int captionWidth = Math.min(this.captionWidth, player.getImage().getWidth());

        int textWidth = (int) (captionWidth - 2 * MARGIN * rowHeight);

        if (text == null) {
            if (caption != null) {
                text = split(caption, font, textWidth);
            } else {
                return;
            }
        }

        int currentX = (int) (BASE_WIDTH - visibilityState * (BASE_WIDTH - rightX + captionWidth));

        double savedVisibilityState = visibilityState;

        setVisibilityState(1);

        setFont(font);

        applyStyle(PictureStylesheet.text);

        drawRectangle(currentX, captionY, captionWidth, rowHeight * text.length);
        int currentY = captionY;
        for (String part : text) {
            drawTextThatFits(part, currentX, currentY, captionWidth, rowHeight + 1,
                    PlateStyle.Alignment.LEFT, false);
            currentY += rowHeight;
        }

//        player.getPlayer().getVideoDimension().
        int pictureWidth = player.getImage().getWidth();
        int pictureHeight = player.getImage().getHeight();

        if (pictureWidth > this.captionWidth) {
            pictureHeight = pictureHeight * captionWidth / pictureWidth;
            pictureWidth = this.captionWidth;
        }

        g.drawImage(player.getImage(), currentX, captionY - pictureHeight,
                pictureWidth, pictureHeight);

        setVisibilityState(savedVisibilityState);
    }

    public CachedData getCorrespondingData(Data data) {
        return data.videoData;
    }
}
