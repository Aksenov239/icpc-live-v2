package org.icpclive.backend;

import com.sun.jna.NativeLibrary;
import org.icpclive.Config;
import org.icpclive.backend.player.FramePlayer;
import org.icpclive.backend.player.MemoryFilePlayer;
import org.icpclive.backend.player.generator.ScreenGenerator;
import org.icpclive.backend.player.generator.ScreenGeneratorGL;
import org.icpclive.backend.player.widgets.*;
import org.icpclive.events.ContestInfo;
import org.icpclive.events.EventsLoader;
import org.icpclive.events.TeamInfo;
import org.icpclive.events.WF.json.WFTeamInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

//import org.json.JSONException;

/**
 * @author: pashka
 */
public class MainGenTeamOverlays {

    public static void main(String[] args) throws InterruptedException, InvocationTargetException, IOException {
        new MainGenTeamOverlays().run();
    }

    private void run() throws InterruptedException, InvocationTargetException, IOException {
        Properties properties = Config.loadProperties("mainscreen");

        int width = Integer.parseInt(properties.getProperty("width", "1280"));
        int height = Integer.parseInt(properties.getProperty("height", "720"));
        int frameRate = Integer.parseInt(properties.getProperty("rate", "25"));



//        generator.addWidget(new TeamStatsWidget(
//                Integer.parseInt(properties.getProperty("sleep.time"))
//        ));
        ContestInfo data = EventsLoader.getInstance().getContestData();
        for (TeamInfo team : data.getStandings()) {
            int id = Integer.parseInt(((WFTeamInfo)team).cdsId);
            if (id != 2) continue;
            ScreenGenerator generator = new ScreenGeneratorGL(width, height, properties, (double) width / Widget.BASE_WIDTH, null);
            Widget widget = new TeamStatsWidget(team);
            widget.setVisibilityState(1);
            generator.addWidget(widget);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            generator.draw(image.createGraphics());
            System.out.println(id + " " + ((WFTeamInfo) team).shortName);
            ImageIO.write(image, "png", new File("teamOverlays/" + id + ".png"));
//            return;
        }
    }
}
