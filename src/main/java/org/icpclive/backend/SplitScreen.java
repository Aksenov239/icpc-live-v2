package org.icpclive.backend;

import com.sun.jna.NativeLibrary;
import org.icpclive.backend.player.FramePlayer;
import org.icpclive.backend.player.generator.ScreenGeneratorGL;
import org.icpclive.backend.player.generator.ScreenGeneratorSWT;
import org.icpclive.backend.player.widgets.old.ClockWidget;
import org.icpclive.backend.player.widgets.SplitScreenWidget;
import org.icpclive.backend.player.widgets.Widget;
import org.icpclive.Config;

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

        ScreenGeneratorSWT generator = new ScreenGeneratorSWT(width, height, readMainProperties(), (double) width / Widget.BASE_WIDTH);
        long updateWait = Long.parseLong(properties.getProperty("update.wait", "1000"));

        // generator.addWidget(new GreenScreenWidget(true));
        generator.addWidget(new SplitScreenWidget(
                updateWait,
                Widget.BASE_WIDTH,
                Widget.BASE_HEIGHT,
                16. / 9,
                Integer.parseInt(properties.getProperty("sleep.time"))
        ));
        generator.addWidget(new ClockWidget(updateWait));
//        generator.addWidget(new QueueWidget(100));
        new FramePlayer("Split screen", generator, frameRate).frame.setLocation(0, 0);
    }

    private Properties readProperties() {
        Properties properties;
        try {
            properties = Config.loadProperties("splitscreen");
//            properties.load(ScreenGeneratorGL.class.getClassLoader().getResourceAsStream("splitscreen.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    private Properties readMainProperties() {
        Properties properties;
        try {
            properties = Config.loadProperties("mainscreen");
//            properties.load(ScreenGeneratorGL.class.getClassLoader().getResourceAsStream("mainscreen.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}

