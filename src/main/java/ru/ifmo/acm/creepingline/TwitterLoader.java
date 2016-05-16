package ru.ifmo.acm.creepingline;

import com.vaadin.ui.Notification;
import org.apache.logging.log4j.*;
import org.apache.logging.log4j.Logger;
import ru.ifmo.acm.mainscreen.Utils;
import twitter4j.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

public class TwitterLoader extends Utils.StoppedRunnable {
    private static final Logger log = LogManager.getLogger(TwitterLoader.class);

    TwitterStream twitterUserStream, twitterKeywordStream;
    String twitterQueryString = "";

    public static TwitterLoader getInstance() {
        if (instance == null) {
            instance = new TwitterLoader();
        }
        return instance;
    }


    public String getTwitterQueryString() {
        return twitterQueryString;
    }

    public void setTwitterQueryString(String twitterQueryString) {
        this.twitterQueryString = twitterQueryString;
    }

    private TwitterLoader() {
        Properties properties = new Properties();
        twitter = new TwitterFactory().getSingleton();
        try {
            properties.load(getClass().getResourceAsStream("/creepingline.properties"));
            account = properties.getProperty("twitter.account");
            duration = Long.parseLong(properties.getProperty("twitter.message.duration"));
            List<Status> updates = twitter.getUserTimeline(account);
            final Consumer<Status> tweetConsumer = tweet -> {
                Message message = new Message(tweet.getText(), System.currentTimeMillis(), duration, false, "@" + tweet.getUser().getScreenName());
                MessageData.getMessageData().addMessageToFlow(message);
            };
//            updates.forEach(tweetConsumer);
            twitterUserStream = new TwitterStreamFactory().getInstance();
//            twitterKeywordStream = new TwitterStreamFactory().getInstance();
            StatusAdapter tweetsListener = new StatusAdapter() {
                @Override
                public void onStatus(Status status) {
                    System.out.println("@" + status.getUser().getScreenName() + ": " + status.getText());
                    tweetConsumer.accept(status);
                }
            };
            twitterUserStream.addListener(tweetsListener);
//            twitterKeywordStream.addListener(tweetsListener);
            lastId = -1;
        } catch (IOException | TwitterException e) {
            log.error("Twitter init failure", e);
        }
    }

    public static void changeStreamInInstance(String query) {
        instance.changeStream(query);
    }

    public synchronized void changeStream(String query) {
        twitterQueryString = query;
        twitterUserStream.cleanUp();
//        twitterKeywordStream.cleanUp();
        System.out.println("query: " + query);
        String[] queries = query.split(",");
//        List<String> users = new ArrayList<>();
        List<String> keywords = new ArrayList<>();
        List<Long> userIDs = new ArrayList<>();
        for (String e : queries) {
            if (e.isEmpty()) continue;
            if (e.startsWith("@")) {
                try {
                    User user = twitter.showUser(e.substring(1));
                    userIDs.add(user.getId());
                } catch (TwitterException e1) {
                    e1.printStackTrace();
                }
            } else {
                keywords.add(e);
            }
        }
        System.out.println("users: " + userIDs);
        System.out.println("keywords: " + keywords);
        long[] f = new long[userIDs.size()];
        for (int i = 0; i < f.length; i++) {
            f[i] = userIDs.get(i);
        }
        twitterUserStream.filter(new FilterQuery(0, f, keywords.toArray(new String[keywords.size()])));
    }

    private List<Status> getUpdates() throws TwitterException {
        List<Status> updates = (lastId < 0) ? twitter.getUserTimeline(account) : twitter.getUserTimeline(account, new Paging(lastId));

        if (updates.size() > 0){
            lastId = updates.get(0).getId();
        }
        return updates;
    }

    void postMessage(String message) {
        try {
            Status status = twitter.updateStatus(message);
        } catch (TwitterException e) {
            log.error("Twitter update failure", e);
        }
    }

    @Override
    public void run() {
        while (!stop) {
            if (true) break;
            try {
                List<Status> updates = getUpdates();
                updates.forEach(tweet -> {
                    Message message = new Message(tweet.getText(), System.currentTimeMillis(), duration, false, "@" + tweet.getUser().getScreenName());
                    MessageData.getMessageData().addMessageToFlow(message);
                });
                Thread.sleep(20000);
            } catch (InterruptedException | TwitterException e) {
                log.error("Twitter get updates failure", e);
            }
        }
    }

    private String account;
    private long lastId;
    private final Twitter twitter;
    private long duration;
    private static TwitterLoader instance;

    public void addSearch(String query) {
        if (query.startsWith("@")) {
            String username = query.substring(1);
            try {
                List<Status> statuses = twitter.getUserTimeline(username);
//                Collections.reverse(statuses);
                for (int i = 4; i >= 0; i--) {
                    if (i >= statuses.size()) continue;
                    Status e = statuses.get(i);
                    MessageData.getMessageData().addMessageToFlow(new Message(e.getText(), System.currentTimeMillis(), duration, false, "@" + e.getUser().getScreenName()));
                }
            } catch (TwitterException e) {
                Notification.show("TwitterException: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            try {
                List<Status> statuses = twitter.search(new Query(query)).getTweets();
//                Collections.reverse(statuses);
                for (int i = 4; i >= 0; i--) {
                    if (i >= statuses.size()) continue;
                    Status e = statuses.get(i);
                    MessageData.getMessageData().addMessageToFlow(new Message(e.getText(), System.currentTimeMillis(), duration, false, "@" + e.getUser().getScreenName()));
                }
            } catch (TwitterException e) {
                Notification.show("TwitterException: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
