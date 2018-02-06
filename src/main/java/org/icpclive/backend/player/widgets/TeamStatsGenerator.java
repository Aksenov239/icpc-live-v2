package org.icpclive.backend.player.widgets;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author egor@egork.net
 */
public class TeamStatsGenerator {

    public static void main(String[] args) throws IOException {
        TeamStatsWidget generator = new TeamStatsWidget(0, 0);
        for (int i = 1; ; i++) {
            generator.showTeam(i);
            BufferedImage res = new BufferedImage(TeamStatsWidget.WIDTH, TeamStatsWidget.HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = (Graphics2D) res.getGraphics();
            g.drawImage(generator.getUnmovable(), 0, 0, null);
            g.drawImage(generator.getMovable(), generator.getUnmovable().getWidth(), 0, null);
            ImageIO.write(res, "PNG", new File("stats/" + i + ".png"));
        }
    }
}
