package ru.ifmo.acm.mainscreen.loaders;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.acm.ContextListener;
import ru.ifmo.acm.mainscreen.Polls.PollsData;
import ru.ifmo.acm.mainscreen.Utils;
import twitter4j.*;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by Aksenov239 on 26.03.2017.
 */
public class TwitterLoader extends Utils.StoppedRunnable {
    private static final Logger logger = LogManager.getLogger(TwitterLoader.class);

    private static Twitter twitter;

    private static TwitterLoader instance;

    private static PollsData pollsData;

    private String mainHashTag;

    public static void start() {
        if (instance == null) {
            pollsData = PollsData.getInstance();
            twitter = TwitterFactory.getSingleton();
            instance = new TwitterLoader();
            Utils.StoppedThread twitchThread = new Utils.StoppedThread(instance);
            twitchThread.start();
            ContextListener.addThread(twitchThread);
        }
    }

    private TwitterLoader() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/mainscreen.properties"));
            mainHashTag = properties.getProperty("twitter.hashtag");
        } catch (IOException e) {
            logger.error("error", e);
        }
    }

    public void doOnStatus(Status status) {
//        System.err.println(status.getUser().getId() + " " + status.getText());
        PollsData.vote("Twitter#" + status.getUser().getId(), status.getText());
    }

    private TwitterStream twitterStream;

    public void run() {
        while (true) {
            try {
                twitterStream = new TwitterStreamFactory().getInstance();
                StatusListener statusListener = new StatusListener() {
                    @Override
                    public void onStatus(Status status) {
                        doOnStatus(status);
                    }

                    @Override
                    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

                    }

                    @Override
                    public void onTrackLimitationNotice(int i) {

                    }

                    @Override
                    public void onScrubGeo(long l, long l2) {

                    }

                    @Override
                    public void onStallWarning(StallWarning stallWarning) {

                    }

                    @Override
                    public void onException(Exception e) {

                    }
                };
                FilterQuery filterQuery = new FilterQuery();
                filterQuery.track(mainHashTag);

                twitterStream.addListener(statusListener);
                twitterStream.filter(filterQuery);

                break;
            } catch (Exception e) {
                logger.error("error", e);
            }
        }

        while (!stop) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        twitterStream.cleanUp();
        twitterStream.clearListeners();
    }
}
