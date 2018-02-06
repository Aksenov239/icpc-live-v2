package org.icpclive.testing;

import org.icpclive.backend.player.urls.TeamUrls;
import org.icpclive.backend.graphics.Graphics;
import org.icpclive.backend.player.widgets.PlayerWidget;
import org.icpclive.backend.player.widgets.Widget;
import org.icpclive.datapassing.CachedData;
import org.icpclive.datapassing.Data;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Aksenov239 on 09.04.2017.
 */
public class CamerasTestWidget extends Widget {
    int videoWidth;
    int videoHeight;
    int sleepTime;

    private int oldPage;
    private int currentPage;
    private int totalTeams;
    private String contest;
    private String videoType;
    private int onPage;

    PlayerWidget[][] cameraWidgets;

    private long lastChange;
    private boolean inChange;

    Font font = new Font("Open Sans", Font.BOLD, 30);

    public class PageChoice extends JPanel {
        public PageChoice() {
            JComboBox<Integer> pages = new JComboBox<>();
            for (int page = 1; page < (totalTeams + onPage - 1) / onPage; page++) {
                pages.addItem(page);
            }
            pages.setSelectedIndex(0);
            JButton button = new JButton("Show");
            add(pages);
            add(button);

            button.addActionListener(e -> {
                if (inChange) {
                    return;
                }

                int selectedPage = (int) pages.getSelectedItem();
                if (currentPage == selectedPage) {
                    return;
                }
                oldPage = currentPage;
                currentPage = selectedPage;
                int id = onPage * (currentPage - 1) + 1;
                for (int i = 0; i < cameraWidgets.length; i++) {
                    for (int j = 0; j < cameraWidgets[i].length; j++) {
                        if (id > totalTeams) {
                            cameraWidgets[i][j].stop();
                        }
                        cameraWidgets[i][j].loadNext(TeamUrls.getUrl(
                                id++,
                                contest, videoType));
                    }
                }
                lastChange = System.currentTimeMillis();
                inChange = true;
            });
        }
    }

    public void createForm() {
        JFrame frame = new JFrame("Controller");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(new PageChoice());

        frame.pack();
        frame.setVisible(true);
    }

    public CamerasTestWidget(int sleepTime, int totalTeams,
                             int rows, int columns, String aspectRatio,
                             String contest, String videoType) {
        int dx = 16;
        int dy = 9;
        if (aspectRatio.equals("4:3")) {
            dx = 4;
            dy = 3;
        }
        int times = Math.min(Widget.BASE_WIDTH / columns / dx, Widget.BASE_HEIGHT / rows / dy);
        videoWidth = times * dx;
        videoHeight = times * dy;
        this.sleepTime = sleepTime;

        this.totalTeams = totalTeams;

        this.contest = contest;
        this.videoType = videoType;

        currentPage = 1;
        oldPage = 1;

        onPage = rows * columns;

        cameraWidgets = new PlayerWidget[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                cameraWidgets[i][j] = PlayerWidget.getPlayerWidget(videoWidth * j,
                        videoHeight * i, videoWidth, videoHeight, sleepTime, 0);
                cameraWidgets[i][j].change(TeamUrls.getUrl(i * columns + j + 1, contest, videoType));
            }
        }
    }

    public void paintImpl(Graphics g, int width, int height) {
        if (inChange && lastChange + sleepTime < System.currentTimeMillis()) {
            oldPage = currentPage;
            int id = (currentPage - 1) * onPage + 1;
            for (int i = 0; i < cameraWidgets.length; i++) {
                for (int j = 0; j < cameraWidgets[i].length; j++) {
                    if (id > totalTeams) {
                        continue;}
                    cameraWidgets[i][j].switchToNext();
                    id++;
                }
            }
            inChange = false;
        }

        int id = (oldPage - 1) * onPage + 1;
        for (int i = 0; i < cameraWidgets.length; i++) {
            for (int j = 0; j < cameraWidgets[i].length; j++) {
                if (id > totalTeams) {
                    continue;
                }
                cameraWidgets[i][j].paint(g, width, height);
                int x = videoWidth * (j + 1) - 50;
                int y = videoHeight * (i + 1) - 5;
                g.drawString("" + id, x, y, font, Color.WHITE);
                id++;
            }
        }
    }

    public CachedData getCorrespondingData(Data data) {
        return null;
    }
}
