package org.icpclive.webadmin.mainscreen.picture;

/**
 * Created by Meepo on 11/29/2018.
 */
public class Picture {
    private String caption;
    private String path;

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Picture(String caption, String path) {
        this.caption = caption;
        this.path = path;
    }

}
