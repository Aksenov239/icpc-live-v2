package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.backend.graphics.GraphicsSWT;
import ru.ifmo.acm.backend.player.widgets.stylesheets.WordStatisticsStylesheet;
import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;
import ru.ifmo.acm.mainscreen.Words.WordStatistics;
import ru.ifmo.acm.mainscreen.Words.WordStatisticsData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by Meepo on 4/22/2017.
 */
public class WordStatisticsWidget extends Widget {
    private String word;
    private long count;
    private BufferedImage picture;

    private int widthR;
    private int heightR;
    private int Y;
    private int DX;
    private int IMAGE_WIDTH;

    private Font font;

    HashMap<String, BufferedImage> memesImages;

    public WordStatisticsWidget(long updateWait, int Y, int widthR, int heightR) {
        super(updateWait);

        visibilityState = 0;
        this.widthR = widthR;
        IMAGE_WIDTH = (int) (this.widthR * 0.5);
        this.DX = (int) (0.05 * widthR);
        this.heightR = heightR;
        this.Y = Y;
        font = new Font("Open Sans", Font.BOLD, 40);

        Properties properties = new Properties();
        try {
            properties.load(WordStatisticsWidget.class.getClassLoader().getResourceAsStream("mainscreen.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateImpl(Data data) {
        if (data.wordStatisticsData.isVisible) {
            if (data.wordStatisticsData.word == null) {
                return;
            }

            WordStatistics wordStatistics = data.wordStatisticsData.word;
            word = wordStatistics.getWord();

            if (WordStatisticsData.TWEET_KEYWORD.equals(word)) {
                word = "Tweets";
            }

            count = wordStatistics.getCount();

            picture = null;
            try {
                if (wordStatistics.getPicture().length() != 0) {
                    picture = ImageIO.read(new File(wordStatistics.getPicture()));
                }
            } catch (IOException e) {
                picture = null;
            }

            setVisible(true);
        } else {
            setVisible(false);
        }
    }

    @Override
    public void paintImpl(Graphics g, int width, int height) {
        update();
        updateVisibilityState();
        if (visibilityState == 0) {
            return;
        }
        int X = Widget.BASE_WIDTH - (int) ((widthR + DX) * visibilityState);
        g.drawRect(X, Y, widthR, heightR, WordStatisticsStylesheet.word.background, 1, Graphics.RectangleType.SOLID);

        if (picture == null) {
            int w = IMAGE_WIDTH - 20;
            int h = heightR - 20;

            g.drawRectWithText(word, X + 10, Y + 10, w, h,
                    Graphics.Alignment.CENTER, font, WordStatisticsStylesheet.word,
                    visibilityState, 1, 0, false);
        } else {
            int x = X + IMAGE_WIDTH / 2 - picture.getWidth() / 2;
            int y = Y + heightR / 2 - picture.getHeight() / 2;
            if (g instanceof GraphicsSWT) {
                ((GraphicsSWT) g).drawImage(picture, x, y, visibilityState);
            } else {

                g.drawImage(picture, x, y, picture.getWidth(), picture.getHeight());
            }
        }

        g.drawString(" X " + (count < 1000 ? count : count / 1000 + "k"),
                X + IMAGE_WIDTH, Y + heightR / 2 + (int) (heightR * 0.065),
                font, WordStatisticsStylesheet.word.text, 1);
    }

    @Override
    public CachedData getCorrespondingData(Data data) {
        return data.wordStatisticsData;
    }
}
