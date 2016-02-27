package ru.ifmo.acm.backend.player.generator;

import ru.ifmo.acm.backend.player.TickPlayer;
import ru.ifmo.acm.backend.player.widgets.*;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by aksenov on 14.04.2015.
 */
public class MainScreenGenerator extends ScreenGenerator {
    public static final int width = 1280;//1920 / 2;
    public static final int height = 720;//1080 / 2;

    public MainScreenGenerator() throws IOException {
        super(width, height);

        properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("mainscreen.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        long updateWait = Long.parseLong(properties.getProperty("update.wait", "1000"));
        long timeAdvertisement = Long.parseLong(properties.getProperty("advertisement.time"));
        long timePerson = Long.parseLong(properties.getProperty("person.time"));

        widgets = new Widget[8];
        widgets[0] = new GreenScreenWidget();
        widgets[0].setVisible(true);
        //widgets[0] = new CameraVideoWidget(updateWait, width, height, Integer.parseInt(properties.getProperty("sleep.time")));
        widgets[1] = new TeamInfoWidget(
                updateWait,
                width,
                height - (int) (32 * TickPlayer.scale),
                4. / 3,
                Integer.parseInt(properties.getProperty("sleep.time"))
        );
        widgets[2] = new BigStandingsWidget(0, 0, width, height - (int) (32 * TickPlayer.scale), updateWait, false);
        widgets[3] = new ClockWidget(updateWait);
        widgets[4] = new CreepingLineWidget(updateWait);
        widgets[5] = new DoublePersonWidget(updateWait, timePerson);
        widgets[6] = new AdvertisementWidget(updateWait, timeAdvertisement);
        widgets[7] = new StandingsWidget(updateWait);
    }

}
