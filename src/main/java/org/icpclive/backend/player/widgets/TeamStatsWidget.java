package org.icpclive.backend.player.widgets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.egork.teaminfo.data.Person;
import net.egork.teaminfo.data.Record;
import net.egork.teaminfo.data.University;
import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.backend.player.widgets.stylesheets.QueueStylesheet;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author egor@egork.net
 */
public class TeamStatsWidget extends Widget {

    private static final int AWARD_HEIGHT = 55;
    private static final int WIDTH = 1305;
    private static final int HEIGHT = 177;
    private static final int BASE_X = 1893 - WIDTH;
    private static final int BASE_Y = 1007 - HEIGHT;
    private static final int LOGO_SIZE = 143;
    private static final int LOGO_X = 17;
    private static final int AWARD_SIZE = 40;
    private static final int STATS_WIDTH = WIDTH - LOGO_SIZE - LOGO_X - LOGO_X;

    private static final double MOVE_SPEED = 2.0;

    private Record record;
    private BufferedImage logo;
    private BufferedImage cupImage;
    private BufferedImage regionalCupImage;
    private BufferedImage goldMedalImage;
    private BufferedImage silverMedalImage;
    private BufferedImage bronzeMedalImage;

    private ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private StatsPanel[] panels;

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
            panels = new StatsPanel[]{
                    new UnivsersityNamePanel(10000, STATS_WIDTH, record.university),
                    new PersonStatsPanel(5000, record.contestants[0], false),
                    new PersonStatsPanel(5000, record.contestants[1], false),
                    new PersonStatsPanel(5000, record.contestants[2], false),
                    new PersonStatsPanel(5000, record.coach, true)
            };
            fullPeriod = 0;
            fullWidth = 0;
            for (StatsPanel panel : panels) {
                fullPeriod += panel.pauseTime;
                fullPeriod += panel.width / MOVE_SPEED;
                fullWidth += panel.width;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int time;
    private int fullPeriod;
    private int fullWidth;

    @Override
    protected void paintImpl(AbstractGraphics g, int ww, int hh) {
        super.paintImpl(g, ww, hh);
        if (visibilityState == 0) time = 0;
        time += dt;
        time %= fullPeriod;
        g = g.create();
        g.translate(BASE_X, BASE_Y);
        g.clip(0, 0, WIDTH, HEIGHT);
        setGraphics(g);
        PlateStyle color = QueueStylesheet.name;
        applyStyle(color);
        drawRectangle(0, 0, WIDTH, HEIGHT);
        g.drawImage(logo, LOGO_X, LOGO_X, LOGO_SIZE, LOGO_SIZE, opacity);

        int dx = 0;
        int tt = time;
        for (StatsPanel panel : panels) {
            if (tt < panel.pauseTime) break;
            tt -= panel.pauseTime;
            if (tt < panel.width / MOVE_SPEED) {
                dx += tt * MOVE_SPEED;
                break;
            }
            tt -= panel.width / MOVE_SPEED;
            dx += panel.width;
        }

        g.translate(WIDTH - STATS_WIDTH, 0);
        g.clip(0, 0, STATS_WIDTH, HEIGHT);

        int x = 0;
        for (StatsPanel panel : panels) {
            int l = x - dx;
            int r = l + panel.width;
            if (r < 0) {
                l += fullWidth;
                r += fullWidth;
            }
            if (l < STATS_WIDTH && r >= 0) {
                AbstractGraphics g1 = g.create();
                g1.translate(l, 0);
                panel.setVisibilityState(visibilityState);
                panel.paintImpl(g1, 1920, 1080);
            }
            x += panel.width;
        }

    }

    static class StatsPanel extends Widget {
        private final int pauseTime;
        private final int width;

        public StatsPanel(int pauseTime, int width) {
            this.pauseTime = pauseTime;
            this.width = width;
        }
    }

    static class UnivsersityNamePanel extends StatsPanel {

        private static final Font NAME_FONT = Font.decode(MAIN_FONT + " " + 60);
        private static final Font NAME_FONT_SMALLER = Font.decode(MAIN_FONT + " " + 48);
        private final University university;

        public UnivsersityNamePanel(int pauseTime, int width, University university) {
            super(pauseTime, width);
            this.university = university;
        }

        @Override
        protected void paintImpl(AbstractGraphics g, int width, int height) {
            super.paintImpl(g, width, height);
            String[] parts = split(university.getFullName(), 40);
            if (parts.length == 1) {
                int y = 32;
                setFont(NAME_FONT);
                setTextColor(Color.WHITE);
                drawTextThatFits(parts[0], 0, y, STATS_WIDTH, 60, PlateStyle.Alignment.LEFT, true);
            } else {
                parts = split(university.getFullName(), 50);
                int y = parts.length == 1 ? 32 : 12;
                setFont(NAME_FONT_SMALLER);
                setTextColor(Color.WHITE);
                for (int i = 0; i < parts.length; i++) {
                    drawTextThatFits(parts[i], 0, y, STATS_WIDTH, 60, PlateStyle.Alignment.LEFT, true);
                    y += 52;
                }
            }
        }
    }

    static class PersonStatsPanel extends StatsPanel {

        private static final Font NAME_FONT = Font.decode(MAIN_FONT + " " + 38);
        private static final Font TEXT_FONT = Font.decode("Open Sans 18");
        private static final int ACHIEVEMENT_WIDTH = 360;
        private final Person person;
        private final boolean coach;

        public PersonStatsPanel(int pauseTime, Person person, boolean coach) {
            super(pauseTime, Math.max(Math.max(
                    750,
                    getStringWidth(NAME_FONT, person.getName() + (coach ? ", coach" : "")) + 50),
                    (person.getAchievements().size() + 2) / 3 * ACHIEVEMENT_WIDTH
                    ));
            this.person = person;
            this.coach = coach;
        }

        private static int getStringWidth(Font font, String string) {
            return (int) font.getStringBounds(string, new FontRenderContext(new AffineTransform(), true, true)).getWidth();
        }

        @Override
        protected void paintImpl(AbstractGraphics g, int width, int height) {
            super.paintImpl(g, width, height);
            setFont(NAME_FONT);
            setTextColor(Color.WHITE);
            drawText(person.getName() + (coach ? ", coach" : ""), 0, 48);

            int xx = 0;
            int yy = 80;
            setFont(TEXT_FONT);
            int rating = person.getTcRating();
            if (rating != -1) {
                setTextColor(Color.WHITE);
                drawText("TC: ", xx, yy);
                xx += getStringWidth(TEXT_FONT, "TC: ");
                setTextColor(getTcColor(rating));
                drawText(Integer.toString(rating), xx, yy);
                xx += getStringWidth(TEXT_FONT, Integer.toString(rating));
                xx += 20;
            }
            rating = person.getCfRating();
            if (rating != -1) {
                setTextColor(Color.WHITE);
                drawText("CF: ", xx, yy);
                xx += getStringWidth(TEXT_FONT, "CF: ");
                setTextColor(getCfColor(rating));
                drawText(Integer.toString(rating), xx, yy);
                xx += getStringWidth(TEXT_FONT, Integer.toString(rating));
                xx += 20;
            }
            xx = 0;
            yy += 27;
            setTextColor(Color.WHITE);
            for (int i = 0; i < 6 && i < person.getAchievements().size(); i++) {
                int cx = (i / 3) * ACHIEVEMENT_WIDTH;
                int cy = 27 * (i % 3);
                drawText(person.getAchievements().get(i).achievement, xx + cx, yy + cy);
            }
        }
    }

    private static Color getTcColor(int tcRating) {
        if (tcRating >= 2200) {
            return new Color(0xED1F24);
        }
        if (tcRating >= 1500) {
            return new Color(0xEDD221);
        }
        if (tcRating >= 1200) {
            return new Color(0x7777ff);
        }
        if (tcRating >= 900) {
            return new Color(0x148A43);
        }
        return new Color(0x808080);
    }

    private static Color getCfColor(int tcRating) {
        if (tcRating >= 2400) {
            return new Color(0xED1F24);
        }
        if (tcRating >= 2200) {
            return new Color(0xF79A3B);
        }
        if (tcRating >= 1900) {
            return new Color(0xcc59ff);
        }
        if (tcRating >= 1600) {
            return new Color(0x7777ff);
        }
        if (tcRating >= 1400) {
            return new Color(0x63C29E);
        }
        if (tcRating >= 1200) {
            return new Color(0x148A43);
        }
        return new Color(0x808080);
    }

    private static String[] split(String s, int max) {
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

    private static BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, Object hint, boolean higherQuality) {
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
