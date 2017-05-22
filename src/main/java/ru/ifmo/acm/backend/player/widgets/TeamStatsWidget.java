package ru.ifmo.acm.backend.player.widgets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.egork.teaminfo.data.Person;
import net.egork.teaminfo.data.Record;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.acm.backend.graphics.Graphics;
import ru.ifmo.acm.backend.player.widgets.stylesheets.TeamStatsStylesheet;
import ru.ifmo.acm.datapassing.CachedData;
import ru.ifmo.acm.datapassing.Data;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;

/**
 * @author egor@egork.net
 */
public class TeamStatsWidget extends RotatableWidget {
    private static Logger log = LogManager.getLogger(TeamStatsWidget.class);

    private static final int X = 519;
    private static final int Y = 794;
    private static final int MARGIN = 2;
    private static final int WIDTH = 1371;
    private static final int LEFT_WIDTH = WIDTH / 2;
    private static final int HEIGHT = 200;
    private static final int INITIAL_SHIFT = WIDTH + MARGIN;
    private static final int PERSON_WIDTH = 1066;
    private static final int PERSON_SHIFT = PERSON_WIDTH + MARGIN;
    private static final int BOTTOM_WIDTH = WIDTH + 3 * PERSON_WIDTH + 4 * MARGIN + WIDTH;
    private static final int LOGO_SIZE = 130;
    private static final int LOGO_SHIFT = 22;
    private static final int[] SHIFTS = new int[]{0, INITIAL_SHIFT, INITIAL_SHIFT + PERSON_SHIFT,
            INITIAL_SHIFT + PERSON_SHIFT * 2, INITIAL_SHIFT + PERSON_SHIFT * 3};
    private static final int SHOW_TIME = 5000;
    private static final int SHIFT_SPEED = 1800; //pixels in second
    private static final int FADE_TIME = 1000;
    private static final int UNIVERSITY_NAME_X = 168;
    private static final int UNIVERSITY_NAME_Y = 50;
    private static final Font UNIVERSITY_NAME = Font.decode("Open Sans 30").deriveFont(Font.BOLD);
    private static final int TEAM_INFO_X = UNIVERSITY_NAME_X;
    private static final int TEAM_INFO_Y = 94;
    private static final Font TEAM_INFO = Font.decode("Open Sans 24");
    private static final Color TOP_FOREGROUND = Color.WHITE;
    private static final Color NAME_COLOR = Color.WHITE;
    private static final Color TOP_BACKGROUND = TeamStatsStylesheet.background;
    private static final Color BOTTOM_BACKGROUND = TeamStatsStylesheet.background;

    private static final int ACHIVEMENT_X = 20;
    private static final int ACHIVEMENT_Y = 50;
    private static final Color ACHIVEMENT_COLOR = new Color(0xaaaacc);
    private static final Font ACHIVEMENT_FONT = Font.decode("Open Sans 30").deriveFont(Font.BOLD);

    private static final int AWARDS_X = 200;
    private static final int AWARDS_Y = 50;
    private static final Color AWARDS_COLOR = new Color(0xaaaacc);
    private static final Font AWARDS_FONT = Font.decode("Open Sans 30").deriveFont(Font.BOLD);

    private static final int APPEARANCES_X = 20;
    private static final int APPEARANCES_Y = 114;
    private static final Color APPEARANCES_COLOR = Color.WHITE;
    private static final Font APPEARANCES_FONT = Font.decode("Open Sans 50").deriveFont(Font.BOLD);

    private static final int PERSON_CIRCLE_X = 30;
    private static final int PERSON_CIRCLE_Y = 28;
    private static final int PERSON_CIRCLE_DIAMETER = 24;
    private static final int PERSON_NAME_X = 63;
    private static final int PERSON_NAME_Y = 50;
    private static final Font PERSON_NAME_FONT = Font.decode("Open Sans 30").deriveFont(Font.BOLD);
    private static final int PERSON_RATING_Y = 86;
    private static final Font RATING_FONT = Font.decode("Open Sans 24").deriveFont(Font.BOLD);
    private static final int RATING_SPACE = 18;
    private static final int TOP_ACHIEVEMENT_Y = 114;
    private static final int ACHIEVEMENT_DY = 30;
    private static final int ACHIEVEMENT_WIDTH = 314;
    private static final Color ACHIEVEMENT_COLOR = new Color(0xEFDFED);
    private static final Font ACHIEVEMENT_CAPTION_FONT = Font.decode("Open Sans 18");

    private ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public TeamStatsWidget(long updateWait, long sleepTime) {
        super(updateWait, X, Y, WIDTH, MARGIN, SHIFTS, SHOW_TIME, SHIFT_SPEED, FADE_TIME);
        this.sleepTime = sleepTime;
    }

    private long sleepTime;
    private long lastUpdateTimestamp;
    private long lastUpdateLocalTimestamp = Long.MAX_VALUE / 2;
    private boolean previousVisible;

    public void updateImpl(Data data) {
        sleepTime = data.teamData.sleepTime;
        if (data.teamStatsData.timestamp > lastUpdateTimestamp) {
            lastUpdateTimestamp = data.teamStatsData.timestamp;
            setVisible(data.teamStatsData.isVisible);
            lastUpdateLocalTimestamp = System.currentTimeMillis();
            if (!isVisible()) {
                hide();
                previousVisible = false;
            }
        }
        if (isVisible() && lastUpdateLocalTimestamp + sleepTime < System.currentTimeMillis()) {
            showTeam(data.teamStatsData.getTeamId() + 1);
            if (previousVisible) {
                setFaded();
            }
            lastUpdateLocalTimestamp = Long.MAX_VALUE / 2;
            previousVisible = true;
        }
    }

    private void showTeam(int id) {
        try {
            Record record = mapper.readValue(new File("teamData/" + id + ".json"), Record.class);
            System.out.println("teamData/" + id + ".json");
            BufferedImage logo = getScaledInstance(ImageIO.read(new File("teamData/" + id + ".png")), LOGO_SIZE, LOGO_SIZE, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
            VolatileImage unmovable = prepareLeftPlaque(record, logo);
            VolatileImage movable = prepareRightPlaque(record);
            setUnmovable(unmovable);
            setMovable(movable);
            start();
        } catch (IOException e) {
            log.error("Can't load team info for team " + id, e);
        }
    }

    private VolatileImage prepareRightPlaque(Record record) {
        VolatileImage image = createVolatileImage(BOTTOM_WIDTH, HEIGHT);
        Graphics2D g = (Graphics2D) image.getGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        g.setColor(ACHIEVEMENT_COLOR);
        g.setFont(ACHIVEMENT_FONT);
        g.drawString("World Finals", ACHIVEMENT_X, ACHIVEMENT_Y);

        g.setColor(APPEARANCES_COLOR);
        g.setFont(APPEARANCES_FONT);
        g.drawString("" + record.university.getAppearances(), APPEARANCES_X, APPEARANCES_Y);

        if (record.university.getGold() + record.university.getSilver() + record.university.getBronze() + record.university.getRegionalChampionships() > 0) {
            g.setColor(AWARDS_COLOR);
            g.setFont(AWARDS_FONT);
            g.drawString("Awards", AWARDS_X, AWARDS_Y);
        }
        int x = INITIAL_SHIFT;
        Person[] persons = {record.coach, record.contestants[0], record.contestants[1], record.contestants[2]};
        for (int i = 0; i < 4; i++) {
            g.setColor(BOTTOM_BACKGROUND);
            g.fillRect(x, 0, i == 3 ? WIDTH : PERSON_WIDTH, HEIGHT);
            drawPersonProfile(g, x, persons[i], i == 0);
            x += PERSON_WIDTH + MARGIN;
        }
        return image;
    }

    private void drawPersonProfile(Graphics2D g, int x, Person person, boolean isCoach) {
        g.setColor(NAME_COLOR);
        g.fillOval(x + PERSON_CIRCLE_X, PERSON_CIRCLE_Y, PERSON_CIRCLE_DIAMETER, PERSON_CIRCLE_DIAMETER);
        g.setFont(PERSON_NAME_FONT);
        g.drawString(person.getName() + (isCoach ? ", Coach" : ", Contestant"), x + PERSON_NAME_X, PERSON_NAME_Y);
        g.setFont(RATING_FONT);
        int xx = x + PERSON_NAME_X;
        if (person.getTcRating() != -1) {
            g.setColor(NAME_COLOR);
            FontMetrics fontMetrics = g.getFontMetrics();
            g.drawString("TC: ", xx, PERSON_RATING_Y);
            xx += fontMetrics.stringWidth("TC: ");
            g.setColor(getTcColor(person.getTcRating()));
            g.drawString(Integer.toString(person.getTcRating()), xx, PERSON_RATING_Y);
            xx += fontMetrics.stringWidth(Integer.toString(person.getTcRating()));
            xx += RATING_SPACE;
        }
        if (person.getCfRating() != -1) {
            g.setColor(NAME_COLOR);
            FontMetrics fontMetrics = g.getFontMetrics();
            g.drawString("CF: ", xx, PERSON_RATING_Y);
            xx += fontMetrics.stringWidth("CF: ");
            g.setColor(getCfColor(person.getCfRating()));
            g.drawString(Integer.toString(person.getCfRating()), xx, PERSON_RATING_Y);
        }
        g.setColor(ACHIEVEMENT_COLOR);
        g.setFont(ACHIEVEMENT_CAPTION_FONT);
        for (int i = 0; i < 6 && i < person.getAchievements().size(); i++) {
            int cx = x + PERSON_NAME_X + (i / 3) * ACHIEVEMENT_WIDTH;
            int cy = TOP_ACHIEVEMENT_Y + ACHIEVEMENT_DY * (i % 3);
            g.drawString(prepareAchievement(g, person.getAchievements().get(i).achievement, ACHIEVEMENT_WIDTH - RATING_SPACE)
                    , cx, cy);
        }
    }

    private String prepareAchievement(Graphics2D g, String achievement, int maxWidth) {
        if (g.getFontMetrics().stringWidth(achievement) <= maxWidth) {
            return achievement;
        }
        int yearAt = achievement.indexOf("(");
        String years = achievement.substring(yearAt);
        years = years.substring(1, years.length() - 1);
        String[] tokens = years.split(", ");
        int times = 0;
        for (String token : tokens) {
            if (token.length() == 4) {
                times++;
            } else {
                times += Integer.parseInt(token.substring(5)) - Integer.parseInt(token.substring(0, 4)) + 1;
            }
        }
        return achievement.substring(0, yearAt) + "(" + times + ")";
    }

    private Color getTcColor(int tcRating) {
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

    private Color getCfColor(int tcRating) {
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

    private VolatileImage prepareLeftPlaque(Record record, BufferedImage logo) {
        VolatileImage image = createVolatileImage(LEFT_WIDTH, HEIGHT);
        Graphics2D g = (Graphics2D) image.getGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        g.setColor(new Color(TOP_BACKGROUND.getRGB() & 0x77ffffff));
        g.fillRect(0, 0, INITIAL_SHIFT, HEIGHT);
        g.drawImage(logo, LOGO_SHIFT, LOGO_SHIFT, null);
        g.setColor(TOP_FOREGROUND);
        g.setFont(UNIVERSITY_NAME);
        g.drawString(record.university.getFullName(), UNIVERSITY_NAME_X, UNIVERSITY_NAME_Y);
        g.setFont(TEAM_INFO);
        g.drawString(
                record.team.getName(), //record.team.getRegionals().iterator().next() + " | " +
                TEAM_INFO_X, TEAM_INFO_Y
        );
//        g.drawString(
//                record.team.getRegionals().iterator().next(),
//                TEAM_INFO_X, TEAM_INFO_Y + 30
//        );
        g.drawString(
                        "#" + record.university.getHashTag(),
                TEAM_INFO_X, TEAM_INFO_Y + 40
        );
        return image;
    }

    private VolatileImage createVolatileImage(int width, int height) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        VolatileImage image = gc.createCompatibleVolatileImage(width, height, Transparency.TRANSLUCENT);
        Graphics2D g = image.createGraphics();
        g.setColor(TeamStatsStylesheet.background);
        g.fillRect(0, 0, width, height);
        return image;
    }

    public void paintImpl(Graphics g, int width, int height) {
        update();
        super.paintImpl(g, width, height);
    }

    public CachedData getCorrespondingData(Data data) {
        return data.teamData;
    }


    private BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, Object hint, boolean higherQuality) {
        int type = (img.getTransparency() == Transparency.OPAQUE)
                ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

        BufferedImage ret = img;

        if (targetHeight > 0 || targetWidth > 0) {
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
