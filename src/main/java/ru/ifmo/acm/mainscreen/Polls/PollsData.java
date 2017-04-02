package ru.ifmo.acm.mainscreen.Polls;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.acm.backup.BackUp;
import ru.ifmo.acm.events.EventsLoader;
import ru.ifmo.acm.events.TeamInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Aksenov239 on 12.03.2017.
 */
public class PollsData {
    private static Logger log = LogManager.getLogger(PollsData.class);

    private static PollsData pollsData;

    public static PollsData getInstance() {
        if (pollsData == null) {
            pollsData = new PollsData();
        }
        return pollsData;
    }

    public final BackUp<Poll> pollList;
    private String backUpFile;
    private static ConcurrentHashMap<String, Poll> pollsByHashtag;
    public final String[] teamHashtags;

    public PollsData() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/mainscreen.properties"));
            backUpFile = properties.getProperty("polls.backup.file");
        } catch (IOException e) {
            log.error("error", e);
        }

        pollList = new BackUp<>(Poll.class, backUpFile);
        pollsByHashtag = new ConcurrentHashMap<>();
        for (Poll poll : pollList.getData()) {
            pollsByHashtag.put(poll.getHashtag(), poll);
        }

        ArrayList<String> hashtags = new ArrayList<>();
        for (TeamInfo teamInfo : EventsLoader.getInstance().getContestData().getStandings()) {
            hashtags.add(teamInfo.getHashTag());
        }
        teamHashtags = hashtags.toArray(new String[0]);
    }

    public void addPoll(Poll poll) {
        pollList.addItem(poll);
        pollsByHashtag.put(poll.getHashtag(), poll);
    }

    public void removePoll(Poll poll) {
        pollList.removeItem(poll);
        pollsByHashtag.remove(poll.getHashtag(), poll);
    }

    public void updateHashtag(Poll poll, String hashtag) {
        pollsByHashtag.remove(poll.getHashtag());
        pollsByHashtag.put(hashtag, poll);
    }

    public static void vote(String user, String message) {
        String[] tokens = message.split(" ");
        Poll pollToUpdate = null;
        for (int i = 0; i < tokens.length; i++) {
            if (pollsByHashtag.containsKey(tokens[i])) {
                pollToUpdate = pollsByHashtag.get(tokens[i]);
                break;
            }
        }
        if (pollToUpdate == null) {
            return;
        }
        for (int i = 0; i < tokens.length; i++) {
            if (pollToUpdate.updateIfOption(user, tokens[i])) {
                return;
            }
        }
    }

}
