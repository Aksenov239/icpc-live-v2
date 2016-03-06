package ru.ifmo.acm.mainscreen;


import ru.ifmo.acm.datapassing.*;

/**
 * Created by Aksenov239 on 15.11.2015.
 */
public class MainScreenData {
    public static MainScreenData getMainScreenData() {
        if (mainScreenData == null) {
            mainScreenData = new MainScreenData();
            //new DataLoader().frontendInitialize();
        }
        return mainScreenData;
    }

    private MainScreenData() {
        advertisementData = new AdvertisementData();
        personData = new PersonData();
        standingsData = new StandingsData();
        teamData = new TeamData();
        cameraData = new CameraData();
        clockData = new ClockData();
    }

    public void update() {
      advertisementData.update();
      personData.update();
      standingsData.update();
    }

    private static MainScreenData mainScreenData;

    public AdvertisementData advertisementData;
    public ClockData clockData;
    public PersonData personData;
    public StandingsData standingsData;
    public TeamData teamData;
    public CameraData cameraData;
}
