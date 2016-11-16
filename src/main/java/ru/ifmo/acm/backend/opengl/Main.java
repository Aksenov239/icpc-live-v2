package ru.ifmo.acm.backend.opengl;

import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.sun.jna.NativeLibrary;
import ru.ifmo.acm.backend.player.widgets.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Aksenov239 on 22.08.2016.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        new Main().run();
    }

    private Point initialClick;
    private int screenNumber = -1;

    private void run() throws IOException {
        String dir = new File(".").getCanonicalPath();
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            if (System.getProperty("sun.arch.data.model").equals("32")) {
                NativeLibrary.addSearchPath("libvlc", dir + "/libvlc/x86");
            } else {
                NativeLibrary.addSearchPath("libvlc", dir + "/libvlc/x64");
            }
        }

        Properties properties = readProperties();
        int width = Integer.parseInt(properties.getProperty("width", "1280"));
        int height = Integer.parseInt(properties.getProperty("height", "720"));
        int frameRate = Integer.parseInt(properties.getProperty("rate", "30"));

        long updateWait = Long.parseLong(properties.getProperty("update.wait", "1000"));
        long timeAdvertisement = Long.parseLong(properties.getProperty("advertisement.time"));
        long timePerson = Long.parseLong(properties.getProperty("person.time"));

        WidgetManager manager = new WidgetManager(properties);

//        manager.addWidget(new NewTeamWidget(
//                Integer.parseInt(properties.getProperty("sleep.time")),
//                Boolean.parseBoolean(properties.getProperty("team.double.video", "false"))));

//        manager.addWidget(new VerticalCreepingLineWidget(updateWait,
//                Integer.parseInt(properties.getProperty("creeping.line.rotate.time", "10000")),
//                properties.getProperty("creeping.line.logo", "ICPC 2016"),
//                Integer.parseInt(properties.getProperty("creeping.line.logo.time", "20000")),
//                Integer.parseInt(properties.getProperty("creeping.line.logo.change.time", "1000"))));

//        StandingsWidget standingsWidget = new StandingsWidget(519, 825, 39, updateWait);
//        standingsWidget.alignBottom(994);
//        manager.addWidget(standingsWidget);
//
//        boolean showVerdict = Boolean.parseBoolean(properties.getProperty("queue.show.verdict", "true"));
//        manager.addWidget(new QueueWidget(30, 994, 39, 100, showVerdict));
//
//        BigStandingsWidget bigStandingsWidget = new BigStandingsWidget(519, 69,
//                1350, 39, updateWait, 20, true);
//        bigStandingsWidget.alignBottom(994);
//        manager.addWidget(bigStandingsWidget);

//        manager.addWidget(new StatisticsWidget(
//                519, 200, 39, 1350, updateWait
//        ));
//
//        manager.addWidget(new DoublePersonWidget(updateWait, timePerson));
//        manager.addWidget(new AdvertisementWidget(updateWait, timeAdvertisement));

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
//        frame.setUndecorated(true);
        frame.pack();
        frame.setVisible(true);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        canvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    GraphicsDevice[] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
                    screenNumber++;
                    if (screenNumber == screenDevices.length) {
                        screenNumber = 0;
                    }
                    Rectangle bounds = screenDevices[screenNumber].getDefaultConfiguration().getBounds();
                    if (screenNumber >= 0)
                        frame.setLocation(bounds.x, bounds.y);
                }
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {

                // get location of Window
                int thisX = frame.getLocation().x;
                int thisY = frame.getLocation().y;

                // Determine how much the mouse moved since the initial click
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;

                // Move window to this position
                int newX = thisX + xMoved;
                int newY = thisY + yMoved;
                frame.setLocation(newX, newY);
            }
        });

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
