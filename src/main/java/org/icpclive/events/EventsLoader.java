package org.icpclive.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.Config;
import org.icpclive.events.PCMS.PCMSEventsLoader;
import org.icpclive.events.WF.json.WFEventsLoader;

import java.io.IOException;
import java.util.Properties;

public abstract class EventsLoader implements Runnable {
    private static final Logger log = LogManager.getLogger(EventsLoader.class);

    private static EventsLoader instance;

    protected double emulationSpeed;
    protected long emulationStartTime;

    public static synchronized EventsLoader getInstance() {
        if (instance == null) {
            try {
                Properties properties = Config.loadProperties("events");
                String standingsType = properties.getProperty("standings.type");
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

    public double getEmulationSpeed() {
        return emulationSpeed;
    }
}
