package ru.ifmo.acm.mainscreen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.acm.ContextListener;
import ru.ifmo.acm.backup.BackUp;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.EventsLoader;
import ru.ifmo.acm.events.PCMS.PCMSTeamInfo;
import ru.ifmo.acm.events.TeamInfo;
import ru.ifmo.acm.events.WF.WFTeamInfo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public class MainScreenProperties {
    private static final Logger log = LogManager.getLogger(MainScreenProperties.class);

    public MainScreenProperties() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/mainscreen.properties"));
        } catch (IOException e) {
            log.error("error", e);
        }

        latency = Long.parseLong(properties.getProperty("latency.time"));
        backupPersonsFilename = properties.getProperty("backup.persons");
        backupPersons = new BackUp<>(Person.class, backupPersonsFilename);
        personTimeToShow = Long.parseLong(properties.getProperty("person.time")) + latency;

        sleepTime = Integer.parseInt(properties.getProperty("sleep.time"));
        automatedShowTime = Integer.parseInt(properties.getProperty("automated.show.time"));
        automatedInfo = properties.getProperty("automated.info");
        EventsLoader loader = EventsLoader.getInstance();

        Utils.StoppedThread loaderThread = new Utils.StoppedThread(new Utils.StoppedRunnable() {
            public void run() {
                loader.run();
            }
        });

        ContextListener.addThread(loaderThread);

        contestInfo = loader.getContestData();

        teamInfos = contestInfo.getStandings();
        int l = 0;
        for (int i = 0; i < teamInfos.length; i++) {
            if (teamInfos[i] instanceof PCMSTeamInfo) {
                if (((PCMSTeamInfo) teamInfos[i]).getAlias().startsWith("S")) {
                    teamInfos[l++] = teamInfos[i];
                }
            } else if (teamInfos[i] instanceof WFTeamInfo) {
                l++;
            }
        }
        teamInfos = Arrays.copyOf(teamInfos, l);
        Arrays.sort(teamInfos);

        loaderThread.start();

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

        breakingNewsTimeToShow = Long.parseLong(properties.getProperty("breakingnews.time")) + latency;
        breakingNewsRunsNumber = Integer.parseInt(properties.getProperty("breakingnews.runs.number"));
        backupBreakingNewsFilename = properties.getProperty("backup.breakingnews");

        breakingNewsPatternsFilename = properties.getProperty("breakingnews.patterns.filename");

        overlayedDelay = Long.parseLong(properties.getProperty("overlayed.delay", "4000"));
    }

    public long overlayedDelay;

    // Person
    public final long latency;
    public final BackUp<Person> backupPersons;
    public final String backupPersonsFilename;
    public final long personTimeToShow;

    // Team
    public final int sleepTime;
    public final int automatedShowTime;
    public final String automatedInfo;
    public final ContestInfo contestInfo;
    public TeamInfo[] teamInfos;

    // Camera
    public final int cameraNumber;
    public final String[] cameraURLs;
    public final String[] cameraNames;

    // Advertisement
    public final String backupAdvertisementsFilename;
    public final long timeAdvertisement;
    public final BackUp<Advertisement> backupAdvertisements;

    // Breaking News
    public final long breakingNewsTimeToShow;
    public final int breakingNewsRunsNumber;
    public final String backupBreakingNewsFilename;

    public final String breakingNewsPatternsFilename;
}
