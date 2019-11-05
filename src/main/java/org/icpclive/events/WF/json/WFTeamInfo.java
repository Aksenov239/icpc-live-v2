package org.icpclive.events.WF.json;

/**
 * Created by Meepo on  4/1/2018.
 */
public class WFTeamInfo extends org.icpclive.events.WF.WFTeamInfo {
    public String cdsId;

    // videos url
    public String photo;
    public String video;
    public String screen;
    public String camera;

    public String organization;

    public WFTeamInfo(int problems) {
        super(problems);
    }

    public String getAlias() {
        return cdsId;
    }

    public boolean isTemplated(String type) {
        switch (type) {
            case "screen":
                return false;
            case "camera":
                return false;
        }
        return true;
    }

    public String getUrlByType(String type) {
        switch (type) {
            case "screen":
                return screen;
            case "camera":
                return camera;
            case "video":
                return cdsId;
            default:
                return "";
        }
    }

    public String getOrganization() {
        return organization;
    }

    public String toString() {
        return cdsId + ". " + shortName;
    }
}
