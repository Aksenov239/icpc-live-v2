package ru.ifmo.acm.backend.player.generator;

import ru.ifmo.acm.backend.player.widgets.*;

import java.io.IOException;
import java.util.Properties;

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

        widgets = new Widget[5];
        widgets[0] = new GreenScreenWidget();
        widgets[0].setVisible(true);
        widgets[1] = new ClockWidget(updateWait);
        widgets[2] = new CreepingLineWidget(updateWait);
        widgets[3] = new DoublePersonWidget(updateWait, timePerson);
        widgets[4] = new AdvertisementWidget(updateWait, timeAdvertisement);
    }

}
