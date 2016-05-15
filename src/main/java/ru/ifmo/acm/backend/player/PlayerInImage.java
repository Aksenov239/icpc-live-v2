package ru.ifmo.acm.backend.player;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * @author: pashka
 */
public class PlayerInImage {
    private static final Logger log = LogManager.getLogger(PlayerInImage.class);

    private final BufferedImage image;
    private JComponent frame;
    private final MediaPlayer mediaPlayer;

    public PlayerInImage(int width, int height, JComponent frame, String media) {
        image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height);
        image.setAccelerationPriority(1.0f);
        this.frame = frame;
        MediaPlayerFactory factory = new MediaPlayerFactory(new String[0]);
        mediaPlayer = factory.newDirectMediaPlayer(new TestBufferFormatCallback(), new TestRenderCallback());
        mediaPlayer.setRepeat(true);
//        mediaPlayer.addMediaOptions(":file-caching=1500");
//        mediaPlayer.setStandardMediaOptions(":file-caching=1500");
//        mediaPlayer.setVolume(0);
        mediaPlayer.setAspectRatio("4:3");
        if (media != null)
            mediaPlayer.playMedia(media, ":file-caching=0");
        log.info("PLAY!!! " + media);
    }

    public void stop() {
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    public JComponent getComponent() {
        return frame;
    }

    public void setComponent(JComponent frame) {
        this.frame = frame;
    }

    public MediaPlayer getPlayer() {
        return mediaPlayer;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setVolume(int volume) {
        mediaPlayer.setVolume(volume);
    }

    private final class TestRenderCallback extends RenderCallbackAdapter {

        public TestRenderCallback() {
            super(((DataBufferInt) image.getRaster().getDataBuffer()).getData());
        }

        @Override
        public void onDisplay(DirectMediaPlayer mediaPlayer, int[] data) {
            // The image data could be manipulated here...

            /* RGB to GRAYScale conversion example */
//            for(int i=0; i < data.length; i++){
//                int argb = data[i];
//                int b = (argb & 0xFF);
//                int g = ((argb >> 8 ) & 0xFF);
//                int r = ((argb >> 16 ) & 0xFF);
//                int grey = (r + g + b + g) >> 2 ; //performance optimized - not real grey!
//                data[i] = (grey << 16) + (grey << 8) + grey;
//            }
            if (frame != null)
                frame.repaint();
        }
    }

    private final class TestBufferFormatCallback implements BufferFormatCallback {

        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            return new RV32BufferFormat(image.getWidth(), image.getHeight());
        }

    }
}
