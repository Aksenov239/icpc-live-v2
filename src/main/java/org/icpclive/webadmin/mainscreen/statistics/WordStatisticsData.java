package org.icpclive.webadmin.mainscreen.statistics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.webadmin.backup.BackUp;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by Aksenov239 on 14.05.2017.
 */
public class WordStatisticsData {
    private static Logger log = LogManager.getLogger(WordStatisticsData.class);

    private static WordStatisticsData wordData;

    public static WordStatisticsData getInstance() {
        if (wordData == null) {
            wordData = new WordStatisticsData();
        }
        return wordData;
    }

    public static BackUp<WordStatistics> wordsList;
    private String backUpFile;

    public final static String TWEET_KEYWORD = "$tweets$";

    public WordStatisticsData() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/mainscreen.properties"));
            backUpFile = properties.getProperty("word.statistics.backup.file");
        } catch (IOException e) {
            log.error("error", e);
        }
        wordsList = new BackUp<>(WordStatistics.class, backUpFile);

        if (wordsList.getData().size() == 0) {
            String[] words = properties.getProperty("word.statistics.words").split(";");
            for (String word : words) {
                String picture = properties.getProperty("word.statistics." + word + ".picture");
                String realWord = properties.getProperty("word.statistics." + word + ".text");

                wordsList.addItem(new WordStatistics(word, realWord, picture));
            }
        }
    }

    public void addWord(WordStatistics word) {
        wordsList.addItem(word);
    }

    public void removeWord(WordStatistics word) {
        wordsList.removeItem(word);
    }

    public void setValue(Object key, String property, String value) {
        wordsList.setProperty(key, property, value);
    }

    public static void vote(String text) {
        text = text.toLowerCase();
        if (wordsList == null) {
            return;
        }
        for (WordStatistics word : wordsList.getData()) {
            String[] patterns = word.getWord().toLowerCase().split(";");
            for (String pattern : patterns) {
                if (text.contains(pattern)) {
                    wordsList.getItem(word).getItemProperty("count").setValue(word.getCount() + 1);
                    break;
                }
            }
        }
    }
}
