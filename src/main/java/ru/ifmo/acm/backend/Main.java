package ru.ifmo.acm.backend;

import com.sun.jna.NativeLibrary;
import ru.ifmo.acm.backend.player.TickPlayer;
import ru.ifmo.acm.backend.player.generator.ScreenGenerator;
import ru.ifmo.acm.backend.player.widgets.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

//import org.json.JSONException;

/**
 * @author: pashka
 */
public class Main {

    public static void main(String[] args) throws InterruptedException, InvocationTargetException, IOException {
        new Main().run();
    }

    private void run() throws InterruptedException, InvocationTargetException, IOException {
        String dir = new File(".").getCanonicalPath();
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            if (System.getProperty("sun.arch.data.model").equals("32")) {
                NativeLibrary.addSearchPath("libvlc", dir + "/libvlc/x86");
            } else {
                NativeLibrary.addSearchPath("libvlc", dir + "/libvlc/x64");
            }
        } else {
            NativeLibrary.addSearchPath("vlc", "/Applications/VLC.app/Contents/MacOS/lib");
        }

        Properties properties = readProperties();
        int width = Integer.parseInt(properties.getProperty("width", "1280"));
        int height = Integer.parseInt(properties.getProperty("height", "720"));
        int frameRate = Integer.parseInt(properties.getProperty("rate", "25"));

        ScreenGenerator generator = new ScreenGenerator(width, height, properties, (double) width / Widget.BASE_WIDTH);
        long updateWait = Long.parseLong(properties.getProperty("update.wait", "1000"));
        long timeAdvertisement = Long.parseLong(properties.getProperty("advertisement.time"));
        long timePerson = Long.parseLong(properties.getProperty("person.time"));

        generator.addWidget(new GreenScreenWidget(true));

        generator.addWidget(new NewTeamWidget(
                Integer.parseInt(properties.getProperty("sleep.time")),
                Boolean.parseBoolean(properties.getProperty("team.double.video", "false"))));

        generator.addWidget(new VerticalCreepingLineWidget(updateWait,
                Integer.parseInt(properties.getProperty("creeping.line.rotate.time", "10000")),
                properties.getProperty("creeping.line.logo", "ICPC 2016"),
                Integer.parseInt(properties.getProperty("creeping.line.logo.time", "20000")),
                Integer.parseInt(properties.getProperty("creeping.line.logo.change.time", "1000"))));

        StandingsWidget standingsWidget = new StandingsWidget(519, 825, 39, updateWait);
        standingsWidget.alignBottom(994);
        generator.addWidget(standingsWidget);

        generator.addWidget(new QueueWidget(30, 994, 39, 100));

        BigStandingsWidget bigStandingsWidget = new BigStandingsWidget(519, 69,
                1350, 39, updateWait, 20, true);
        bigStandingsWidget.alignBottom(994);
        generator.addWidget(bigStandingsWidget);

        generator.addWidget(new StatisticsWidget(
                519, 200, 39, 1350, updateWait
        ));

//        generator.addWidget(new BreakingNewsWidget(
//                updateWait,
//                (int)(Widget.BASE_WIDTH * 0.65),
//                (int)(Widget.BASE_HEIGHT * 0.6),
//                (int)(Widget.BASE_WIDTH * 0.3),
//                (int)(Widget.BASE_HEIGHT * 0.2),
//                16. / 9,
//                Integer.parseInt(properties.getProperty("sleep.time")),
//                Integer.parseInt(properties.getProperty("breakingnews.time"))
//        ));

        generator.addWidget(new DoublePersonWidget(updateWait, timePerson));
        generator.addWidget(new AdvertisementWidget(updateWait, timeAdvertisement));

        generator.addWidget(new ClockWidget(updateWait));

        TeamStatsWidget widget = new TeamStatsWidget(updateWait, Integer.parseInt(properties.getProperty("sleep.time")));
        generator.addWidget(widget);

//        new Timer().schedule(new TimerTask() {
//            int id = 1;
//
//            @Override
//            public void run() {
//                widget.showTeam(id);
//                id++;
//                if (id == 129) {
//                    id = 1;
//                }
//            }
//        }, 1000, 32000);
//
        new TickPlayer("Main screen", generator, frameRate).frame.setLocation(0, 0);
    }

    private Properties readProperties() {
        Properties properties = new Properties();
        try {
            properties.load(ScreenGenerator.class.getClassLoader().getResourceAsStream("mainscreen.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
