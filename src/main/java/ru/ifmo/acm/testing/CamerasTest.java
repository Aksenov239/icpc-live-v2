package ru.ifmo.acm.testing;

import com.sun.jna.NativeLibrary;
import ru.ifmo.acm.backend.player.TickPlayer;
import ru.ifmo.acm.backend.player.generator.ScreenGenerator;
import ru.ifmo.acm.backend.player.widgets.Widget;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * Created by Aksenov239 on 09.04.2017.
 */
public class CamerasTest {
    public static void main(String[] args) throws InterruptedException, InvocationTargetException, IOException {
        new CamerasTest().run();
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

        int rows = Integer.parseInt(properties.getProperty("rows", "4"));
        int columns = Integer.parseInt(properties.getProperty("columns", "4"));

        String aspectRatio = properties.getProperty("aspect.ratio", "16:9");
        int dx = 16;
        int dy = 9;
        if (aspectRatio.equals("4:3")) {
            dx = 4;
            dy = 3;
        }

        int times = Math.min(width / dx / columns, height / dy / rows);

        width = times * columns * dx;
        height = times * rows * dy;

        int sleepTime = Integer.parseInt(properties.getProperty("sleep.time", "3000"));
        int teams = Integer.parseInt(properties.getProperty("teams.number", "120"));
        String contest = properties.getProperty("contest.type", "WF");
        String videoType = properties.getProperty("video.type", "camera");

        ScreenGenerator generator = new ScreenGenerator(width, height, properties, (double) width / Widget.BASE_WIDTH);

        CamerasTestWidget widget = new CamerasTestWidget(sleepTime, teams, rows, columns, aspectRatio, contest, videoType);

        generator.addWidget(widget);

        widget.createForm();

        new TickPlayer("Cameras test", generator, frameRate).frame.setLocation(0, 0);
    }

    private Properties readProperties() {
        Properties properties = new Properties();
        try {
            properties.load(ScreenGenerator.class.getClassLoader().getResourceAsStream("camerastest.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
