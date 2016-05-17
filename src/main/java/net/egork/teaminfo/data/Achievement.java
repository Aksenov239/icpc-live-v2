package net.egork.teaminfo.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author egor@egork.net
 */
public class Achievement implements Comparable<Achievement> {
    public final String achievement;
    public final Integer year;
    public final int priority;

    @JsonCreator
    public Achievement(@JsonProperty("achievement") String achievement,
            @JsonProperty("year") Integer year,
            @JsonProperty("priority") int priority) {
        this.achievement = achievement;
        this.year = year;
        this.priority = priority;
    }

    @Override
    public int compareTo(Achievement o) {
        return o.priority - priority;
    }
}
