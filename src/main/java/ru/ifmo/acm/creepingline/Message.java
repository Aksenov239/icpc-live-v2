package ru.ifmo.acm.creepingline;

/**
 * Created by Aksenov239 on 14.11.2015.
 */
public class Message {
    private String message;
    private long creationTime;
    private long endTime;
    private long time;
    private boolean isAdvertisement;
    private String source;


    public Message(String message, long time, long duration, boolean isAdvertisement) {
        this.setMessage(message);
        this.setCreationTime(time);
        this.setEndTime(time + duration);
        this.setTime(duration / 1000);
        this.setIsAdvertisement(isAdvertisement);
    }

    public Message(String message, long time, long duration, boolean isAdvertisement, String source) {
        this(message, time, duration, isAdvertisement);
        this.source = source;
    }

    public Message(String message, long time, long duration)    {
        this(message, time, duration, false);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean getIsAdvertisement() {
        return isAdvertisement;
    }

    public void setIsAdvertisement(boolean isAdvertisement) {
        this.isAdvertisement = isAdvertisement;
    }

    public Message clone() {
            return new Message(message, creationTime, endTime - creationTime, isAdvertisement );
    }

    public String getSource() {
        return source;
    }
}
