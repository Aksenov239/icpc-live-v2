package ru.ifmo.acm.backend.player;

import ru.ifmo.acm.backend.player.generator.ScreenGenerator;
import ru.ifmo.acm.backend.player.generator.ScreenGeneratorGL;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.lang.reflect.InvocationTargetException;
import java.util.TimerTask;

public class FramePlayer extends Player {
    private final JLabel label;
    ImagePane imagePane;
    private Point initialClick;
    private int screenNumber = -1;
    public final JFrame frame;


    public FramePlayer(String name, ScreenGenerator generator, int frameRate) throws InterruptedException, InvocationTargetException {
        super(generator);
        int width = generator.getWidth();
        int height = generator.getHeight();

        label = new JLabel(new ImageIcon(generator.getScreen()));

        frame = new JFrame(name);
        frame.setUndecorated(true);

        frame.add(label);

        frame.pack();
        frame.setVisible(true);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        label.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    GraphicsDevice[] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
//                    if (screenNumber >= 0)
//                        screenDevices[screenNumber].setFullScreenWindow(null);
                    screenNumber++;
                    if (screenNumber == screenDevices.length) {
                        screenNumber = 0;
                    }
                    Rectangle bounds = screenDevices[screenNumber].getDefaultConfiguration().getBounds();
                    if (screenNumber >= 0)
//                        screenDevices[screenNumber].setFullScreenWindow(frame);
                        frame.setLocation(bounds.x, bounds.y);
                }
            }
        });

        label.addMouseMotionListener(new MouseMotionAdapter() {
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
                        repaint();
                    }
                }, 0L, 1000 / frameRate);

    }

    private void repaint() {
        label.setIcon(new ImageIcon(generator.getScreen()));
    }

    @SuppressWarnings("serial")
    private final class ImagePane extends JPanel {
        @Override
        public void paint(Graphics g) {
//            AffineTransform transform = AffineTransform.getTranslateInstance(0, generator.getHeight());
//            transform.concatenate(AffineTransform.getScaleInstance(1, -1));
//            ((Graphics2D)g).setTransform(transform);
            g.drawImage(generator.getScreen(), 0, 0, null);
        }
    }

}