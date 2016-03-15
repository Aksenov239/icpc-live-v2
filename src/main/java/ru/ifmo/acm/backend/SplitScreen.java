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
public class SplitScreen {

    public static void main(String[] args) throws InterruptedException, InvocationTargetException, IOException {
        new SplitScreen().run();
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

        generator.addWidget(new GreenScreenWidget(true));
        generator.addWidget(new SplitScreenWidget(
                updateWait,
                Widget.BASE_WIDTH,
                Widget.BASE_HEIGHT - 32,
                4. / 3,
                Integer.parseInt(properties.getProperty("sleep.time"))
        ));
        generator.addWidget(new ClockWidget(updateWait));
        generator.addWidget(new CreepingLineWidget(updateWait));
//        generator.addWidget(new QueueWidget(100));
        new TickPlayer("Main screen", generator, frameRate).frame.setLocation(0, 0);
    }

    private Properties readProperties() {
        Properties properties = new Properties();
        try {
            properties.load(ScreenGenerator.class.getClassLoader().getResourceAsStream("splitscreen.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
