package ru.ifmo.acm.mainscreen.Polls;

import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.EventsLoader;

import java.util.*;

/**
 * Created by Aksenov239 on 12.03.2017.
 */
public class Poll {
    public static class Option implements Comparable<Option> {
        public int id;
        public String option;
        public int votes;

        public Option(int id, String option, int votes) {
            this.option = option;
            this.votes = votes;
            this.id = id;
        }

        public int compareTo(Option option) {
            return votes == option.votes ? this.option.compareTo(option.option) : this.votes - option.votes;
        }

        public int hashCode() {
            return option.hashCode();
        }
    }

    private String question;
    private String hashtag;

    public TreeMap<String, Option> options; // Option to votes
    private HashSet<String> usersVoted;
    boolean teamOptions;
    int totalOptions = 0;

    // Take team hashtags
    public Poll(String question, String hashtag, boolean teamOptions) {
        this.question = question;
        this.hashtag = hashtag;
        options = new TreeMap<>();
        usersVoted = new HashSet<>();
        if (teamOptions) {
            ContestInfo contestInfo = null;
            while (contestInfo == null) {
                contestInfo = EventsLoader.getInstance().getContestData();
            }
            this.teamOptions = true;
            for (String option : contestInfo.getHashTags()) {
                this.options.put(option, new Option(totalOptions++, option, 0));
            }
        }
    }

    public Poll(String question, String hashtag, String[] options) {
        this.question = question;
        this.hashtag = hashtag;
        this.options = new TreeMap<>();
        this.usersVoted = new HashSet<>();
        totalOptions = 0;
        for (String option : options) {
            this.options.put(option, new Option(totalOptions++, option, 0));
        }
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getHashtag() {
        return hashtag;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    public synchronized void setOptions(String[] hashtags) {
        HashSet<String> currentOptions = new HashSet<>();
        for (String hashtag : hashtags) {
            currentOptions.add(hashtag);
            if (!options.containsKey(hashtag)) {
                options.put(hashtag, new Option(totalOptions++, hashtag, 0));
            }
        }
        HashSet<String> toRemove = new HashSet<>();
        for (String hashtag : options.keySet()) {
            if (!currentOptions.contains(hashtag)) {
                toRemove.add(hashtag);
            }
        }
        for (String remove : toRemove) {
            options.remove(remove);
        }
        usersVoted.clear();
    }

    public synchronized boolean updateIfOption(String user, String option) {
        if (usersVoted.contains(user))
            return true;
        Option value = options.get(option);
        if (value != null) {
            value.votes++;
            usersVoted.add(user);
        }
        return value != null;
    }

    public boolean getTeamOptions() {
        return teamOptions;
    }

    public void setTeamOptions(boolean teamOptions) {
        this.teamOptions = teamOptions;
    }

    public synchronized Option[] getData() {
        Option[] result = new Option[options.size()];
        int id = 0;
        for (Option option : options.values()) {
            result[id++] = option;
        }
        return result;
    }

    public String toString() {
        return "Poll: " + question + " " + hashtag + " " + options;
    }
}
