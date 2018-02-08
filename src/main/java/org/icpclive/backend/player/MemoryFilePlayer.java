package org.icpclive.backend.player;

import org.icpclive.backend.player.generator.ScreenGenerator;
import org.icpclive.backend.player.generator.ScreenGeneratorGL;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.TimerTask;

public class MemoryFilePlayer extends Player {

    private final int width;
    private final int height;
    private final int length;

    public MemoryFilePlayer(String filename, ScreenGenerator generator, int frameRate) throws InterruptedException, InvocationTargetException {
        super(generator);
        width = generator.getWidth();
        height = generator.getHeight();

        length = width * height * 4;
        try {
            out = new RandomAccessFile(filename, "rw")
                    .getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 2 * length + 4);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

//        new Thread() {
//            @Override
//            public void run() {
//                while (true) {
//                    repaint();
//                }
//            }
//        }.start();

        new java.util.Timer().scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        repaint();
                    }
                }, 0L, 1000 / frameRate);


    }

    private MappedByteBuffer out = null;

    public static int reverseBytes(int i) {
        return ((i >>> 24) |
                (i << 8));
    }

    private void repaint() {
//        DataBufferInt buf = generator.getBuffer();
//        int[] bytes = buf.getData();
//        out.rewind();
//        for (int i = 0; i < bytes.length; i++) {
//            out.putInt(reverseBytes(bytes[i]));
//        }
        ((ScreenGeneratorGL) generator).drawToBuffer(out);
    }
}