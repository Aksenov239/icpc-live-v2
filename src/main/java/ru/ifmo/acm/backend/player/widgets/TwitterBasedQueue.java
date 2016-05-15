package ru.ifmo.acm.backend.player.widgets;

import org.apache.logging.log4j.*;
import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.TeamInfo;
import twitter4j.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Aksenov239 on 28.03.2016.
 */
public class TwitterBasedQueue extends Thread {
    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(TwitterBasedQueue.class);

    private Twitter twitter;
    private String mainHashTag;
    private LinkedBlockingQueue<Request> queue;
    private ConcurrentSkipListSet<String> inQueueHashtags;
    private long sleepTime;
    private ContestInfo contestInfo;
    private long accountWaitTime;
    private int votesToShow;

    public TwitterBasedQueue() {
        Properties properties = new Properties();
        twitter = TwitterFactory.getSingleton();
        try {
            properties.load(getClass().getResourceAsStream("/splitscreen.properties"));
            mainHashTag = properties.getProperty("main.hashtag");
            sleepTime = Long.parseLong(properties.getProperty("hashtag.loader.sleep.time", "60000"));
            accountWaitTime = Long.parseLong(properties.getProperty("account.wait.time"));
            votesToShow = Integer.parseInt(properties.getProperty("votes.to.show"));
            queue = new LinkedBlockingQueue<>();
            inQueueHashtags = new ConcurrentSkipListSet<>();
            contestInfo = Preparation.eventsLoader.getContestData();
        } catch (IOException e) {
            log.error("error", e);
        }
    }

    public Request nextRequest() {
        Request request = queue.poll();
        if (request != null) {
            inQueueHashtags.remove(request.hashTag);
        }
        return request;
    }

    public void run() {
        boolean firstRun = true;
        long lastId = 0;
        HashMap<Long, Long> lastTweetFromUser = new HashMap<>();
        HashMap<Request, Integer> votesForTeam = new HashMap<>();
        while (true) {
            Query query = new Query(mainHashTag);
            query.setCount(100);
            query.setSinceId(lastId);
            List<Status> list = null;
            try {
                list = twitter.search(query).getTweets();
            } catch (TwitterException e) {
                twitter = TwitterFactory.getSingleton();
                log.error("error", e);
                continue;
            }
            for (Status status : list) {
                long timestamp = status.getCreatedAt().getTime();
                if (lastTweetFromUser.getOrDefault(status.getUser().getId(), 0L) + accountWaitTime > timestamp) {
                    continue;
                }
                lastId = Math.max(status.getId(), lastId);
                for (HashtagEntity entity : status.getHashtagEntities()) {
                    if (firstRun)
                        continue;
                    String hashTag = entity.getText();
                    TeamInfo teamInfo = contestInfo.getParticipantByHashTag(hashTag);
                    if (teamInfo != null && !inQueueHashtags.contains(hashTag)) {
                        Request request = new Request(teamInfo.getId(),
                                status.getText().contains("camera") ? "camera" : "screen",
                                hashTag
                        );
                        int total = votesForTeam.getOrDefault(request, 0) + 1;
                        if (total == votesToShow) {
                            queue.add(request);
                            inQueueHashtags.add(teamInfo.getHashTag());
                            total = 0;
                        }
                        votesForTeam.put(request, total);
                        break;
                    }
                }
                lastTweetFromUser.put(status.getUser().getId(), timestamp);
            }
            try {
                Thread.sleep(sleepTime);
            } catch (Exception e) {
                log.error("error", e);
            }
            firstRun = false;
        }
    }

    public class Request {
        int teamId;
        String type;
        String hashTag;

        public Request(int teamId, String type, String hashTag) {
            this.teamId = teamId;
            this.type = type;
            this.hashTag = hashTag;
        }

        public int hashCode() {
            return type.hashCode() * 200 + teamId;
        }
    }
}
