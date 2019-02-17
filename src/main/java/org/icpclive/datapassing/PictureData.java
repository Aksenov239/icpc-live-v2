package org.icpclive.datapassing;

import com.vaadin.data.util.BeanItemContainer;
import org.icpclive.webadmin.mainscreen.MainScreenData;
import org.icpclive.webadmin.mainscreen.picture.Picture;

/**
 * Created by Meepo on 11/29/2018.
 */
public class PictureData extends CachedData {
    public PictureData() {

    }

    public CachedData initialize() {
        PictureData data = MainScreenData.getMainScreenData().pictureData;
        this.timestamp = data.timestamp;
        this.picture = data.picture;
        this.delay = data.delay;
        return this;
    }

    public Picture picture;

    public synchronized String setVisible(Picture picture) {
        String error = checkOverlays();
        if (error != null) {
            return error;
        }
        if (this.picture != null) {
            return "Please hide the previous picture first";
        }
        this.picture = picture;
        this.timestamp = System.currentTimeMillis();
        switchOverlaysOff();
        recache();
        return null;
    }

    public synchronized void hide() {
        this.picture = null;
        this.timestamp = System.currentTimeMillis();
        delay = 0;
        recache();
    }

    public synchronized boolean isVisible() {
        return picture != null;
    }

    private void recache() {
        Data.cache.refresh(PictureData.class);
    }

    public void addPicture(Picture picture) {
        MainScreenData.getProperties().backupPictures.addItem(picture);
    }

    public void removePicture(Picture picture) {
        MainScreenData.getProperties().backupPictures.removeItem(picture);
    }

    public BeanItemContainer<Picture> getContainer() {
        return MainScreenData.getProperties().backupPictures.getContainer();
    }

    public synchronized void setNewCaption(Object picture, String newCaption) {
        MainScreenData.getProperties().backupPictures.setProperty(picture, "caption", newCaption);
    }

    public String checkOverlays() {
        MainScreenData mainScreenData = MainScreenData.getMainScreenData();
        if (mainScreenData.teamData.isVisible) {
            return mainScreenData.teamData.getOverlayError();
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
        return "You need to hide picture first";
    }

    public void update() {

    }

    public synchronized String toString() {
        return picture != null ? "The analytics picture is now showing" : "No analytics picture now";
    }
}
