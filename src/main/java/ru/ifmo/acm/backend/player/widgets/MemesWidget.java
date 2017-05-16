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
    private String next;
    private int nextCount;
    private String meme;
    private int count;
    private boolean visible;
    private long lastChange;

    private double visibilityRectangle;
    private double visibilityText;

    private int widthR;
    private int heightR;
    private int Y;
    private int DX;
    private int IMAGE_WIDTH;

    private Font font;

    private int flickerTime;
    private MemeStatus status = MemeStatus.CLOSE;

    private double V = 0;

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
                memesImages.put(meme, after);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateImpl(Data data) {
        if (data.memesData.isVisible) {
            if (data.memesData.currentMeme == null) {
                return;
            }
            if (!visible) {
                next = data.memesData.currentMeme;
                meme = data.memesData.currentMeme;
                count = data.memesData.count;
                status = MemeStatus.OPEN;
            } else {
                if (!data.memesData.currentMeme.equals(next)) {
                    next = data.memesData.currentMeme;
                    nextCount = data.memesData.count;
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
            visibilityText = 1;
        } else if (status == MemeStatus.CLOSE) {
            visibilityRectangle = Math.max(visibilityRectangle - V * dt, 0);
        } else {
            if (time - lastChange < flickerTime / 2) {
                visibilityText = Math.max(visibilityText - V * dt, 0);
            } else {
                if (!meme.equals(next)) {
                    meme = next;
                    count = nextCount;
                }
                visibilityText = Math.min(visibilityText + V * dt, 1);
            }
        }
    }

    @Override
    public void paintImpl(Graphics g, int width, int height) {
        update();
        updateVisibility();
        if (visibilityRectangle == 0) {
            return;
        }
        int X = Widget.BASE_WIDTH - (int) ((widthR + DX) * visibilityRectangle);
        g.drawRect(X, Y, widthR, heightR, MemesStylesheet.meme.background, 1, Graphics.RectangleType.SOLID);

        if (g instanceof GraphicsSWT) {
            ((GraphicsSWT) g).drawImage(memesImages.get(meme), X + 10, Y + 10, visibilityText);
        } else {
            int w = IMAGE_WIDTH - 20;
            int h = heightR - 20;
            g.drawImage(memesImages.get(meme),
                    X + 10, Y + 10 + (int) (h / 2 - h / 2 * visibilityText),
                    w, (int) (h * visibilityText));
        }

        g.drawString(" X " + count, X + IMAGE_WIDTH, Y + heightR / 2 + (int) (heightR * 0.07),
                font, MemesStylesheet.meme.text, visibilityText);
    }

    @Override
    public CachedData getCorrespondingData(Data data) {
        return data.memesData;
    }
}
