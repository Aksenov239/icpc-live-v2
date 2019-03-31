package org.icpclive.backend.player.widgets.locator;

import java.awt.image.BufferedImage;

/**
 * @author: pashka
 */
public interface MJpegViewer {
    void setBufferedImage(BufferedImage image);

    void repaint();

    void setFailedString(String s);

}
