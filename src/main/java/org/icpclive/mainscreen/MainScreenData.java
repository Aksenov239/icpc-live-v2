package org.icpclive.mainscreen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.datapassing.*;
import org.icpclive.ContextListener;

import static org.icpclive.mainscreen.BreakingNews.MainScreenBreakingNews.getUpdaterThread;

/**
 * Created by Aksenov239 on 15.11.2015.
 */
public class MainScreenData {
    private static final Logger log = LogManager.getLogger(MainScreenData.class);

    public static MainScreenData getMainScreenData() {
        if (mainScreenData == null) {
            mainScreenData = new MainScreenData();
            //new DataLoader().frontendInitialize();
            //Start update
            Utils.StoppedThread updater = new Utils.StoppedThread(new Utils.StoppedRunnable() {
                public void run() {
                    while (!stop) {
                        mainScreenData.update();
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            log.error("error", e);
                        }
                    }
                }
            });
            updater.start();
            ContextListener.addThread(updater);

            Utils.StoppedThread breakingNewsTableUpdater = getUpdaterThread();
            breakingNewsTableUpdater.start();
            ContextListener.addThread(breakingNewsTableUpdater);
        }
        return mainScreenData;
    }

    private MainScreenData() {
        advertisementData = new AdvertisementData();
        personData = new PersonData();
        standingsData = new StandingsData();
        teamData = new TeamData();
//        cameraData = new CameraData();
        clockData = new ClockData();
        splitScreenData = new SplitScreenData();
        breakingNewsData = new BreakingNewsData();
        queueData = new QueueData();
        statisticsData = new StatisticsData();
        teamStatsData = new TeamStatsData();
        pollData = new PollData();
        wordStatisticsData = new WordStatisticsData();
        frameRateData = new FrameRateData();
    }

    public void update() {
        advertisementData.update();
        personData.update();
        standingsData.update();
        breakingNewsData.update();
        teamData.update();
        pollData.update();
        wordStatisticsData.update();
    }

    private static MainScreenData mainScreenData;

    public static MainScreenProperties getProperties() {
        return mainScreenData.mainScreenProperties;
    }

    public AdvertisementData advertisementData;
    public ClockData clockData;
    public PersonData personData;
    public StandingsData standingsData;
    public TeamData teamData;
    public CameraData cameraData;
    public SplitScreenData splitScreenData;
    public BreakingNewsData breakingNewsData;
    public QueueData queueData;
    public StatisticsData statisticsData;
    public TeamStatsData teamStatsData;
    public PollData pollData;
    public WordStatisticsData wordStatisticsData;
    public FrameRateData frameRateData;

    private final MainScreenProperties mainScreenProperties = new MainScreenProperties();
}
