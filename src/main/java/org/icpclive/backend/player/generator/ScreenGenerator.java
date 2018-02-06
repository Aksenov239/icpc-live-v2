package org.icpclive.backend.player.generator;

import java.awt.*;
/**
 * Created by Meepo on 5/22/2017.
 */
public interface ScreenGenerator {
    public int getWidth();
    public int getHeight();
    public Image getScreen();
    void draw(Graphics2D g2);
}
