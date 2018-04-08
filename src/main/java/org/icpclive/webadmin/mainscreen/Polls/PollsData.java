package org.icpclive.webadmin.mainscreen.Polls;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.Config;
import org.icpclive.webadmin.backup.BackUp;

import java.io.IOException;
import java.util.Arrays;
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
            properties = Config.loadProperties("mainscreen");
//            properties.load(getClass().getResourceAsStream("/mainscreen.properties"));
            backUpFile = properties.getProperty("polls.backup.file");
        } catch (IOException e) {
            log.error("error", e);
        }

        pollList = new BackUp<>(Poll.class, backUpFile);
        pollsByHashtag = new ConcurrentHashMap<>();
        for (Poll poll : pollList.getData()) {
            pollsByHashtag.put(poll.getHashtag().toLowerCase(), poll);
        }
    }

    public void addPoll(Poll poll) {
        pollList.addItem(poll);
        pollsByHashtag.put(poll.getHashtag().toLowerCase(), poll);
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
        if (message.startsWith("vote ")) {
            message = message.substring("vote ".length());
        } else {
            return;
        }

        String[] tokens = message.toLowerCase().split(" ");

        if (tokens.length != 2) {
            return;
        }

        Poll pollToUpdate = pollsByHashtag.get(
                tokens[0].startsWith("#") ? tokens[0] : "#" + tokens[0]);
        
        if (pollToUpdate == null) {
            return;
        }
        System.err.println("Vote for " + message);
        pollToUpdate.updateIfOption(user, tokens[1].startsWith("#") ? tokens[1] : "#" + tokens[1]);
    }

}
