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

    // Type is vote %poll% %option%
    public static void vote(String user, String message) {
        String[] tokens = message.split(" ");

        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].toLowerCase();
        }

        if (tokens.length == 0) {
            return;
        }

        if (!tokens[0].equals("vote") || tokens.length > 3) {
            return;
        }

        Poll pollToUpdate = pollsByHashtag.get(tokens[1].startsWith("#") ? tokens[1] : "#" + tokens[1]);
        if (pollToUpdate == null) {
            return;
        }
        System.err.println("Vote for " + message);
        pollToUpdate.updateIfOption(user, tokens[2].startsWith("#") ? tokens[2] : "#" + tokens[2]);
    }

}
