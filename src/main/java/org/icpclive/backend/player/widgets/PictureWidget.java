package org.icpclive.backend.player.widgets;

import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.player.widgets.stylesheets.PictureStylesheet;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.datapassing.PictureData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Meepo on 1/27/2019.
 */
public class PictureWidget extends Widget {
    private int rightX;
    private int widgetHeight;
    private int captionY;
    private int rowHeight;

    private Font font;

    private long lastTimestamp;

    private BufferedImage image;
    private String caption;
    private String[] text;

    public PictureWidget(long updateWait, int rightX, int widgetHeight, int captionY, int rowHeight) {
        super(updateWait);
        this.rightX = rightX;
        this.widgetHeight = widgetHeight;
        this.captionY = captionY;
        this.rowHeight = rowHeight;

        font = Font.decode(MAIN_FONT + " " + (int) Math.round(0.7 * rowHeight));
    }

    @Override
    public void updateImpl(Data data) {
        PictureData pictureData = data.pictureData;
        lastTimestamp = pictureData.timestamp;
        if (pictureData.picture != null) {
            if (image == null) {
                setVisible(true);
                System.err.println(pictureData.picture.getPath() + " " + pictureData.picture.getCaption());
                try {
                    image = ImageIO.read(new File(pictureData.picture.getPath()));
                    caption = pictureData.picture.getCaption();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            setVisible(false);
            if (visibilityState == 0) {
                image = null;
                text = null;
            }
        }
    }

    @Override
    public void paintImpl(AbstractGraphics g, int width, int height) {
        super.paintImpl(g, width, height);

        if (visibilityState == 0) {
            return;
        }

        int widgetWidth = this.widgetHeight * image.getWidth() / image.getHeight();

        int captionWidth = Math.min(widgetWidth, image.getWidth());
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

        int pictureWidth = image.getWidth();
        int pictureHeight = image.getHeight();

        if (pictureHeight > widgetHeight) {
            pictureHeight = widgetHeight;
            pictureWidth = widgetWidth;
        }

        g.drawImage(image, currentX, captionY - pictureHeight,
                pictureWidth, pictureHeight);

        setVisibilityState(savedVisibilityState);
    }

    public CachedData getCorrespondingData(Data data) {
        return data.pictureData;
    }
}
