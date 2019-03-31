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
<<<<<<< HEAD

    private int baseX;
    private int baseY;
    private int width;
    private int height;
    private int textWidth;
=======
    private int rightX;
    private int widgetHeight;
    private int captionY;
>>>>>>> f05bd20e4066083065602ce412ab5d7943920256
    private int rowHeight;

    private Font font;

    private long lastTimestamp;

    private BufferedImage image;
    private String caption;
    private String[] text;

    public PictureWidget(long updateWait, int baseX, int baseY, int width, int height, int rowHeight) {
        super(updateWait);
        this.baseX = baseX;
        this.baseY = baseY;
        this.width = width;
        this.height = height;
        this.textWidth = (int) (width - 2 * MARGIN * rowHeight);
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
        if (image == null || text == null) {
            return;
        }

//        setBackgroundColor(Color.RED);
//        drawRectangle(baseX, baseY, this.width, this.height);

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

        double scale = Math.min(
                1.0 * (this.width) / image.getWidth(),
                1.0 * (this.height - rowHeight * text.length) / image.getHeight());

        int imageHeight = (int) (image.getHeight() * scale);
        int imageWidth = (int) (image.getWidth() * scale);

        int y = baseY + (this.height - (imageHeight + rowHeight * text.length)) / 2;

        g.drawImage(image, baseX, y,
                imageWidth, imageHeight, opacity);

        y += imageHeight;

        setFont(font);
        applyStyle(PictureStylesheet.text);

        drawRectangle(baseX, y, this.width, rowHeight * text.length);

        for (String part : text) {
            setTextOpacity(getTextOpacity(visibilityState));
            drawTextThatFits(part, baseX, y, this.width, rowHeight + 1,
                    PlateStyle.Alignment.LEFT, false);
            y += rowHeight;
        }
    }

    public CachedData getCorrespondingData(Data data) {
        return data.pictureData;
    }
}
