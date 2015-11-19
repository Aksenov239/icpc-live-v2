package ru.ifmo.acm.mainscreen;

import ru.ifmo.acm.mainscreen.statuses.*;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by Aksenov239 on 15.11.2015.
 */
public class MainScreenData {
public static MainScreenData getMainScreenData() {
        if (mainScreenData == null) {
            mainScreenData = new MainScreenData();
        }
        return mainScreenData;
    }

    private MainScreenData() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/persons.properties"));
            String backupPersons = properties.getProperty("backup.file.name");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static MainScreenData mainScreenData;

    public AdvertisementStatus advertisementStatus;
    public ClockStatus clockStatus;
    public PersonStatus personStatus;
    public StandingsStatus standingsStatus;
    public TeamStatus teamStatus;
}
