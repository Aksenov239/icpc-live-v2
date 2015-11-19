package ru.ifmo.acm.mainscreen.statuses;

import com.vaadin.data.util.BeanItemContainer;
import ru.ifmo.acm.backup.BackUp;
import ru.ifmo.acm.mainscreen.Advertisement;

public class AdvertisementStatus {
    AdvertisementStatus(String backupFilename) {
        this.backupFilename = backupFilename;
        advertisements = new BackUp<>(Advertisement.class, backupFilename);
    }

    public synchronized void setAdvertisementVisible(boolean visible, Advertisement advertisement){
        synchronized (advertisementLock) {
            advertisementTimestamp = System.currentTimeMillis();
            isAdvertisementVisible = visible;
            advertisementValue = advertisement;
        }
    }

    public synchronized String advertisementStatus(){
        synchronized (advertisementLock) {
            return advertisementTimestamp + "\n" + isAdvertisementVisible + "\n" + advertisementValue;
        }
    }

    public void addAdvertisement(Advertisement advertisement) {
//        synchronized (advertisements) {
//            advertisements.addBean(advertisement);
//        }
        advertisements.addItem(advertisement);
    }

    public void removeAdvertisement(Advertisement advertisement) {
//        synchronized (advertisements) {
//            advertisements.removeItem(advertisement);
//        }
        advertisements.removeItem(advertisement);
    }

    public BeanItemContainer<Advertisement> getContainer() {
        return advertisements.getContainer();
    }

    public void setValue(Object key, String value) {
        advertisements.getItem(key).getItemProperty("advertisement").setValue(value);
    }

    private long advertisementTimestamp;
    private boolean isAdvertisementVisible;
    private Advertisement advertisementValue;
    final private Object advertisementLock = new Object();

    final private BackUp<Advertisement> advertisements;
    final String backupFilename;
}
