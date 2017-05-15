package ru.ifmo.acm.mainscreen.Words;

/**
 * Created by Aksenov239 on 14.05.2017.
 */
public class WordStatistics {
    private String word;
    private String picture;
    private long count;

    public WordStatistics(String word, String picture) {
        this.word = word;
        this.picture = picture;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void increase() {
        setCount(count + 1);
    }

    public String toString() {
        return word + " " + picture + " " + count;
    }
}
