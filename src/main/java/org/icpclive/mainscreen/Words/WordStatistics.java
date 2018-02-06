package org.icpclive.mainscreen.Words;

/**
 * Created by Aksenov239 on 14.05.2017.
 */
public class WordStatistics {
    private String word;
    private String wordName;
    private String picture;
    private long count;

    public WordStatistics(String wordName, String word, String picture) {
        this.word = word;
        this.wordName = wordName;
        this.picture = picture;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getWordName() {
        return wordName;
    }

    public void setWordName(String wordName) {
        this.wordName = wordName;
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
