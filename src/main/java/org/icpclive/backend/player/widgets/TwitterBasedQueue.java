package org.icpclive.backend.player.widgets;

import org.apache.logging.log4j.LogManager;
import org.icpclive.Config;
import org.icpclive.backend.Preparation;
import org.icpclive.events.ContestInfo;
import org.icpclive.events.TeamInfo;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import twitter4j.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Aksenov239 on 28.03.2016.
 */
public class TwitterBasedQueue extends Thread {
    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(TwitterBasedQueue.class);

    private Twitter twitter;
    private PircBotX twitch;
    private String twitchChannel;
    private String mainHashTag;
    private Queue<Request> queue;
    private Set<String> inQueueHashtags;
    private ContestInfo contestInfo;
    private long accountWaitTime;
    private int votesToShow;

    private TwitterBasedQueue() {
        Properties properties = new Properties();
        twitter = TwitterFactory.getSingleton();
        try {
//            properties.load(getClass().getResourceAsStream("/splitscreen.properties"));
            properties = Config.loadProperties("splitscreen");
            mainHashTag = properties.getProperty("main.hashtag");
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

    public synchronized int currentThreshold() {
        return Math.max(votesToShow, queue.size() / 2 + 1);
    }

    public synchronized Request nextRequest() {
        Request request = queue.poll();
        if (request != null) {
            inQueueHashtags.remove(request.hashTag);
        }

        int threshold = currentThreshold();
        for (Request request1 : votesForTeam.keySet()) {
            if (votesForTeam.get(request1) >= threshold) {
                enqueue(request1);
                votesForTeam.put(request1, 0);
            }
        }
        return request;
    }

    Map<String, Long> lastVoteFromUser = new HashMap<>();
    Map<Request, Integer> votesForTeam = new HashMap<>();

    TwitterStream twitterStream;

    public void run() {
        while (true) {
            try {
                twitterStream = new TwitterStreamFactory().getInstance();
                step();
                break;
            } catch (Throwable e) {
                twitter = null;
                log.error("error", e);
            }
        }
        startTwitchBot();
    }
//sp

    private synchronized void operate(String user, String text) {
        System.err.println(user + " " + text);

        long timestamp = System.currentTimeMillis();
        if (lastVoteFromUser.getOrDefault(user, 0L) + accountWaitTime > timestamp) {
            return;
        }

        lastVoteFromUser.put(user, timestamp);

        String[] parts = text.trim().split(" ");
        if (parts.length > 2) {
            return;
        }
        String type = "screen";
        if (parts.length == 2) {
            if (parts[0].equals("camera")) {
                type = "camera";
            } else {
                if (!parts[0].equals("screen")) {
                    return;
                }
            }
        }

        String hashTag = parts[parts.length - 1].toLowerCase();
        TeamInfo teamInfo = contestInfo.getParticipantByHashTag(hashTag);
        System.err.println(teamInfo);
        if (teamInfo != null && !inQueueHashtags.contains(hashTag)) {
            Request request = new Request(teamInfo.getId(),
                    type,
                    hashTag);
            System.err.println("Read request \"" + request.type + "\" for team " + request.teamId);
            int total = votesForTeam.getOrDefault(request, 0) + 1;
            System.err.println("It gets " + total + " votes.");

            if (total == currentThreshold()) {
                System.err.println(request.teamId + " is in the queue.");
                enqueue(request);
                total = 0;
            }
            votesForTeam.put(request, total);

        }
    }

    private void step() {
        StatusListener statusListener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                String text = status.getText();
                int prefixLen = (mainHashTag + " show ").length();
                if (text.startsWith(mainHashTag + " show ")) {
                    operate("Twitter:" + status.getUser().getScreenName(),
                            text.substring(prefixLen));
                }
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

    private void startTwitchBot() {
        Properties properties;
        String url = null;
        String username = null;
        String password = null;
        try {
//            properties.load(getClass().getResourceAsStream("/mainscreen.properties"));
            properties = Config.loadProperties("mainscreen");
            url = properties.getProperty("twitch.chat.server", "irc.chat.twitch.tv");
            username = properties.getProperty("twitch.chat.username");
            password = properties.getProperty("twitch.chat.password");
            twitchChannel = properties.getProperty("twitch.chat.channel", "#" + username);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Configuration.Builder configuration = new Configuration.Builder()
                .setAutoNickChange(false)
                .setOnJoinWhoEnabled(false)
                .setCapEnabled(true)
                .setName(username)
                .setServerPassword(password)
                .addServer(url)
                .addListener(new ListenerAdapter() {
                    AtomicLong lastTimestamp = new AtomicLong();
                    @Override
                    public void onMessage(MessageEvent event) throws Exception {
                        long previousTimestamp = lastTimestamp.get();
                        if (event.getTimestamp() > previousTimestamp + 50) {
                            lastTimestamp.compareAndSet(previousTimestamp, event.getTimestamp());
                            String text = event.getMessage();
                            int prefixLen = "!show ".length();
                            if (text.startsWith("!show ")) {
                                operate("Twitch:" + event.getUser().getLogin(),
                                        text.substring(prefixLen));
                            }
                        }
                    }
                })
                .setEncoding(Charset.forName("UTF-8"));

        twitch = new PircBotX(configuration.addAutoJoinChannel(twitchChannel).buildConfiguration());
        while (true) {
            try {
                System.err.println("Bot is started!");
                twitch.startBot();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void enqueue(Request request) {
//        twitch.send().message("#icpclive2", "hui");
//        twitch.sendRaw().rawLine("Fuck1");
//        twitch.sendRaw().rawLineNow("Fuck2");
        twitch.sendIRC().message(twitchChannel, "Added to automatic queue " + request.type
                + " for team " + contestInfo.getParticipant(request.teamId).getName()
                + ". See at http://icpclive.com. " + mainHashTag);
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

        public boolean equals(Object o) {
            Request request = (Request) o;
            return request.teamId == teamId && request.type.equals(type);
        }
    }
}