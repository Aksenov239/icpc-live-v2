package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.backup.BackUp;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.EventsLoader;
import ru.ifmo.acm.events.PCMS.PCMSEventsLoader;
import ru.ifmo.acm.events.PCMS.PCMSTeamInfo;
import ru.ifmo.acm.events.TeamInfo;
import ru.ifmo.acm.mainscreen.Advertisement;
import ru.ifmo.acm.mainscreen.Person;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public class MainScreenProperties {
    private MainScreenProperties() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/mainscreen.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        latency = Long.parseLong(properties.getProperty("latency.time"));
        backupPersonsFilename = properties.getProperty("backup.persons");
        backupPersons = new BackUp<>(Person.class, backupPersonsFilename);
        personTimeToShow = Long.parseLong(properties.getProperty("person.time")) + latency;

        sleepTime = Integer.parseInt(properties.getProperty("sleep.time"));
        EventsLoader loader = PCMSEventsLoader.getInstance();
        contestInfo = loader.getContestData();

        TeamInfo[] teamInfos = contestInfo.getStandings();
        teamNames = new String[teamInfos.length];
        int l = 0;
        for (int i = 0; i < teamNames.length; i++) {
            if (((PCMSTeamInfo) teamInfos[i]).getAlias().startsWith("S")) {
                teamNames[l++] = teamInfos[i].getShortName() + " :" + ((PCMSTeamInfo) teamInfos[i]).getAlias();
            }
        }
        teamNames = Arrays.copyOf(teamNames, l);
        Arrays.sort(teamNames);


        cameraNumber = Integer.parseInt(properties.getProperty("camera.number", "0"));

        cameraURLs = new String[cameraNumber];
        cameraNames = new String[cameraNumber];
        for (int i = 0; i < cameraNumber; i++) {
            cameraURLs[i] = properties.getProperty("camera.url." + (i + 1));
            cameraNames[i] = properties.getProperty("camera.name." + (i + 1));
        }

        backupAdvertisementsFilename = properties.getProperty("backup.advertisements");
        timeAdvertisement = Long.parseLong(properties.getProperty("advertisement.time")) + latency;
        backupAdvertisements = new BackUp<>(Advertisement.class, backupAdvertisementsFilename);
    }

    public static MainScreenProperties getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MainScreenProperties();
        }

        return INSTANCE;
    }

    // Person
    public static long latency;
    public static BackUp<Person> backupPersons;
    public static String backupPersonsFilename;
    public static long personTimeToShow;

    // Team
    public static int sleepTime;
    public static ContestInfo contestInfo;
    public static String[] teamNames;

    // Camera
    public static int cameraNumber;
    public static String[] cameraURLs;
    public static String[] cameraNames;

    // Advertisement
    public static String backupAdvertisementsFilename;
    public static long timeAdvertisement;
    public static BackUp<Advertisement> backupAdvertisements;

    private static MainScreenProperties INSTANCE = null;

}
