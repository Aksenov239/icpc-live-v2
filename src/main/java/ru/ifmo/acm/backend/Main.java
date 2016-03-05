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

        TickPlayer.scale = width / 1280d;

        ScreenGenerator generator = new ScreenGenerator(width, height, properties);
        long updateWait = Long.parseLong(properties.getProperty("update.wait", "1000"));
        long timeAdvertisement = Long.parseLong(properties.getProperty("advertisement.time"));
        long timePerson = Long.parseLong(properties.getProperty("person.time"));

        generator.addWidget(new GreenScreenWidget(true));
        generator.addWidget(new TeamInfoWidget(
                updateWait,
                width,
                height - (int) (32 * TickPlayer.scale),
                4. / 3,
                Integer.parseInt(properties.getProperty("sleep.time"))
        ));
        generator.addWidget(new ClockWidget(updateWait));
        generator.addWidget(new CreepingLineWidget(updateWait));
        generator.addWidget(new DoublePersonWidget(updateWait, timePerson));
        generator.addWidget(new AdvertisementWidget(updateWait, timeAdvertisement));
        generator.addWidget(new StandingsWidget(updateWait));
        generator.addWidget(new BigStandingsWidget(0, 0, width, height - (int) (32 * TickPlayer.scale), updateWait,
                false));
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
