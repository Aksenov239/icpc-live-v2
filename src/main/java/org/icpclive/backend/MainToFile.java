package org.icpclive.backend;

import com.sun.jna.NativeLibrary;
import org.icpclive.backend.player.MemoryFilePlayer;
import org.icpclive.backend.player.generator.ScreenGeneratorGL;
import org.icpclive.backend.player.widgets.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

//import org.json.JSONException;

/**
 * @author: pashka
 */
public class MainToFile {

    public static void main(String[] args) throws InterruptedException, InvocationTargetException, IOException {
        new MainToFile().run();
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

        ScreenGeneratorGL generator = new ScreenGeneratorGL(width, height, properties, (double) width / Widget.BASE_WIDTH);
        long updateWait = Long.parseLong(properties.getProperty("update.wait", "1000"));
        long timeAdvertisement = Long.parseLong(properties.getProperty("advertisement.time"));
        long timePerson = Long.parseLong(properties.getProperty("person.time"));

        generator.addWidget(new NewTeamWidget(
                Integer.parseInt(properties.getProperty("sleep.time")),
                Boolean.parseBoolean(properties.getProperty("team.double.video", "false"))));

        generator.addWidget(new VerticalCreepingLineWidget(updateWait,
                Integer.parseInt(properties.getProperty("creeping.line.rotate.time", "10000")),
                properties.getProperty("creeping.line.logo", "ICPC 2016"),
                Integer.parseInt(properties.getProperty("creeping.line.logo.time", "2000")),
                Integer.parseInt(properties.getProperty("creeping.line.clock.time", "20000")),
                Integer.parseInt(properties.getProperty("creeping.line.logo.change.time", "1000"))));

        StandingsWidget standingsWidget = new StandingsWidget(519, 825, 39, updateWait);
        standingsWidget.alignBottom(994);
        generator.addWidget(standingsWidget);

        boolean showVerdict = Boolean.parseBoolean(properties.getProperty("queue.show.verdict", "true"));
        generator.addWidget(new QueueWidget(30, 994, 39, 100, showVerdict));

        BigStandingsWidget bigStandingsWidget = new BigStandingsWidget(519, 69,
                1371, 39, updateWait, 22, true);
        bigStandingsWidget.alignBottom(994);
        generator.addWidget(bigStandingsWidget);

        generator.addWidget(new StatisticsWidget(
                519, 994, 39, 1371, updateWait
        ));

//        generator.addWidget(new OldBreakingNewsWidget(
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

//        generator.addWidget(new ClockWidget(updateWait));

        TeamStatsWidget widget = new TeamStatsWidget(updateWait, Integer.parseInt(properties.getProperty("sleep.time")));
        generator.addWidget(widget);

        generator.addWidget(new PollWidget(updateWait,
                Integer.parseInt(properties.getProperty("poll.show.time", "20000")),
                Integer.parseInt(properties.getProperty("poll.top.teams", "5")),
                519,
                50,
                1371,
                200,
                80,
                519, 994
        ));

        generator.addWidget(new WordStatisticsWidget(updateWait,
                600,
                400,
                200
        ));

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

        generator.addWidget(new TestFramesWidget());

//        new FramePlayer("Main", generator, frameRate);
        String filename = properties.getProperty("outputFile", "c:\\work\\image.bin");
        new MemoryFilePlayer(filename, generator, frameRate);
    }

    private Properties readProperties() {
        Properties properties = new Properties();
        try {
            properties.load(ScreenGeneratorGL.class.getClassLoader().getResourceAsStream("mainscreen.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
