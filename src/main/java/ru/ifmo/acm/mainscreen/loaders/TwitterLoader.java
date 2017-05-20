package ru.ifmo.acm.mainscreen.loaders;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.acm.ContextListener;
import ru.ifmo.acm.creepingline.MessageData;
import ru.ifmo.acm.mainscreen.MainScreenData;
import ru.ifmo.acm.mainscreen.Polls.PollsData;
import ru.ifmo.acm.mainscreen.Utils;
import ru.ifmo.acm.mainscreen.Words.WordStatisticsData;
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

    public static TwitterLoader getInstance() {
        return instance;
    }

    public static void start() {
        if (instance == null) {
            pollsData = PollsData.getInstance();
            twitter = TwitterFactory.getSingleton();
            instance = new TwitterLoader();
            Utils.StoppedThread twitterThread = new Utils.StoppedThread(instance);
            twitterThread.start();
            ContextListener.addThread(twitterThread);
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

    public synchronized void postMessage(String text) {
        try {
            twitter.updateStatus(text);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    public void doOnStatus(Status status) {
        System.err.println(status.getUser().getId() + " " + status.getText());
        WordStatisticsData.vote(WordStatisticsData.TWEET_KEYWORD + " " + status.getText());
//        System.err.println(status.getUser().getId() + " " + status.getText());
        if (status.getText().startsWith(mainHashTag + " ")) {
            PollsData.vote("Twitter#" + status.getUser().getId(),
                    status.getText().substring(mainHashTag.length() + 1));
        }
        MessageData.processTwitterMessage(status);
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
