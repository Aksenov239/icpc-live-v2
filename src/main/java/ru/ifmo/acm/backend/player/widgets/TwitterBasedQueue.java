package ru.ifmo.acm.backend.player.widgets;

import org.apache.logging.log4j.LogManager;
import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.TeamInfo;
import twitter4j.*;

import java.io.IOException;
import java.util.*;

/**
 * Created by Aksenov239 on 28.03.2016.
 */
public class TwitterBasedQueue extends Thread {
    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(TwitterBasedQueue.class);

    private Twitter twitter;
    private String mainHashTag;
    private Queue<Request> queue;
    private Set<String> inQueueHashtags;
    private long sleepTime;
    private ContestInfo contestInfo;
    private long accountWaitTime;
    private int votesToShow;

    private TwitterBasedQueue() {
        Properties properties = new Properties();
        twitter = TwitterFactory.getSingleton();
        try {
            properties.load(getClass().getResourceAsStream("/splitscreen.properties"));
            mainHashTag = properties.getProperty("main.hashtag");
            sleepTime = Long.parseLong(properties.getProperty("hashtag.loader.sleep.time", "60000"));
            accountWaitTime = Long.parseLong(properties.getProperty("account.wait.time"));
            votesToShow = Integer.parseInt(properties.getProperty("votes.to.show"));
            queue = new ArrayDeque<>();
            inQueueHashtags = new HashSet<>();
            contestInfo = Preparation.eventsLoader.getContestData();
        } catch (IOException e) {
            log.error("error", e);
        }
    }

    private static TwitterBasedQueue tbq;

    public static synchronized TwitterBasedQueue getInstance() {
        if (tbq == null) {
            tbq = new TwitterBasedQueue();
            tbq.start();
        }
        return tbq;
    }

    public int currentThreshold() {
        return Math.min(votesToShow, queue.size() / 2 + 1);
    }

    public synchronized Request nextRequest() {
        Request request = queue.poll();
        if (request != null) {
            inQueueHashtags.remove(request.hashTag);
        }

        int threshold = currentThreshold();
        for (Request request1 : votesForTeam.keySet()) {
            if (votesForTeam.getOrDefault(request1, 0) >= threshold) {
                enqueue(request1);
                votesForTeam.put(request1, 0);
            }
        }
        return request;
    }

    long lastId = 0;
    Map<Long, Long> lastTweetFromUser = new HashMap<>();
    Map<Request, Integer> votesForTeam = new HashMap<>();
    Map<Request, Long> lastRequest = new HashMap<>();

    public void run() {
        while (true) {
            try {
                step();
            } catch (Throwable e) {
                twitter = null;
                log.error("error", e);
                continue;
            }
            try {
                Thread.sleep(sleepTime);
            } catch (Exception e) {
                log.error("error", e);
            }
        }
    }

//    private void step() throws TwitterException {
//        query = new Query(mainHashTag);
//        query.setCount(100);
//        query.setSinceId(lastId);
//        if (twitter == null) {
//            twitter = TwitterFactory.getSingleton();
//        }
//        list = twitter.search(query).getTweets();
//        for (Status status : list) {
//            long timestamp = status.getCreatedAt().getTime();
//            if (lastTweetFromUser.getOrDefault(status.getUser().getId(), 0L) + accountWaitTime > timestamp) {
//                continue;
//            }
//            lastId = Math.max(status.getId(), lastId);
//            for (HashtagEntity entity : status.getHashtagEntities()) {
//                if (firstRun)
//                    continue;
//                String hashTag = entity.getText();
//                TeamInfo teamInfo = contestInfo.getParticipantByHashTag(hashTag);
//                if (teamInfo != null && !inQueueHashtags.contains(hashTag)) {
//                    Request request = new Request(teamInfo.getId(),
//                            status.getText().contains("camera") ? "camera" : "screen",
//                            hashTag
//                    );
//                    System.err.println("Read request for team " + request.teamId + " " + request.type);
//                    int total = votesForTeam.getOrDefault(request, 0) + 1;
//                    lastRequest.put(request, status.getId());
//                    if (total == currentThreshold()) {
//                        enqueue(request);
//                        total = 0;
//                    }
//                    votesForTeam.put(request, total);
//                    break;
//                }
//            }
//            lastTweetFromUser.put(status.getUser().getId(), timestamp);
//        }
//        firstRun = false;
//    }

    private void step() {
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();

        StatusListener statusListener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                long timestamp = status.getCreatedAt().getTime();
                if (lastTweetFromUser.getOrDefault(status.getUser().getId(), 0L) + accountWaitTime > timestamp) {
                    return;
                }
                lastId = Math.max(status.getId(), lastId);
                for (HashtagEntity entity : status.getHashtagEntities()) {
                    String hashTag = entity.getText();
                    TeamInfo teamInfo = contestInfo.getParticipantByHashTag(hashTag);
                    if (teamInfo != null && !inQueueHashtags.contains(hashTag)) {
                        Request request = new Request(teamInfo.getId(),
                                status.getText().contains("camera") ? "camera" : "screen",
                                hashTag
                        );
                        System.err.println("Read request for team " + request.teamId + " " + request.type);
                        int total = votesForTeam.getOrDefault(request, 0) + 1;
                        lastRequest.put(request, status.getId());
                        if (total == currentThreshold()) {
                            enqueue(request);
                            total = 0;
                        }
                        votesForTeam.put(request, total);
                        break;
                    }
                }
                lastTweetFromUser.put(status.getUser().getId(), timestamp);
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            }

            @Override
            public void onTrackLimitationNotice(int i) {
            }

            @Override
            public void onScrubGeo(long l, long l1) {
            }

            @Override
            public void onStallWarning(StallWarning stallWarning) {
            }

            @Override
            public void onException(Exception e) {
            }
        };

        FilterQuery fq = new FilterQuery();
        String keywords[] = {mainHashTag};
        fq.track(keywords);

        twitterStream.addListener(statusListener);
        twitterStream.filter(fq);
    }

    private synchronized void enqueue(Request request) {
        try {
            String statusText =
                    "Added to automatic queue " + request.type + " for team " + contestInfo.getParticipant(request.teamId).getName()
                    + ". See at http://icpclive.com/2. #ICPC2016";
            StatusUpdate status = new StatusUpdate(statusText);
            status.setInReplyToStatusId(lastRequest.get(request));
            twitter.updateStatus(status);

            System.err.println(statusText);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        queue.add(request);
        inQueueHashtags.add(request.hashTag);
    }

    public class Request {
        final int teamId;
        final String type;
        final String hashTag;

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