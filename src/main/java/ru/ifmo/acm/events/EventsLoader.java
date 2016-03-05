package ru.ifmo.acm.events;

import ru.ifmo.acm.events.PCMS.PCMSContestInfo;
import ru.ifmo.acm.events.PCMS.PCMSEventsLoader;
import ru.ifmo.acm.events.WF.WFEventsLoader;

import java.io.IOException;
import java.util.Properties;

public abstract class EventsLoader extends Thread {
    private static EventsLoader instance;

    public static EventsLoader getInstance() {
        if (instance == null) {
            Properties properties = new Properties();

            try {
                properties.load(EventsLoader.class.getClassLoader().getResourceAsStream("events.properties"));

                String standingsType = properties.getProperty("standings.type");

                if ("WF".equals(standingsType)) {
                    instance = new WFEventsLoader();
                } if ("PCMS".equals(standingsType)) {
                    instance = new PCMSEventsLoader();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }


    public abstract void run();

    public abstract ContestInfo getContestData();
}
