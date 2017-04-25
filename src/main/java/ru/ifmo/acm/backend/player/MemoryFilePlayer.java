package ru.ifmo.acm.backend.player;

import ru.ifmo.acm.backend.player.generator.ScreenGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.TimerTask;

public class MemoryFilePlayer extends Player {

    public MemoryFilePlayer(String filename, ScreenGenerator generator, int frameRate) throws InterruptedException, InvocationTargetException {
        super(generator);
        int width = generator.getWidth();
        int height = generator.getHeight();

        int length = width * height * 4;
        try {
            out = new RandomAccessFile(filename, "rw")
                    .getChannel().map(FileChannel.MapMode.READ_WRITE, 0, length);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        new java.util.Timer().scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        repaint();
                    }
                }, 0L, 1000 / frameRate);

    }

    private MappedByteBuffer out = null;

    private void repaint() {
        DataBufferByte buf = generator.getBuffer();
        byte[] bytes = buf.getData();
        out.rewind();
        out.put(bytes);
    }

}