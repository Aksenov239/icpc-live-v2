package org.icpclive.webadmin.mainscreen.Polls;

import com.google.gson.*;
import org.icpclive.events.ContestInfo;
import org.icpclive.events.EventsLoader;

import java.lang.reflect.Type;
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

    public Option[] options; // Option to votes
    private HashSet<String> usersVoted;
    boolean teamOptions;
    int totalOptions = 0;

    // Take team hashtags
    public Poll(String question, String hashtag, boolean teamOptions) {
        this.question = question;
        this.hashtag = hashtag;
        usersVoted = new HashSet<>();
        if (teamOptions) {
            ContestInfo contestInfo = null;
            while (contestInfo == null) {
                contestInfo = EventsLoader.getInstance().getContestData();
            }
            this.teamOptions = true;
            String[] hashtags = contestInfo.getHashTags();
            Arrays.sort(hashtags);
            options = new Option[hashtags.length];
            for (int i = 0; i < hashtags.length; i++) {
                if (hashtags[i] != null) {
                    options[i] = new Option(totalOptions++, hashtags[i], 0);
                }
            }
        } else {
            options = new Option[0];
        }
    }

    public Poll(String question, String hashtag, String[] options) {
        this.question = question;
        this.hashtag = hashtag.toLowerCase();
        this.options = new Option[options.length];
        this.usersVoted = new HashSet<>();
        totalOptions = 0;
        for (int i = 0; i < options.length; i++) {
            if (!options[i].startsWith("#")) {
                options[i] = "#" + options[i];
            }
            this.options[i] = new Option(totalOptions++, options[i].toLowerCase(), 0);
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
        this.hashtag = hashtag.toLowerCase();
    }

    public void setOptions(String[] hashtags) {
        synchronized (this) {
            for (int i = 0; i < hashtags.length; i++) {
                if (!hashtags[i].startsWith("#")) {
                    hashtags[i] = "#" + hashtags[i];
                }
                hashtags[i] = hashtags[i].toLowerCase();
            }

            Option[] newOptions = new Option[hashtags.length];
            for (int i = 0; i < newOptions.length; i++) {
                int id = -1;
                for (int j = 0; j < options.length; j++) {
                    if (options[j].option.equals(hashtags[i])) {
                        id = j;
                        break;
                    }
                }
                if (id != -1) {
                    newOptions[i] = options[id];} else {
                    newOptions[i] = new Option(i, hashtags[i], 0);
                }
            }

            for (int i = 0; i < newOptions.length; i++) {
                newOptions[i].id = i;
            }

            options = newOptions;
            totalOptions = newOptions.length;
        }
    }

    public boolean updateIfOption(String user, String option) {
        synchronized (this) {
            option = option.toLowerCase();
            if (usersVoted.contains(user))
                return true;
            Option value = null;
            for (Option op : options) {
                if (op.option.equals(option)) {
                    value = op;
                    break;
                }
            }
            if (value != null) {
                value.votes++;
                usersVoted.add(user);
            }
            return value != null;
        }
    }

    public boolean getTeamOptions() {
        return teamOptions;
    }

    public void setTeamOptions(boolean teamOptions) {
        this.teamOptions = teamOptions;
    }

    public Option[] getData() {
        synchronized (this) {
            return Arrays.copyOf(options, options.length);
        }
    }

    public String toString() {
        return "Poll: " + question + " " + hashtag + " " + options;
    }

    public static class PollSerializer implements JsonSerializer<Poll> {
        @Override
        public JsonElement serialize(Poll poll, Type type, JsonSerializationContext jsonSerializationContext) {
            if (poll == null) {
                return null;
            }
            synchronized (poll) {
                final JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("question", poll.getQuestion());
                jsonObject.addProperty("hashtag", poll.getHashtag());
                jsonObject.addProperty("teamOptions", poll.getTeamOptions());
                JsonArray optionsArray = new JsonArray();
                for (Option options : poll.options) {
                    JsonObject jsonOption = new JsonObject();
                    jsonOption.addProperty("id", options.id);
                    jsonOption.addProperty("option", options.option);
                    jsonOption.addProperty("votes", options.votes);
                    optionsArray.add(jsonOption);
                }
                jsonObject.add("options", optionsArray);
                return jsonObject;
            }
        }
    }

    public static class PollDeserializer implements JsonDeserializer<Poll> {
        @Override
        public Poll deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContest) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String question = jsonObject.get("question").getAsString();
            String hashtag = jsonObject.get("hashtag").getAsString();
            boolean teamOptions = jsonObject.get("teamOptions").getAsBoolean();

            JsonArray optionsArray = jsonObject.get("options").getAsJsonArray();
            Option[] options = new Option[optionsArray.size()];
            for (int i = 0; i < options.length; i++) {
                JsonObject jo = optionsArray.get(i).getAsJsonObject();
                options[i] = new Option(
                        jo.get("id").getAsInt(),
                        jo.get("option").getAsString(),
                        jo.get("votes").getAsInt()
                );
            }
            Poll poll = new Poll(question, hashtag, teamOptions);
            poll.options = options;
            return poll;
        }
    }
}
