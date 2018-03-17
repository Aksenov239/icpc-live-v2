package org.icpclive.webadmin.mainscreen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.Config;
import org.icpclive.webadmin.ContextListener;
import org.icpclive.events.ContestInfo;
import org.icpclive.events.TeamInfo;
import org.icpclive.webadmin.backup.BackUp;
import org.icpclive.events.EventsLoader;

import java.io.IOException;
import java.util.*;

public class MainScreenProperties {
    private static final Logger log = LogManager.getLogger(MainScreenProperties.class);

    public MainScreenProperties() {
        Properties properties = new Properties();
        try {
            properties = Config.loadProperties("mainscreen");
        } catch (IOException e) {
            log.error("Cannot read mainscreen properties file", e);
        }

        latency = Long.parseLong(properties.getProperty("latency.time"));
        backupPersonsFilename = properties.getProperty("backup.persons");
        backupPersons = new BackUp<>(Person.class, backupPersonsFilename);
        personTimeToShow = Long.parseLong(properties.getProperty("person.time")) + latency;

        sleepTime = Integer.parseInt(properties.getProperty("sleep.time"));
        automatedShowTime = Integer.parseInt(properties.getProperty("automated.show.time"));
        automatedInfo = properties.getProperty("automated.info");
        EventsLoader loader = EventsLoader.getInstance();

        String topteamsfilename = properties.getProperty("top.teams.file");
        topteamsids = new HashSet<>();
        //try {
        // topteamsids = Files.lines(Paths.get(topteamsfilename)).mapToInt(Integer::parseInt).collect(Collectors.toSet());
        //          Files.lines(Paths.get(topteamsfilename)).
        //                forEach(topteamsids::add);
        //  } catch (IOException e) {
        //     e.printStackTrace();
        // }

        Utils.StoppedThread loaderThread = new Utils.StoppedThread(new Utils.StoppedRunnable() {
            public void run() {
                loader.run();
            }
        });

        ContextListener.addThread(loaderThread);

        ContestInfo tmpContestInfo;
        while ((tmpContestInfo = loader.getContestData()) == null) {
        }

        contestInfo = tmpContestInfo;

        String onsiteRegex = properties.getProperty("onsite.teams", ".*");
        teamInfos = contestInfo.getStandings();
//        int l = 0;
//        for (int i = 0; i < teamInfos.length; i++) {
//            if (teamInfos[i].getAlias().matches(onsiteRegex)) {
//                teamInfos[l++] = teamInfos[i];
//            }
//        }
//        teamInfos = Arrays.copyOf(teamInfos, l);
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

        pollTimeToShow = Integer.parseInt(properties.getProperty("poll.show.time", "20000"));

        wordTimeToShow = Integer.parseInt(properties.getProperty("word.statistics.word.show.time", "5000"));

        maximumFlowSize = Integer.parseInt(properties.getProperty("creeping.line.maximum.flow", "30"));
        messageLifespanCreepingLine = Integer.parseInt(properties.getProperty("creeping.line.message.lifespan",
                "600000"));
    }

    public long overlayedDelay;

    // Person
    public final long latency;
    public final BackUp<Person> backupPersons;
    public final String backupPersonsFilename;
    public final long personTimeToShow;

    // Team
    public int sleepTime;
    public final int automatedShowTime;
    public final String automatedInfo;
    public final ContestInfo contestInfo;
    public TeamInfo[] teamInfos;
    public static HashSet<String> topteamsids;

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

    // Polls
    public final int pollTimeToShow;

    // Memes
    public final int wordTimeToShow;

    // Creeping line
    public final int maximumFlowSize;
    public final long messageLifespanCreepingLine;
}
