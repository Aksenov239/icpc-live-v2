package ru.ifmo.acm.backend.player.widgets;

import ru.ifmo.acm.datapassing.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ru.ifmo.acm.datapassing.CachedData;

/**
 * @author: pashka
 */
public class GreenScreenWidget extends Widget {
    private final Color color = new Color(255, 0, 255);

    public GreenScreenWidget(boolean isVisible) {
        setVisible(isVisible);
    }

    @Override
    public void paintImpl(Graphics2D g, int width, int height) {
        if (isVisible()) {
            g.setColor(color);
            g.fillRect(0, 0, width, height);
        }
    }

    public CachedData getCorrespondingData(Data data) {
        return null;
    }
}
