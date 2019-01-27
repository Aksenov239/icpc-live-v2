package org.icpclive.backend.player.widgets;

import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.graphics.GraphicsSWT;
import org.icpclive.backend.player.widgets.stylesheets.FactStylesheet;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;
import org.icpclive.datapassing.FactData;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by vaksenov on 13.04.2018.
 */
public class FactWidget extends Widget {
    private int leftX;
    private int topY;
    private int factWidth;
    private int actualWidth;
    private int rowHeight;

    private String title;
    private String fullText;
    private String[] text;

    Font font;

    private long lastTimestamp;

    public FactWidget(long updateWait, int leftX, int topY, int factWidth, int rowHeight) {
        super(updateWait);
        this.leftX = leftX;
        this.topY = topY;
        this.factWidth = factWidth;
        this.actualWidth = (int) (factWidth - 2 * MARGIN * rowHeight);
        this.rowHeight = rowHeight;

        font = Font.decode(MAIN_FONT + " " + (int) Math.round(rowHeight * 0.7));
    }

    @Override
    public void updateImpl(Data data) {
        FactData factData = data.factData;
        if (lastTimestamp == factData.timestamp) {
            return;
        }
        lastTimestamp = factData.timestamp;
        if (factData.isVisible) {
            title = factData.factTitle;
            fullText = factData.factText;
            text = null;
            setVisible(true);
        } else {
            setVisible(false);
        }
    }

    @Override
    public void paintImpl(AbstractGraphics g, int width, int height) {
        super.paintImpl(g, width, height);

        if (fullText != null && text == null) {
            text = split(fullText, font, actualWidth);
        }
        if (text == null) {
            return;
        }

        int currentX = (int) (BASE_WIDTH - visibilityState * (BASE_WIDTH - leftX));

        double savedVisibilityState = visibilityState;
        setVisibilityState(1);

        setFont(font);

        int currentY = topY;
        applyStyle(FactStylesheet.title);
        drawRectangleWithText(title, currentX, currentY, this.factWidth, rowHeight + 1,
                PlateStyle.Alignment.LEFT);

        applyStyle(FactStylesheet.text);
        currentY += rowHeight;
        drawRectangle(currentX, currentY, this.factWidth, this.rowHeight * text.length);
        for (String part : text) {
            drawTextThatFits(part, currentX, currentY, this.factWidth, rowHeight + 1,
                    PlateStyle.Alignment.LEFT, false);
            currentY += rowHeight;
        }

        setVisibilityState(savedVisibilityState);
    }

    public CachedData getCorrespondingData(Data data) {
        return data.factData;
    }
}
