package net.egork.teaminfo.data;

import net.egork.teaminfo.Utils;

import java.util.*;

/**
 * @author egor@egork.net
 */
public class Person {
    private String name;
    private List<String> altNames = new ArrayList<>();
    private String tcHandle;
    private String tcId;
    private int tcRating = -1;
    private String cfHandle;
    private int cfRating = -1;
    private String twitterHandle;
    private List<Achievement> achievements = new ArrayList<>();

    public String getName() {
        return name;
    }

    public List<String> getAltNames() {
        return Collections.unmodifiableList(altNames);
    }

    public String getTcHandle() {
        return tcHandle;
    }

    public int getTcRating() {
        return tcRating;
    }

    public String getCfHandle() {
        return cfHandle;
    }

    public int getCfRating() {
        return cfRating;
    }

    public List<Achievement> getAchievements() {
        return Collections.unmodifiableList(achievements);
    }

    public Person setName(String name) {
        this.name = name;
        if (name != null) {
            if (name.contains("Andrew")) {
                addAltName(name.replace("Andrew", "Andrey"));
            }
            if (name.contains("Alexander")) {
                addAltName(name.replace("Alexander", "Aleksandr"));
            }
            if (name.contains("Alexandr")) {
                addAltName(name.replace("Alexandr", "Aleksandr"));
            }
            if (name.contains("Michael")) {
                addAltName(name.replace("Michael", "Mikhail"));
            }
            if (name.contains("Andrei")) {
                addAltName(name.replace("Andrei", "Andrey"));
            }
            if (name.contains("Dmitrii")) {
                addAltName(name.replace("Dmitrii", "Dmitriy"));
            }
            if (name.contains("Dmitri")) {
                addAltName(name.replace("Dmitri", "Dmitriy"));
            }
            if (name.contains("Dmitry")) {
                addAltName(name.replace("Dmitry", "Dmitriy"));
            }
            if (name.contains("Yurii")) {
                addAltName(name.replace("Yurii", "Yury"));
            }
            if (name.contains("Yuri")) {
                addAltName(name.replace("Yuri", "Yury"));
            }
            String noUmlauts = Utils.replaceUmlauts(name);
            if (!noUmlauts.equals(name)) {
                addAltName(noUmlauts);
            }
        }
        return this;
    }

    public Person setTcHandle(String tcHandle) {
        this.tcHandle = tcHandle;
        return this;
    }

    public Person setTcRating(int tcRating) {
        this.tcRating = tcRating;
        return this;
    }

    public Person setCfHandle(String cfHandle) {
        this.cfHandle = cfHandle;
        return this;
    }

    public Person setCfRating(int cfRating) {
        this.cfRating = cfRating;
        return this;
    }

    public Person addAltName(String name) {
        if (name.equals(this.name) || altNames.contains(name)) {
            return this;
        }
        altNames.add(name);
        String noUmlauts = Utils.replaceUmlauts(name);
        if (!noUmlauts.equals(name)) {
            addAltName(noUmlauts);
        }
        return this;
    }

    public Person addAchievement(Achievement achievement) {
        achievements.add(achievement);
        Collections.sort(achievements);
        return this;
    }

    public Person compressAchievements() {
        Map<String, List<Achievement>> byType = new HashMap<>();
        for (Achievement achievement : achievements) {
            if (!byType.containsKey(achievement.achievement)) {
                byType.put(achievement.achievement, new ArrayList<>());
            }
            byType.get(achievement.achievement).add(achievement);
        }
        achievements = new ArrayList<>();
        for (Map.Entry<String, List<Achievement>> entry : byType.entrySet()) {
            List<String> years = new ArrayList<>();
            for (Achievement achievement : entry.getValue()) {
                years.add(Integer.toString(achievement.year));
            }
            Collections.sort(years);
            achievements.add(new Achievement(entry.getKey() + " (" + Utils.getYears(years) + ")", null, entry
                    .getValue().iterator().next().priority + years.size()));
        }
        Collections.sort(achievements);
        return this;
    }

    public String getTcId() {
        return tcId;
    }

    public void setTcId(String tcId) {
        this.tcId = tcId;
    }

    public String getTwitterHandle() {
        return twitterHandle;
    }

    public void setTwitterHandle(String twitterHandle) {
        this.twitterHandle = twitterHandle;
    }

    public boolean isCompatible(Person other) {
        return Utils.compatible(tcHandle, other.tcHandle) && (Utils.compatible(cfHandle, other.cfHandle) || cfHandle
                .equalsIgnoreCase(other.cfHandle)) && Utils.compatible(tcId, other.tcId);
    }

    public void updateFrom(Person other) {
        if (name == null || name.equals(other.name)) {
            name = other.name;
        } else {
            if (other.name != null && !altNames.contains(other.name)) {
                altNames.add(other.name);
            }
        }
        for (String altName : other.altNames) {
            addAltName(altName);
        }
        if (other.tcHandle != null) {
            tcHandle = other.tcHandle;
        }
        if (other.tcId != null) {
            tcId = other.tcId;
        }
        if (other.tcRating != -1) {
            tcRating = other.tcRating;
        }
        if (other.cfHandle != null) {
            cfHandle = other.cfHandle;
        }
        if (other.cfRating != -1) {
            cfRating = other.cfRating;
        }
        if (other.twitterHandle != null) {
            twitterHandle = other.twitterHandle;
        }
        achievements.addAll(other.achievements);
        Collections.sort(achievements);
    }

    public boolean isSamePerson(Person other) {
        if (name != null) {
            if (name.equals(other.name)) {
                return true;
            }
            for (String altName : other.altNames) {
                if (name.equalsIgnoreCase(altName)) {
                    return true;
                }
            }
        }
        for (String name : altNames) {
            if (name.equalsIgnoreCase(other.name)) {
                return true;
            }
            for (String altName : other.altNames) {
                if (name.equalsIgnoreCase(altName)) {
                    return true;
                }
            }
        }
        if (tcHandle != null && tcHandle.equals(other.tcHandle)) {
            return true;
        }
        if (tcId != null && tcId.equals(other.tcId)) {
            return true;
        }
        return cfHandle != null && cfHandle.equalsIgnoreCase(other.cfHandle);
    }
}
