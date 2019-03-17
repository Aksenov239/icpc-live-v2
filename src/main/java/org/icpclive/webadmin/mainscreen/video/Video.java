package org.icpclive.webadmin.mainscreen.video;

public class Video {
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

    public Video(String caption, String path) {
        this.caption = caption;
        this.path = path;
    }
}
