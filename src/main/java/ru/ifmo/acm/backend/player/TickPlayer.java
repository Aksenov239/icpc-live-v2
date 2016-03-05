package ru.ifmo.acm.backend.player;

import org.imgscalr.Scalr;
import ru.ifmo.acm.backend.player.generator.ScreenGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.TimerTask;

public class TickPlayer {
    private static final int FRAME_RATE = Integer.getInteger("tickrate", 25);

    ImagePane imagePane;
    ScreenGenerator generator;
    private Point initialClick;
    private int screenNumber = -1;
    public static double scale;
    public final JFrame frame;


    public TickPlayer(String name, ScreenGenerator generator) throws InterruptedException, InvocationTargetException {
        this(name, generator, generator.getWidth(), generator.getHeight());
    }

    public TickPlayer(String name, ScreenGenerator generator, int width, int height) throws InterruptedException, InvocationTargetException {
        this.generator = generator;

        imagePane = new ImagePane();
        imagePane.setSize(width, height);
        imagePane.setMinimumSize(new Dimension(width, height));
        imagePane.setPreferredSize(new Dimension(width, height));

        frame = new JFrame(name);
        frame.setUndecorated(true);
        frame.getContentPane().setLayout(new BorderLayout());

        frame.getContentPane().add(imagePane, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        imagePane.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    GraphicsDevice[] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
                    if (screenNumber >= 0)
                        screenDevices[screenNumber].setFullScreenWindow(null);
                    screenNumber++;
                    if (screenNumber == screenDevices.length) {
                        screenNumber = -1;
                    }
                    if (screenNumber >= 0)
                        screenDevices[screenNumber].setFullScreenWindow(frame);
                }
            }
        });

        imagePane.addMouseMotionListener(new MouseMotionAdapter() {
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


        new java.util.Timer().scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        imagePane.repaint();
                    }
                }, 0L, 1000 / FRAME_RATE);
    }

    @SuppressWarnings("serial")
    private final class ImagePane extends JPanel {
        @Override
        public void paint(Graphics g) {
            g.drawImage(Scalr.resize(generator.getScreen(), this.getWidth(), this.getHeight()), 0, 0, null);
        }
    }

}