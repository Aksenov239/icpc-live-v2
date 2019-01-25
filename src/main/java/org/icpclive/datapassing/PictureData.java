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
        this.picture = data.picture;
        return this;
    }

    public Picture picture;

    public synchronized void setVisible(Picture picture) {
        this.picture = picture;
        this.timestamp = System.currentTimeMillis();
        recache();
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

    public void update() {

    }

    public synchronized String toString() {
        return picture != null ? "The analytics picture is now showing" : "No analytics picture now";
    }
}
