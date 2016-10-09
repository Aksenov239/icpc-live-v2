package ru.ifmo.acm.backend.opengl;

import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import ru.ifmo.acm.backend.player.widgets.ClockWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Aksenov239 on 22.08.2016.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        new Main().run();
    }

    private void run() throws IOException {
        Properties properties = readProperties();
        int width = Integer.parseInt(properties.getProperty("width", "1280"));
        int height = Integer.parseInt(properties.getProperty("height", "720"));
        int frameRate = Integer.parseInt(properties.getProperty("rate", "30"));

        long updateWait = Long.parseLong(properties.getProperty("update.wait", "1000"));

        WidgetManager manager = new WidgetManager(properties);

        manager.addWidget(new ClockWidget(updateWait));

        GLCanvas canvas = new GLCanvas();
        canvas.addGLEventListener(manager);
        canvas.setPreferredSize(new Dimension(width, height));
        final FPSAnimator animator = new FPSAnimator(canvas, frameRate, true);

        final JFrame frame = new JFrame();
        frame.getContentPane().add(canvas);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                new Thread() {
                    public void run() {
                        animator.stop();
                        System.exit(0);
                    }
                }.start();
            }
        });
        frame.setTitle("Main screen");
        frame.pack();
        frame.setVisible(true);
        animator.start();
    }

    private Properties readProperties() {
        Properties properties = new Properties();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream("mainscreen.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
