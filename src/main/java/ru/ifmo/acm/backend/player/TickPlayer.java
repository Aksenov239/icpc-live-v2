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
    ImagePane imagePane;
    ScreenGenerator generator;
    private Point initialClick;
    private int screenNumber;
    public static double scale;
    public final JFrame frame;


    public TickPlayer(String name, ScreenGenerator generator) throws InterruptedException, InvocationTargetException {
        this(name, generator, generator.getWidth(), generator.getHeight());
    }

    public TickPlayer(String name, ScreenGenerator generator, int width, int height) throws InterruptedException, InvocationTargetException {
        this.generator = generator;

        imagePane = new ImagePane(generator.getScreen());
        imagePane.setSize(width, height);
        imagePane.setMinimumSize(new Dimension(width, height));
        imagePane.setPreferredSize(new Dimension(width, height));

        frame = new JFrame(name);
//        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);
        frame.getContentPane().setLayout(new BorderLayout());

        frame.getContentPane().add(imagePane, BorderLayout.CENTER);

        frame.pack();
//        frame.setResizable(false);
        frame.setVisible(true);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

//        ComponentResizer cr = new ComponentResizer();
//        cr.registerComponent(frame);
//        cr.setSnapSize(new Dimension(10, 10));
//        cr.setMaximumSize(new Dimension(1280, 720));
//        cr.setMinimumSize(new Dimension(10, 10));

        imagePane.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
//                getComponentAt(initialClick);
            }
        });

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 32) {
                    if (screenNumber > 0)
                        GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[screenNumber - 1].setFullScreenWindow(null);
                    screenNumber = (screenNumber + 1) % (GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length + 1);
                    if (screenNumber > 0)
                        GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[screenNumber - 1].setFullScreenWindow(frame);
//                frame.setLocation(0, 0);
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
                int xMoved = (thisX + e.getX()) - (thisX + initialClick.x);
                int yMoved = (thisY + e.getY()) - (thisY + initialClick.y);

                // Move window to this position
                int X = thisX + xMoved;
                int Y = thisY + yMoved;
                frame.setLocation(X, Y);
            }
        });


        new java.util.Timer().scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        imagePane.repaint();
                    }
                }, 0L, 25);// : 40);//40L);

//
    }

    @SuppressWarnings("serial")
    private final class ImagePane extends JPanel {

        private final BufferedImage image;

        public ImagePane(BufferedImage image) {
            this.image = image;
        }

        @Override
        public void paint(Graphics g) {
            g.drawImage(Scalr.resize(generator.getScreen(), this.getWidth(), this.getHeight()), 0, 0, null);
        }
    }

}