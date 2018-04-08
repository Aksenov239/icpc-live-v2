package org.icpclive.backend.player.widgets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.egork.teaminfo.data.Person;
import net.egork.teaminfo.data.Record;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.backend.player.widgets.stylesheets.QueueStylesheet;
import org.icpclive.backend.player.widgets.stylesheets.TeamStatsStylesheet;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * @author egor@egork.net
 */
public class TeamStatsWidget extends Widget {

    private static final int AWARD_HEIGHT = 55;

    private static Logger log = LogManager.getLogger(TeamStatsWidget.class);

    //    private static final int X = 519;
//    private static final int Y = 794;
//    private static final int MARGIN = 2;
//    static final int WIDTH = 1371;
//    private static final int LEFT_WIDTH = WIDTH / 2;
//    static final int HEIGHT = 200;
//    private static final int INITIAL_SHIFT = WIDTH + MARGIN;
//    private static final int PERSON_WIDTH = 1066;
//    private static final int PERSON_SHIFT = PERSON_WIDTH + MARGIN;
//    private static final int BOTTOM_WIDTH = WIDTH + 3 * PERSON_WIDTH + 4 * MARGIN + WIDTH;
//    private static final int LOGO_X = 20;
//    private static final int LOGO_Y = 70;
//    private static final int[] SHIFTS = new int[]{0, INITIAL_SHIFT, INITIAL_SHIFT + PERSON_SHIFT,
//            INITIAL_SHIFT + PERSON_SHIFT * 2, INITIAL_SHIFT + PERSON_SHIFT * 3};
//    private static final int SHOW_TIME = 5000;
//    private static final int SHIFT_SPEED = 1800; //pixels in second
//    private static final int FADE_TIME = 1000;
//    private static final int UNIVERSITY_NAME_X = 20;
//    private static final int UNIVERSITY_NAME_Y = 50;
//    private static final Color UNIVERSITY_NAME_COLOR = new Color(0xaaaacc);
//    private static final int TEAM_INFO_X = 145;
//    private static final int TEAM_INFO_Y = 94;
//    private static final Font TEAM_INFO = Font.decode(MAIN_FONT + " " + 24);
//    private static final Color TOP_FOREGROUND = Color.WHITE;
//    private static final Color NAME_COLOR = Color.WHITE;
//    private static final Color TOP_BACKGROUND = TeamStatsStylesheet.background;
//    private static final Color BOTTOM_BACKGROUND = TeamStatsStylesheet.background;
//
//    private static final int WF_CAPTION_X = 20;
//    private static final int WF_CAPTION_Y = 50;
//    private static final Color WF_CAPTION_COLOR = new Color(0xaaaacc);
//    private static final Font WF_CAPTION_FONT = Font.decode(MAIN_FONT + " " + 30).deriveFont(Font.BOLD);
//
//    private static final int AWARDS_CAPTION_X = 150;
//    private static final int AWARDS_CAPTION_Y = 50;
//    private static final Color AWARDS_CAPTION_COLOR = new Color(0xaaaacc);
//    private static final Font AWARDS_CAPTION_FONT = Font.decode(MAIN_FONT + " " + 30).deriveFont(Font.BOLD);
//
//    private static final Color REGION_CAPTION_COLOR = new Color(0xaaaacc);
//    private static final Font REGION_CAPTION_FONT = Font.decode(MAIN_FONT + " " + 24);
//
//    private static final int AWARDS_X = 150;
//    private static final int AWARDS_Y = 70;
//
//    private static final int WF_X = 20;
//    private static final int WF_Y = 114;
//    private static final Color WF_COLOR = Color.WHITE;
//    private static final Font WF_FONT = Font.decode(MAIN_FONT + " " + 50).deriveFont(Font.BOLD);
//
//    private static final int PERSON_CIRCLE_X = 30;
//    private static final int PERSON_CIRCLE_Y = 28;
//    private static final int PERSON_CIRCLE_DIAMETER = 24;
//    private static final int PERSON_NAME_X = 63;
//    private static final int PERSON_NAME_Y = 50;
//    private static final Color PERSON_NAME_COLOR = Color.WHITE;
//    private static final Font PERSON_NAME_FONT = Font.decode(MAIN_FONT + " " + 30).deriveFont(Font.BOLD);
//    private static final int PERSON_RATING_Y = 86;
//    private static final Font RATING_FONT = Font.decode(MAIN_FONT + " " + 24).deriveFont(Font.BOLD);
//    private static final int RATING_SPACE = 18;
//    private static final int TOP_ACHIEVEMENT_Y = 114;
//    private static final int ACHIEVEMENT_DY = 30;
//    private static final int ACHIEVEMENT_WIDTH = 314;
//    private static final Color ACHIEVEMENT_COLOR = new Color(0xEFDFED);
//    private static final Font ACHIEVEMENT_CAPTION_FONT = Font.decode(MAIN_FONT + " " + 18);

    private static final Font NAME_FONT = Font.decode(MAIN_FONT + " " + 60);
    private static final Font NAME_FONT_SMALLER = Font.decode(MAIN_FONT + " " + 48);

    private static final int LOGO_SIZE = 143;
    private static final int LOGO_X = 17;
    private static final int AWARD_SIZE = 40;
    private Record record;
    private BufferedImage logo;
    private BufferedImage cupImage;
    private BufferedImage regionalCupImage;
    private BufferedImage goldMedalImage;
    private BufferedImage silverMedalImage;
    private BufferedImage bronzeMedalImage;

    private ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public TeamStatsWidget(int id) {
        try {
            cupImage = getScaledInstance(ImageIO.read(new File("pics/cup.png")), AWARD_SIZE, AWARD_SIZE, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false);
            regionalCupImage = getScaledInstance(ImageIO.read(new File("pics/regional.png")), AWARD_SIZE, AWARD_SIZE, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false);
            goldMedalImage = getScaledInstance(ImageIO.read(new File("pics/gold_medal.png")), AWARD_SIZE, AWARD_SIZE, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false);
            silverMedalImage = getScaledInstance(ImageIO.read(new File("pics/silver_medal.png")), AWARD_SIZE, AWARD_SIZE, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false);
            bronzeMedalImage = getScaledInstance(ImageIO.read(new File("pics/bronze_medal.png")), AWARD_SIZE, AWARD_SIZE, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false);
            record = mapper.readValue(new File("teamData/" + id + ".json"), Record.class);
            System.out.println("teamData/" + id + ".json");
            logo = getScaledInstance(ImageIO.read(new File("teamData/" + id + ".png")), LOGO_SIZE, LOGO_SIZE, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintImpl(AbstractGraphics g, int ww, int hh) {
        int height = 177;
        int width = 1305;
        int baseX = 1893 - width;
        int baseY = 1007 - height;
        g = g.create();
        g.translate(baseX, baseY);
        g.clip(0, 0, width, height);
        setGraphics(g);

        PlateStyle color = QueueStylesheet.name;
        applyStyle(color);

        drawRectangle(0, 0, width, height);
        g.drawImage(logo, LOGO_X, LOGO_X, LOGO_SIZE, LOGO_SIZE, opacity);
        String[] parts = split(record.university.getFullName(), 40);
        if (parts.length == 1) {
            int y = 32;
            setFont(NAME_FONT);
            drawTextThatFits(parts[0], LOGO_SIZE + LOGO_X, y, width - LOGO_SIZE - LOGO_X, 60, PlateStyle.Alignment.LEFT, true);
        } else {
            parts = split(record.university.getFullName(), 50);
            int y = parts.length == 1 ? 32 : 12;
            setFont(NAME_FONT_SMALLER);
            for (int i = 0; i < parts.length; i++) {
                drawTextThatFits(parts[i], LOGO_SIZE + LOGO_X, y, width - LOGO_SIZE - LOGO_X, 60, PlateStyle.Alignment.LEFT, true);
                y += 52;
            }
        }
    }

    @Override
    protected CachedData getCorrespondingData(Data data) {
        return null;
    }

    private String[] split(String s, int max) {
        if (s.length() <= max) return new String[]{s};
        int i = max;
        s = s + " ";
        while (s.charAt(i) != ' ' || s.charAt(i + 1) == '-') {
            i--;
        }
        String[] ss = split(s.substring(i + 1), max);
        String[] res = new String[ss.length + 1];
        System.arraycopy(ss, 0, res, 1, ss.length);
        res[0] = s.substring(0, i);
        return res;
    }

    private BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, Object hint, boolean higherQuality) {
        int type = (img.getTransparency() == Transparency.OPAQUE)
                ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

        double scale = Math.min(1.0 * targetHeight / img.getHeight(), 1.0 * targetWidth / img.getWidth());
        targetHeight = (int) (img.getHeight() * scale);
        targetWidth = (int) (img.getWidth() * scale);
        BufferedImage ret = img;

        if (targetHeight > 0 && targetWidth > 0) {
            int w, h;
            if (higherQuality) {
                w = img.getWidth();
                h = img.getHeight();
            } else {
                w = targetWidth;
                h = targetHeight;
            }

            do {
                if (higherQuality && w > targetWidth) {
                    w /= 2;
                    if (w < targetWidth) {
                        w = targetWidth;
                    }
                }

                if (higherQuality && h > targetHeight) {
                    h /= 2;
                    if (h < targetHeight) {
                        h = targetHeight;
                    }
                }

                BufferedImage tmp = new BufferedImage(Math.max(w, 1), Math.max(h, 1), type);
                Graphics2D g2 = tmp.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
                g2.drawImage(ret, 0, 0, w, h, null);
                g2.dispose();

                ret = tmp;
            } while (w != targetWidth || h != targetHeight);
        } else {
            ret = new BufferedImage(1, 1, type);
        }
        return ret;
    }

}
