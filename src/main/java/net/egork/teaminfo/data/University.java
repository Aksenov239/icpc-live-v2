package net.egork.teaminfo.data;

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
    private int wins = -1;
    private int gold = -1;
    private int silver = -1;
    private int bronze = -1;
    private int regionalChampionships = 0;

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
    }
}
