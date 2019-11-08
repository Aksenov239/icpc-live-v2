package org.icpclive.webadmin.mainscreen.loaders;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.Config;
import org.icpclive.webadmin.creepingline.MessageData;
import org.icpclive.webadmin.ContextListener;
import org.icpclive.webadmin.mainscreen.Polls.PollsData;
import org.icpclive.webadmin.mainscreen.Utils;
import org.icpclive.webadmin.mainscreen.statistics.WordStatisticsData;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.util.*;

/**
 * Created by Aksenov239 on 26.03.2017.
 */
public class TwitterLoader extends Utils.StoppedRunnable {
    private static final Logger logger = LogManager.getLogger(TwitterLoader.class);

    private static Twitter twitter;

    private static TwitterLoader instance;

    private static PollsData pollsData;

    private String mainHashTag;
    private String pollHashTag;

    public static TwitterLoader getInstance() {
        return instance;
    }

    public static void start() {
        if (instance == null) {
            pollsData = PollsData.getInstance();

            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey("ufPvAsmjiMgdoqjMUNEzQ")
                    .setOAuthConsumerSecret("2FixWAc64f5m2R0KyN6okd1DWHeaa1qThgHbzLYzHM4")
                    .setOAuthAccessToken("450069411-sjzhMx4PZPp3CiZUnzISyTOgD0hmhgNpAv8cZeiR")
                    .setOAuthAccessTokenSecret("veodlw1zZkz1H4dikWRC7a2jyqAj87KahDcg7cGGZNjUd");
            TwitterFactory twitterFactory = new TwitterFactory(cb.build());
            twitter = twitterFactory.getInstance();
//            TwitterFactory twf = new TwitterFactory();
//            twitter = twf.getInstance();
//            twitter.setOAuthConsumer("ufPvAsmjiMgdoqjMUNEzQ", "2FixWAc64f5m2R0KyN6okd1DWHeaa1qThgHbzLYzHM4");
//            twitter = TwitterFactory.getSingleton();
            instance = new TwitterLoader();
            Utils.StoppedThread twitterThread = new Utils.StoppedThread(instance);
            twitterThread.start();
            ContextListener.addThread(twitterThread);
        }
    }

    private TwitterLoader() {
        try {
            Properties properties = Config.loadProperties("mainscreen");
//            properties.load(getClass().getResourceAsStream("/mainscreen.properties"));
            mainHashTag = properties.getProperty("twitter.hashtag");
            pollHashTag = properties.getProperty("poll.hashtag", mainHashTag);
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
        if (Arrays.stream(status.getHashtagEntities()).anyMatch(e -> ("#" + e.getText()).equals(mainHashTag))) {
            WordStatisticsData.vote(WordStatisticsData.TWEET_KEYWORD + " " + status.getText());
            MessageData.processTwitterMessage(status);
        }

//        System.err.println(status.getUser().getId() + " " + status.getText());
        if (status.getText().startsWith(pollHashTag + " ")) {
            String text = status.getText().substring(pollHashTag.length() + 1);
            if (text.startsWith("vote")) {
                PollsData.vote("Twitter:" + status.getUser().getId(), text);
            }
        }
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
                System.err.println(mainHashTag);
                filterQuery.track(mainHashTag, pollHashTag);

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

    public List<Status> loadByQuery(String query) throws TwitterException {
        List<Status> ret;
        if (query.startsWith("@")) {
            String username = query.substring(1);
            ret = twitter.getUserTimeline(username);
        } else {
            ret = twitter.search(new Query(query)).getTweets();
        }
        ret = ret.subList(0, Math.min(ret.size(), 7));
        Collections.reverse(ret);
        return ret;
    }
}
