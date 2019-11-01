package org.icpclive.backend.player.widgets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import net.egork.teaminfo.data.*;
import org.icpclive.backend.graphics.AbstractGraphics;
import org.icpclive.backend.player.widgets.stylesheets.PlateStyle;
import org.icpclive.backend.player.widgets.stylesheets.QueueStylesheet;
import org.icpclive.events.PCMS.PCMSTeamInfo;
import org.icpclive.events.TeamInfo;
import org.icpclive.events.WF.json.WFTeamInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * @author egor@egork.net
 */
public class TeamStatsWidget extends Widget {

    private static final int CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR);
    private static final int WIDTH = 1305;
    private static final int HEIGHT = 177;
    private static final int BASE_X = 1893 - WIDTH;
    private static final int BASE_Y = 1007 - HEIGHT;
    private static final int LOGO_SIZE = 143;
    private static final int LOGO_X = 17;
    private static final int STATS_WIDTH = WIDTH - LOGO_SIZE - LOGO_X - LOGO_X;

    private static final double MOVE_SPEED = 1.5;

    private Record record;
    private BufferedImage logo;

    private ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private StatsPanel[] panels;

    public TeamStatsWidget(TeamInfo teamInfo) {
        try {
            int id = Integer.parseInt(((WFTeamInfo) teamInfo).cdsId);
            record = mapper.readValue(new File("teamData/" + id + ".json"), Record.class);
            record.university.setHashTag(teamInfo.getHashTag());
            record.university.setFullName(teamInfo.getName());
            try {
                logo = getScaledInstance(ImageIO.read(new File("teamData/" + id + ".png")), LOGO_SIZE, LOGO_SIZE, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
            } catch (Exception e) {
                logo = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            }

            JsonArray additionalAwards = new Gson().fromJson(new FileReader("awards.json"), JsonArray.class);
            University university = record.university;
            for (JsonElement award : additionalAwards) {
                boolean myTeam = false;
                JsonArray teams = award.getAsJsonObject().get("team_ids").getAsJsonArray();
                for (JsonElement t : teams) {
                    if (t.getAsInt() == id) {
                        myTeam = true;
                    }
                }
                if (myTeam) {
                    String awardId = award.getAsJsonObject().get("id").getAsString();
                    if (awardId.equals("winner")) {
                        university.setWins(university.getWins() + 1);
                        university.getWinYears().add(CURRENT_YEAR);
                    } else if (awardId.startsWith("group-winner")) {
                        university.setRegionalChampionships(university.getRegionalChampionships() + 1);
                        university.getRegYears().add(CURRENT_YEAR);
                    } else if (awardId.equals("gold-medal")) {
                        university.setGold(university.getGold() + 1);
                        university.getGoldYears().add(CURRENT_YEAR);
                    } else if (awardId.equals("silver-medal")) {
                        university.setSilver(university.getSilver() + 1);
                        university.getSilverYears().add(CURRENT_YEAR);
                    } else if (awardId.equals("bronze-medal")) {
                        university.setBronze(university.getBronze() + 1);
                        university.getBronzeYears().add(CURRENT_YEAR);
                    }
                }
            }

//            PrintWriter out = new PrintWriter(new FileWriter("temp.txt", true));
//            out.println(id + " " + university.getFullName());
//            out.close();

            panels = new StatsPanel[]{
                    new UnivsersityNamePanel(10000, STATS_WIDTH, record.university, record.team),
                    new PersonStatsPanel(5000, record.contestants[0], false),
                    new PersonStatsPanel(5000, record.contestants[1], false),
                    new PersonStatsPanel(5000, record.contestants[2], false),
                    new PersonStatsPanel(5000, record.coach, true),
                    new AwardsPanel(5000, STATS_WIDTH, record.university, id)
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
        if (fullPeriod == 0) {
            return;
        }
        setGraphics(g.create());
        if (visibilityState == 0) time = 0;
        time += dt;
        time %= fullPeriod;
        g = g.create();
        g.translate(BASE_X, BASE_Y);
        g.clip(0, 0, WIDTH, HEIGHT);
        setGraphics(g);

        int dx = 0;
        int tt = time;
        for (int i = 0; i < panels.length; i++) {
            StatsPanel panel = panels[i];
            if (tt < panel.pauseTime) {
                break;
            }
            tt -= panel.pauseTime;
            if (tt < panel.width / MOVE_SPEED) {
                dx += tt * MOVE_SPEED;
                break;
            }
            tt -= panel.width / MOVE_SPEED;
            dx += panel.width;
        }

        PlateStyle color = QueueStylesheet.name;
        applyStyle(color);
        drawRectangle(0, 0, WIDTH, HEIGHT);
        g.drawImage(logo, LOGO_X + (LOGO_SIZE - logo.getWidth()) / 2, LOGO_X + (LOGO_SIZE - logo.getHeight()) / 2, logo.getWidth(), logo.getHeight(), opacity);

        g.translate(WIDTH - STATS_WIDTH, 0);
        g.clip(0, 0, STATS_WIDTH, HEIGHT);

        int x = 0;
        for (StatsPanel panel : panels) {
            int left = x - dx;
            if (left + panel.width < 0) {
                left += fullWidth;
            }
            AbstractGraphics g1 = g.create();
            g1.translate(left, 0);
            panel.setVisibilityState(visibilityState);
            panel.paintImpl(g1, 1920, 1080);
            x += panel.width;
        }

    }

    static class StatsPanel extends Widget {
        protected final int pauseTime;
        protected final int width;

        public StatsPanel(int pauseTime, int width) {
            this.pauseTime = pauseTime;
            this.width = width;
        }
    }

    static class UnivsersityNamePanel extends StatsPanel {

        private static final Font NAME_FONT = Font.decode(MAIN_FONT + "-" + 60);
        private static final Font NAME_FONT_SMALLER = Font.decode(MAIN_FONT + "-" + 48);
        private static final Font TEXT_FONT = Font.decode(MAIN_FONT + "-" + 28);
        private final University university;
        private final Team team;

        public UnivsersityNamePanel(int pauseTime, int width, University university, Team team) {
            super(pauseTime, Math.max(width, getStringWidth(TEXT_FONT, getInfoText(team, university)) + 20));
            this.university = university;
            this.team = team;
            List<String> ss = new ArrayList<>(team.getRegionals());
            Collections.sort(ss, (o1, o2) -> getNum(o1) - getNum(o2));
            if (team.getRegionals().size() > 2) {
                ss = ss.subList(0, 2);
            }
            team.setRegionals(ss);
        }

        @Override
        protected void paintImpl(AbstractGraphics g, int width, int height) {
            try {
                setGraphics(g.create());
                String[] parts = split(university.getFullName(), NAME_FONT, STATS_WIDTH - 50);
                setTextColor(Color.WHITE);
                if (parts.length == 1) {
                    int y = 32;
                    setFont(NAME_FONT);
                    drawText(parts[0], 0, 80);
                } else {
                    parts = split(university.getFullName(), NAME_FONT_SMALLER, STATS_WIDTH - 50);
                    int y = parts.length == 1 ? 80 : 60;
                    setFont(NAME_FONT_SMALLER);
                    for (int i = 0; i < parts.length; i++) {
                        drawText(parts[i], 0, y);
                        y += 52;
                    }
                }
                setFont(TEXT_FONT);
                String text = getInfoText(team, university);
                drawText(text, 0, parts.length == 1 ? 130 : 160);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static String getInfoText(Team team, University university) {
            String text = team.getName();
            if (university.getHashTag() != null)
                text += " | " + university.getHashTag();
            if (!team.getRegionals().isEmpty())
                text += " | " + String.join(" | ", team.getRegionals());
//                text += " | " + getBest(team.getRegionals());
            text += " | " + university.getRegion();
            return text;
        }

        private static String getBest(Collection<String> regionals) {
            String best = null;
            for (String regional : regionals) {
                if (best == null || getNum(regional) < getNum(best)) {
                    best = regional;
                }
            }
            return best;
        }

        private static int getNum(String s) {
            s = s + ".100.";
            int l = 0;
            while (!Character.isDigit(s.charAt(l))) {
                l++;
            }
            int r = l;
            while (Character.isDigit(s.charAt(r))) {
                r++;
            }
            return Integer.parseInt(s.substring(l, r));
        }
    }

    static class PersonStatsPanel extends StatsPanel {

        private static final Font NAME_FONT = Font.decode(MAIN_FONT + "-" + 38);
        private static final Font TEXT_FONT = Font.decode("Open Sans 18");
        private static final Font RATING_FONT = Font.decode("Open Sans 18").deriveFont(Font.BOLD);
        public static final int COLUMNS_SPACE = 30;
        private final Person person;
        private final boolean coach;

        public PersonStatsPanel(int pauseTime, Person person, boolean coach) {
            super(pauseTime, Math.max(Math.max(
                    500,
                    getStringWidth(NAME_FONT, person.getName() + (coach ? ", coach" : ""))),
                    getAchivementsWidth(person.getAchievements())
            ) + 50);
            this.person = person;
            this.coach = coach;
        }

        private static int getAchivementsWidth(List<Achievement> achievements) {
            int width = 0;
            int maxWidth = 0;
            for (int i = 0; i < achievements.size(); i++) {
                String text = achievements.get(i).achievement;
                maxWidth = Math.max(maxWidth, getStringWidth(TEXT_FONT, text));
                if (i % 3 == 2 || i == achievements.size() - 1) {
                    width += maxWidth + COLUMNS_SPACE;
                    maxWidth = 0;
                }
            }
            return width;
        }

        @Override
        protected void paintImpl(AbstractGraphics g, int width, int height) {
            setGraphics(g.create());
            setFont(NAME_FONT);
            setTextColor(Color.WHITE);
            drawText(person.getName() + (coach ? ", coach" : ""), 0, 48);

            int xx = 0;
            int yy = 80;
            setTextOpacity(getTextOpacity(visibilityState));
            int rating = person.getTcRating();
            if (rating != -1) {
                setTextColor(Color.WHITE);
                setFont(TEXT_FONT);
                String text = "TC: ";
                drawText(text, xx, yy);
                xx += getStringWidth(TEXT_FONT, text);
                setTextColor(getTcColor(rating));
                setFont(RATING_FONT);
                drawText(Integer.toString(rating), xx, yy);
                xx += getStringWidth(TEXT_FONT, Integer.toString(rating));
                xx += 20;
            }
            rating = person.getCfRating();
            if (rating != -1) {
                setTextColor(Color.WHITE);
                setFont(TEXT_FONT);
                String text = "CF: ";
                drawText(text, xx, yy);
                xx += getStringWidth(TEXT_FONT, text);
                setTextColor(getCfColor(rating));
                setFont(RATING_FONT);
                drawText(Integer.toString(rating), xx, yy);
                xx += getStringWidth(TEXT_FONT, Integer.toString(rating));
                xx += 20;
            }
            xx = 0;
            yy += 27;
            setTextColor(Color.WHITE);
            setFont(TEXT_FONT);
            int maxWidth = 0;
            for (int i = 0; i < person.getAchievements().size(); i++) {
                String text = person.getAchievements().get(i).achievement;
                drawText(text, xx, yy + 25 * (i % 3));
                maxWidth = Math.max(maxWidth, getStringWidth(TEXT_FONT, text));
                if (i % 3 == 2) {
                    xx += maxWidth + COLUMNS_SPACE;
                    maxWidth = 0;
                }
            }
        }
    }

    static class AwardsPanel extends StatsPanel {

        private static final int AWARD_SIZE = 70;
        private static final int MEDAL_SIZE = 60;
        public static final int SHIFT = 80;
        private static final Color CUP_COLOR = new Color(0xd6d5cd);
        private static final Color GOLD_COLOR = new Color(0xe9d61d);
        private static final Color SILVER_COLOR = new Color(0xaaaaab);
        private static final Color BRONZE_COLOR = new Color(0xad7329);
        private final University university;

        private static final Font CAPTION_FONT = Font.decode(MAIN_FONT + "-" + 38);
        private static final Font FINALS_FONT = Font.decode(MAIN_FONT + "-" + 88);
        private static final Font YEAR_FONT = Font.decode(MAIN_FONT + "-" + 27);
        private final BufferedImage cupImage;
        private final BufferedImage regionalCupImage;
        private final BufferedImage goldMedalImage;
        private final BufferedImage silverMedalImage;
        private final BufferedImage bronzeMedalImage;

        public AwardsPanel(int pauseTime, int width, University university, int teamId) throws IOException {
            super(pauseTime, width);
            cupImage = getScaledInstance(ImageIO.read(new File("pics/cup.png")), AWARD_SIZE, AWARD_SIZE, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false);
            regionalCupImage = getScaledInstance(ImageIO.read(new File("pics/regional.png")), AWARD_SIZE, AWARD_SIZE, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false);
            goldMedalImage = getScaledInstance(ImageIO.read(new File("pics/gold.png")), MEDAL_SIZE, MEDAL_SIZE, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false);
            silverMedalImage = getScaledInstance(ImageIO.read(new File("pics/silver.png")), MEDAL_SIZE, MEDAL_SIZE, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false);
            bronzeMedalImage = getScaledInstance(ImageIO.read(new File("pics/bronze.png")), MEDAL_SIZE, MEDAL_SIZE, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false);

            this.university = university;
        }

        @Override
        protected void paintImpl(AbstractGraphics g, int width, int height) {
            setGraphics(g.create());
            setFont(CAPTION_FONT);
            setTextColor(Color.WHITE);
            drawText("Finals", 0, 48);
            setFont(FINALS_FONT);
            setTextColor(Color.WHITE);
            drawTextThatFits("" + university.getAppearances(), 0, 80, 100, 60, PlateStyle.Alignment.CENTER, false);

            if (university.getRegionalChampionships() + university.getGold() + university.getSilver() + university.getBronze() == 0) {
                return;
            }

            setFont(CAPTION_FONT);
            setTextColor(Color.WHITE);
            drawText("Awards", 150, 48);
            int x = 150;
            for (int i = 0; i < university.getWins(); i++) {
                drawImage(cupImage, x, 75, "" + university.getWinYears().get(i), CUP_COLOR, false);
                x += SHIFT;
            }
            if (university.getWins() > 0 && university.getRegionalChampionships() > university.getWins()) {
                x += 20;
            }
            {
                int num = university.getRegionalChampionships() - university.getWins();
                if (num > 5) {
                    boolean blinking = university.getRegYears().get(university.getRegionalChampionships() - 1) == CURRENT_YEAR;
                    drawImage(regionalCupImage, x, 75, "", CUP_COLOR, blinking);
                    x += 80;
                    setFont(CAPTION_FONT);
                    setTextColor(CUP_COLOR);
                    drawText("" + university.getRegionalChampionships(), x, 130, blinking ? getBlinkingState() : 1);
                    x += getStringWidth(CAPTION_FONT, "" + university.getRegionalChampionships());
                } else {
                    int k = 0;
                    int t = 0;
                    for (int i = 0; i < num; i++) {
                        while (k < university.getWins() && university.getWinYears().get(k).equals(university.getRegYears().get(t))) {
                            k++; t++;
                        }
                        drawImage(regionalCupImage, x, 75, "" + university.getRegYears().get(t), CUP_COLOR, false);
                        t++;
                        x += SHIFT;
                    }
                }
            }
            if (university.getRegionalChampionships() > 0) {
                x += 20;
            }
            int[] num = {university.getGold(), university.getSilver(), university.getBronze()};
            BufferedImage[] img = {goldMedalImage, silverMedalImage, bronzeMedalImage};
            Color[] colors = {GOLD_COLOR, SILVER_COLOR, BRONZE_COLOR};
            List<Integer>[] years = new List[]{university.getGoldYears(), university.getSilverYears(), university.getBronzeYears()};
            if (x + SHIFT * (university.getGold() + university.getSilver() + university.getBronze()) < this.width) {
                for (int j = 0; j < 3; j++) {
                    for (int i = 0; i < num[j]; i++) {
                        drawImage(img[j], x, 75, "" + years[j].get(i), colors[j], false);
                        x += SHIFT;
                    }
                }
            } else {
                for (int i = 0; i < 3; i++) {
                    if (num[i] > 0) {
                        boolean blinking = years[i].get(num[i] - 1) == CURRENT_YEAR;
                        drawImage(img[i], x, 75, "", colors[i], blinking);
                        x += 70;
                        setFont(CAPTION_FONT);
                        setTextColor(colors[i]);
                        drawText("" + num[i], x, 130, blinking ? getBlinkingState() : 1);
                        x += getStringWidth(CAPTION_FONT, "" + num[i]);
                    }
                }
            }
        }

        private void drawImage(BufferedImage image, int x, int y, String year, Color cupColor, boolean blinking) {
            blinking = blinking || year.equals("" + CURRENT_YEAR);
            double opacity = blinking ? getBlinkingState() : 1;
            graphics.drawImage(image, x + (AWARD_SIZE - image.getWidth()) / 2, y + (AWARD_SIZE - image.getHeight()) / 2, image.getWidth(), image.getHeight(), opacity);
            setTextColor(cupColor);
            setTextOpacity(opacity);
            setFont(YEAR_FONT);
            drawTextThatFits(year, x, y + AWARD_SIZE, AWARD_SIZE, 30, PlateStyle.Alignment.CENTER, false);
            setTextOpacity(1);
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
