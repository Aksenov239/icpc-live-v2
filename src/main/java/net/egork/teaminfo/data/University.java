package net.egork.teaminfo.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author egor@egork.net
 */
public class University {
    private String fullName;
    private String shortName;
    private String region;
    private String hashTag;
    private String url;
    private String myIcpcId;
    private int appearances = -1;
    private List<Integer> appYears = new ArrayList<>();
    private int wins = -1;
    private List<Integer> winYears = new ArrayList<>();
    private int gold = -1;
    private List<Integer> goldYears = new ArrayList<>();
    private int silver = -1;
    private List<Integer> silverYears = new ArrayList<>();
    private int bronze = -1;
    private List<Integer> bronzeYears = new ArrayList<>();
    private int regionalChampionships = 0;
    private List<Integer> regYears = new ArrayList<>();

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getHashTag() {
        return hashTag;
    }

    public void setHashTag(String hashTag) {
        this.hashTag = hashTag;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getAppearances() {
        return appearances;
    }

    public void setAppearances(int appearances) {
        this.appearances = appearances;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getSilver() {
        return silver;
    }

    public void setSilver(int silver) {
        this.silver = silver;
    }

    public int getBronze() {
        return bronze;
    }

    public void setBronze(int bronze) {
        this.bronze = bronze;
    }

    public int getRegionalChampionships() {
        return regionalChampionships;
    }

    public void setRegionalChampionships(int regionalChampionships) {
        this.regionalChampionships = regionalChampionships;
    }

    public String getMyIcpcId() {
        return myIcpcId;
    }

    public void setMyIcpcId(String myIcpcId) {
        this.myIcpcId = myIcpcId;
    }

    public List<Integer> getAppYears() {
        return appYears;
    }

    public void setAppYears(List<Integer> appYears) {
        this.appYears = appYears;
    }

    public List<Integer> getWinYears() {
        return winYears;
    }

    public void setWinYears(List<Integer> winYears) {
        this.winYears = winYears;
    }

    public List<Integer> getGoldYears() {
        return goldYears;
    }

    public void setGoldYears(List<Integer> goldYears) {
        this.goldYears = goldYears;
    }

    public List<Integer> getSilverYears() {
        return silverYears;
    }

    public void setSilverYears(List<Integer> silverYears) {
        this.silverYears = silverYears;
    }

    public List<Integer> getBronzeYears() {
        return bronzeYears;
    }

    public void setBronzeYears(List<Integer> bronzeYears) {
        this.bronzeYears = bronzeYears;
    }

    public List<Integer> getRegYears() {
        return regYears;
    }

    public void setRegYears(List<Integer> regYears) {
        this.regYears = regYears;
    }

    public void updateFrom(University university) {
        if (university.fullName != null) {
            fullName = university.fullName;
        }
        if (university.shortName != null) {
            shortName = university.shortName;
        }
        if (university.region != null) {
            region = university.region;
        }
        if (university.hashTag != null) {
            hashTag = university.hashTag;
        }
        if (university.url != null) {
            url = university.url;
        }
        if (university.appearances != -1) {
            appearances = university.appearances;
        }
        if (university.wins != -1) {
            wins = university.wins;
        }
        if (university.gold != -1) {
            gold = university.gold;
        }
        if (university.silver != -1) {
            silver = university.silver;
        }
        if (university.bronze != -1) {
            bronze = university.bronze;
        }
        if (university.regionalChampionships != -1) {
            regionalChampionships = university.regionalChampionships;
        }
        if (university.myIcpcId != null) {
            myIcpcId = university.myIcpcId;
        }
        if (!university.getAppYears().isEmpty()) {
            setAppYears(university.getAppYears());
        }
        if (!university.getWinYears().isEmpty()) {
            setWinYears(university.getWinYears());
        }
        if (!university.getGoldYears().isEmpty()) {
            setGoldYears(university.getGoldYears());
        }
        if (!university.getSilverYears().isEmpty()) {
            setSilverYears(university.getSilverYears());
        }
        if (!university.getBronzeYears().isEmpty()) {
            setBronzeYears(university.getBronzeYears());
        }
        if (!university.getRegYears().isEmpty()) {
            setRegYears(university.getRegYears());
        }
    }
}
