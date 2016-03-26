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
//        generator.addWidget(new TeamInfoWidget(
//                updateWait,
//                Widget.BASE_WIDTH,
//                Widget.BASE_HEIGHT,
//                16. / 9,
//                Integer.parseInt(properties.getProperty("sleep.time"))
//        ));
        generator.addWidget(new ClockWidget(updateWait));
        generator.addWidget(new CreepingLineWidget(updateWait));
        generator.addWidget(new DoublePersonWidget(updateWait, timePerson));
        generator.addWidget(new AdvertisementWidget(updateWait, timeAdvertisement));
        generator.addWidget(new StandingsWidget(updateWait));
        generator.addWidget(new QueueWidget(100));
        generator.addWidget(new BigStandingsWidget(529, 69,
                1350, 39, updateWait, 20, true));
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
