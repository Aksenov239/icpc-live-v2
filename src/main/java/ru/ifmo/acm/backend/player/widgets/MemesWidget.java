package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;

/**
 * Created by Meepo on 4/22/2017.
 */
public class MemesWidget extends Widget {
    String previous;
    int previousCount;
    String meme;
    int count;
    boolean visible;
    long lastChange;

    double visibilityRectangle;
    double visibilityText;

    int flickerTime;
    MemeStatus status;

    double V = 0;

    private enum MemeStatus {
        OPEN,
        FLICKER,
        CLOSE
    }

    public MemesWidget(int updateWait, int flickerTime) {
        super(updateWait);
        V = 2. / flickerTime;
    }

    @Override
    public void updateImpl(Data data) {
        if (data.memesData.isVisible) {
            if (!visible) {
                previous = null;
                meme = data.memesData.currentMeme;
                count = data.memesData.count;
                status = MemeStatus.OPEN;
            } else {
                if (!previous.equals(data.memesData.currentMeme)) {
                    previous = meme;
                    previousCount = count;
                    meme = data.memesData.currentMeme;
                    count = data.memesData.count;
                    lastChange = System.currentTimeMillis();
                    status = MemeStatus.FLICKER;
                }
            }
            visible = true;
        } else {
            status = MemeStatus.CLOSE;
            visible = false;
        }
    }

    public void updateVisibility() {
        long time = System.currentTimeMillis();
        if (last == 0) {
            visibilityRectangle = visible ? 1 : 0;
            visibilityText = visible ? 1 : 0;
        }
        int dt = last == 0 ? 0 : (int) (time - last);
        last = time;
        if (status == MemeStatus.OPEN) {
            visibilityRectangle = Math.min(visibilityRectangle + V * dt, 1);
        } if (status == MemeStatus.CLOSE) {
            visibilityRectangle = Math.max(visibilityRectangle - V * dt, 0);
        } else {
            if (time - lastChange < flickerTime / 2) {
                visibilityText = Math.max(visibilityText - V * dt, 0);
            } else {
                visibilityText = Math.min(visibilityText + V * dt, 1);
            }
        }
        return;
    }

    @Override
    public void paintImpl(Graphics g, int width, int height) {
        if (visibilityRectangle == 0) {
            return;
        }
    }

    @Override
    public CachedData getCorrespondingData(Data data) {
        return data.memesData;
    }
}
