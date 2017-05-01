package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.backend.graphics.GraphicsSWT;
import ru.ifmo.acm.backend.player.widgets.stylesheets.MemesStylesheet;
import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

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

    int widthR;
    int heightR;
    int Y;
    int DX;
    int IMAGE_WIDTH;

    Font font;

    int flickerTime;
    MemeStatus status;

    double V = 0;

    private enum MemeStatus {
        OPEN,
        FLICKER,
        CLOSE
    }

    HashMap<String, BufferedImage> memesImages;

    public MemesWidget(long updateWait, int flickerTime, int Y, int widthR, int heightR) {
        super(updateWait);
        this.flickerTime = flickerTime;
        V = 2. / flickerTime;
        this.widthR = widthR;
        IMAGE_WIDTH = (int) (this.widthR * 0.5);
        this.DX = (int) (0.05 * widthR);
        this.heightR = heightR;
        this.Y = Y;
        font = new Font("Open Sans", Font.BOLD, 40);

        Properties properties = new Properties();
        try {
            properties.load(MemesWidget.class.getClassLoader().getResourceAsStream("mainscreen.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] memes = properties.getProperty("memes", "goose").split(";");
        memesImages = new HashMap<>();
        for (String meme : memes) {
            try {
                BufferedImage image = ImageIO.read(new File(properties.getProperty("memes.image." + meme)));
                AffineTransform at = new AffineTransform();
                at.scale(1. * (IMAGE_WIDTH - 2 * DX) / image.getWidth(), 1. * (heightR - 2 * DX) / image.getHeight());
                AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
                BufferedImage after = new BufferedImage(IMAGE_WIDTH - 2 * DX, heightR - 2 * DX, BufferedImage.TYPE_INT_ARGB);
                scaleOp.filter(image, after);
                memesImages.put(meme, image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateImpl(Data data) {
        if (data.memesData.isVisible) {
            if (!visible) {
                previous = data.memesData.currentMeme;
                meme = data.memesData.currentMeme;
                count = data.memesData.count;
                status = MemeStatus.OPEN;
            } else {
                if (!data.memesData.currentMeme.equals(meme)) {
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
        update();
        updateVisibility();
        if (visibilityRectangle == 0) {
            return;
        }
        int X = Widget.BASE_WIDTH - (int) ((widthR + DX) * visibilityRectangle);
        g.drawRect(X, Y, widthR, heightR, MemesStylesheet.meme.background, 1);

        if (g instanceof GraphicsSWT) {
            ((GraphicsSWT) g).drawImage(memesImages.get(meme), 10, 10, visibilityText);
        } else {
            int w = IMAGE_WIDTH - 20;
            int h = heightR - 20;
            g.drawImage(memesImages.get(meme),
                    10, (int)((h + 10) - h * textOpacity), IMAGE_WIDTH - 20, (int) (h * textOpacity));
        }

        g.drawString(" X " + count, X + IMAGE_WIDTH, Y + heightR / 2,
                font, MemesStylesheet.meme.text, visibilityText);
    }

    @Override
    public CachedData getCorrespondingData(Data data) {
        return data.memesData;
    }
}
