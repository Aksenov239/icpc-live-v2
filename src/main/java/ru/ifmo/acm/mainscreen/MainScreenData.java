package ru.ifmo.acm.mainscreen;


import ru.ifmo.acm.ContextListener;
import ru.ifmo.acm.datapassing.ClockData;
import ru.ifmo.acm.datapassing.StandingsData;
import ru.ifmo.acm.mainscreen.statuses.AdvertisementStatus;
import ru.ifmo.acm.mainscreen.statuses.CameraStatus;
import ru.ifmo.acm.mainscreen.statuses.PersonStatus;
import ru.ifmo.acm.mainscreen.statuses.TeamStatus;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by Aksenov239 on 15.11.2015.
 */
public class MainScreenData {
    public static MainScreenData getMainScreenData() {
        if (mainScreenData == null) {
            mainScreenData = new MainScreenData();
            //new DataLoader().frontendInitialize();
            //Start update
            Utils.StoppedThread updater = new Utils.StoppedThread(new Utils.StoppedRunnable() {
                public void run() {
                    while (true) {
                        mainScreenData.update();
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            updater.start();
            ContextListener.addThread(updater);
        }
        return mainScreenData;
    }

    private MainScreenData() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/mainscreen.properties"));

            String backupAdvertisements = properties.getProperty("backup.advertisements");

            long latency = Long.parseLong(properties.getProperty("latency.time"));

            long timeAdvertisement = Long.parseLong(properties.getProperty("advertisement.time")) + latency;
            advertisementStatus = new AdvertisementStatus(backupAdvertisements, timeAdvertisement);

            String backupPersons = properties.getProperty("backup.persons");
            long timePerson = Long.parseLong(properties.getProperty("person.time")) + latency;
            personStatus = new PersonStatus(backupPersons, timePerson);

            standingsData = new StandingsData();
            StandingsData.latency = latency;

            int sleepTime = Integer.parseInt(properties.getProperty("sleep.time"));

            teamStatus = new TeamStatus(sleepTime);

            cameraStatus = new CameraStatus(sleepTime);
        } catch (IOException e) {
            e.printStackTrace();
        }
        clockData = new ClockData();
    }

    public void update() {
        advertisementStatus.update();
        personStatus.update();
        standingsData.update();
    }

    private static MainScreenData mainScreenData;

    public AdvertisementStatus advertisementStatus;
    public ClockData clockData;
    public PersonStatus personStatus;
    public StandingsData standingsData;
    public TeamStatus teamStatus;
    public CameraStatus cameraStatus;
}
