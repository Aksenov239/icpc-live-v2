package org.icpclive.backend;

import com.sun.jna.NativeLibrary;
import org.icpclive.Config;
import org.icpclive.backend.player.FramePlayer;
import org.icpclive.backend.player.MemoryFilePlayer;
import org.icpclive.backend.player.generator.ScreenGenerator;
import org.icpclive.backend.player.generator.ScreenGeneratorGL;
import org.icpclive.backend.player.widgets.*;

import javax.imageio.ImageIO;
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

        Properties properties = Config.loadProperties("mainscreen");

        int width = Integer.parseInt(properties.getProperty("width", "1280"));
        int height = Integer.parseInt(properties.getProperty("height", "720"));
        int frameRate = Integer.parseInt(properties.getProperty("rate", "25"));

        String outputMode = properties.getProperty("output.mode", "window");

        ScreenGenerator generator;
        if (outputMode.equals("file")) {
            generator = new ScreenGeneratorGL(width, height, properties, (double) width / Widget.BASE_WIDTH, null);
        } else {
            generator = new ScreenGeneratorGL(width, height, properties,
                    (double) width / Widget.BASE_WIDTH, ImageIO.read(new File("pics/bg.jpg")));
//            generator = new ScreenGeneratorSWT(width, height, properties, (double) width / Widget.BASE_WIDTH);
        }
        long updateWait = Long.parseLong(properties.getProperty("update.wait", "1000"));
        long timeAdvertisement = Long.parseLong(properties.getProperty("advertisement.time"));
        long timePerson = Long.parseLong(properties.getProperty("person.time"));

        generator.addWidget(new NewTeamWidget(500, false));
//                Integer.parseInt(properties.getProperty("sleep.time"))
//        ));

        int plateHeight = 41;
        int bottomY = 1007;
        StandingsWidget standingsWidget = new StandingsWidget(519, 825, plateHeight, updateWait);
        standingsWidget.alignBottom(bottomY);
        generator.addWidget(standingsWidget);

        BigStandingsWidget bigStandingsWidget = new BigStandingsWidget(588, 69,
                1295, plateHeight, updateWait, 22, true);
        bigStandingsWidget.alignBottom(bottomY);
        generator.addWidget(bigStandingsWidget);

        generator.addWidget(new StatisticsWidget(
                588, bottomY, plateHeight, 1295, updateWait
        ));

        generator.addWidget(new DoublePersonWidget(updateWait, timePerson));
        generator.addWidget(new AdvertisementWidget(updateWait, timeAdvertisement));

//        TeamStatsWidget widget = new TeamStatsWidget(updateWait, Integer.parseInt(properties.getProperty("sleep.time")));
//        generator.addWidget(widget);

        generator.addWidget(new PollWidget(updateWait,
                Integer.parseInt(properties.getProperty("poll.top.teams", "5")),
                50,
                1301,
                200,
                80,
                578, bottomY
        ));

        generator.addWidget(new WordStatisticsWidget(updateWait,
                600,
                400,
                200
        ));

        generator.addWidget(new FactWidget(updateWait,
                Widget.BASE_WIDTH - 500,
                600,
                450,
                50
        ));

        generator.addWidget(new PictureWidget(updateWait, 588, 50,
                1295, 1007 - 50, 60));

        generator.addWidget(new PvPWidget(100, false));

//        generator.addWidget(new LocatorWidget(updateWait));

        generator.addWidget(new VideoWidget(updateWait,
                Widget.BASE_WIDTH - 50,
                743,
                900,
                50
        ));

        generator.addWidget(new TestFramesWidget());

        generator.addWidget(new VerticalCreepingLineWidget(updateWait,
                Integer.parseInt(properties.getProperty("creeping.line.rotate.time", "10000")),
                properties.getProperty("creeping.line.logo", "ICPC 2016"),
                Integer.parseInt(properties.getProperty("creeping.line.logo.time", "2000")),
                Integer.parseInt(properties.getProperty("creeping.line.clock.time", "20000")),
                Integer.parseInt(properties.getProperty("creeping.line.logo.change.time", "1000"))));

        boolean showVerdict = Boolean.parseBoolean(properties.getProperty("queue.show.verdict", "true"));
        generator.addWidget(new QueueWidget(30, bottomY, plateHeight, 100, showVerdict));

        if (outputMode.equals("file")) {
            String filename = properties.getProperty("output.file", "c:\\work\\image.bin");
            new MemoryFilePlayer(filename, generator, frameRate);
        } else {
            new FramePlayer("Main", generator, frameRate);
        }
    }
}
