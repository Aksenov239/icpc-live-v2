package ru.ifmo.acm.backend;

import com.sun.jna.NativeLibrary;
import ru.ifmo.acm.backend.player.TickPlayer;
import ru.ifmo.acm.backend.player.generator.MainScreenGenerator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

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

        new TickPlayer("Main screen", new MainScreenGenerator()).frame.setLocation(0, 0);
    }
}
