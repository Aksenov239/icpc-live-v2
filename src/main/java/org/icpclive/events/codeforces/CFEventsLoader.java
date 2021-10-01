package org.icpclive.events.codeforces;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.Config;
import org.icpclive.events.EventsLoader;
import org.icpclive.events.codeforces.api.CFApiCentral;
import org.icpclive.events.codeforces.api.data.CFSubmission;
import org.icpclive.events.codeforces.api.results.CFStandings;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * @author egor@egork.net
 */
public class CFEventsLoader extends EventsLoader {
    private static final Logger log = LogManager.getLogger(CFEventsLoader.class);
    private CFContestInfo contestInfo = new CFContestInfo();
    private CFApiCentral central;

    public CFEventsLoader() throws IOException {
        Properties properties = Config.loadProperties("events");
        central = new CFApiCentral(Integer.parseInt(properties.getProperty("contest_id")));
    }

    public static CFEventsLoader getInstance() {
        EventsLoader eventsLoader = EventsLoader.getInstance();
        if (!(eventsLoader instanceof CFEventsLoader)) {
            throw new IllegalStateException();
        }
        return (CFEventsLoader) eventsLoader;
    }

    @Override
    public void run() {
        while (true) {
            try {
                while (true) {
                    Thread.sleep(100);
                    List<CFSubmission> submissions = central.getStatus();
                    if (submissions == null) {
                        continue;
                    }
                    CFStandings standings = central.getStandings();
                    if (standings == null) {
                        continue;
                    }
                    System.err.println("Data received");
                    contestInfo.update(standings, submissions);
                }
            } catch (InterruptedException e) {
                log.error("error", e);
            }
        }
    }

    @Override
    public CFContestInfo getContestData() {
        return contestInfo;
    }
}
