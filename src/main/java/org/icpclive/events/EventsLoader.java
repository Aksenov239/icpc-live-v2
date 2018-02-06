package org.icpclive.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.events.PCMS.PCMSEventsLoader;
import org.icpclive.events.WF.WFEventsLoader;

import java.io.IOException;
import java.util.Properties;

public abstract class EventsLoader extends Thread {
    private static final Logger log = LogManager.getLogger(EventsLoader.class);

    private static EventsLoader instance;

    public static double EMULATION_SPEED = 1;

    public static synchronized EventsLoader getInstance() {
        if (instance == null) {
            Properties properties = new Properties();

            try {
                properties.load(EventsLoader.class.getClassLoader().getResourceAsStream("events.properties"));

                String standingsType = properties.getProperty("standings.type");

                System.err.println(standingsType);

                if ("WF".equals(standingsType)) {
                    instance = new WFEventsLoader();
                } if ("PCMS".equals(standingsType)) {
                    instance = new PCMSEventsLoader();
                }
            } catch (IOException e) {
                log.error("error", e);
            }
        }
        return instance;
    }


    public abstract void run();

    public abstract ContestInfo getContestData();
}
