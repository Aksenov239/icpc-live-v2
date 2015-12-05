package ru.ifmo.acm.backend.player.generator;

import ru.ifmo.acm.backend.player.widgets.*;

import java.io.IOException;
import java.util.Properties;
import ru.ifmo.acm.backend.player.TickPlayer;

/**
 * Created by aksenov on 14.04.2015.
 */
public class MainScreenGenerator extends ScreenGenerator {
    private static final int width = 1920;
    private static final int height = 1080;

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

        widgets = new Widget[7];
        widgets[0] = new GreenScreenWidget();
        widgets[0].setVisible(true);
        //widgets[0] = new CameraVideoWidget(updateWait, width, height, Integer.parseInt(properties.getProperty("sleep.time")));
        widgets[1] = new TeamInfoWidget(
                updateWait,
                width,
                height - (int)(32 * TickPlayer.scale),
                Integer.parseInt(properties.getProperty("sleep.time"))
        );
        widgets[2] = new ClockWidget(updateWait);
        widgets[3] = new CreepingLineWidget(updateWait);
        widgets[4] = new DoublePersonWidget(updateWait, timePerson);
        widgets[5] = new AdvertisementWidget(updateWait, timeAdvertisement);
        widgets[6] = new StandingsWidget(updateWait);
    }

}
