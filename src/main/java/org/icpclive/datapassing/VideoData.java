package org.icpclive.datapassing;

import com.vaadin.data.util.BeanItemContainer;
import org.icpclive.webadmin.mainscreen.MainScreenData;
import org.icpclive.webadmin.mainscreen.picture.Picture;
import org.icpclive.webadmin.mainscreen.video.Video;

public class VideoData extends CachedData {
    public VideoData() {

    }

    public CachedData initialize() {
        VideoData data = MainScreenData.getMainScreenData().videoData;
        this.timestamp = data.timestamp;
        this.video = data.video;
        this.delay = data.delay;
        return this;
    }

    public Video video;

    public synchronized String setVisible(Video picture) {
        String error = checkOverlays();
        if (error != null) {
            return error;
        }
        if (this.video != null) {
            return "Please hide the previous picture first";
        }
        this.video = picture;
        this.timestamp = System.currentTimeMillis();
        switchOverlaysOff();
        recache();
        return null;
    }

    public synchronized void hide() {
        this.video = null;
        this.timestamp = System.currentTimeMillis();
        delay = 0;
        recache();
    }

    public synchronized boolean isVisible() {
        return video != null;
    }

    private void recache() {
        Data.cache.refresh(VideoData.class);
    }

    public void addVideo(Video video) {
        MainScreenData.getProperties().backupVideos.addItemAt(0, video);
    }

    public void removeVideo(Video video) {
        MainScreenData.getProperties().backupVideos.removeItem(video);
    }

    public BeanItemContainer<Video> getContainer() {
        return MainScreenData.getProperties().backupVideos.getContainer();
    }

    public synchronized void setNewCaption(Object video, String newCaption) {
        MainScreenData.getProperties().backupVideos.setProperty(video, "caption", newCaption);
    }

    public String checkOverlays() {
        MainScreenData mainScreenData = MainScreenData.getMainScreenData();
        if (mainScreenData.teamData.isVisible) {
            return mainScreenData.teamData.getOverlayError();
        }
        if (mainScreenData.pvpData.isVisible()) {
            return mainScreenData.pvpData.getOverlayError();
        }
        return null;
    }

    public void switchOverlaysOff() {
        MainScreenData mainScreenData = MainScreenData.getMainScreenData();
        boolean turnOff = false;
        if (mainScreenData.standingsData.isVisible &&
                mainScreenData.standingsData.isBig) {
            mainScreenData.standingsData.hide();
            turnOff = true;
        }
        if (mainScreenData.statisticsData.isVisible()) {
            mainScreenData.statisticsData.hide();
            turnOff = true;
        }
        if (mainScreenData.pollData.isVisible) {
            mainScreenData.pollData.hide();
            turnOff = true;
        }
        if (turnOff) {
            delay = MainScreenData.getProperties().overlayedDelay;
        } else {
            delay = 0;
        }
    }

    public String getOverlayError() {
        return "You need to hide video first";
    }

    public void update() {

    }

    public synchronized String toString() {
        return video == null ? "No video is shown" : "Video is now shown";
    }
}
