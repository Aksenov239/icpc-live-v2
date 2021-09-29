package org.icpclive.events.codeforces;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.Config;
import org.icpclive.events.EventsLoader;
import org.icpclive.events.codeforces.api.CFApiCentral;
import org.icpclive.events.codeforces.api.data.CFContest;
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
    private CFContestInfo contestInfo;
    private CFApiCentral central;

    public CFEventsLoader(Class<? extends CFContestInfo> infoType) throws IOException {
        try {
            contestInfo = infoType.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
        Properties properties = Config.loadProperties("events");
        emulationSpeed = 1;
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
        boolean interrupted = false;
        while (!interrupted) {
            try {
                while (true) {
                    Thread.sleep(10000);
                    CFStandings standings = central.getStandings();
                    if (standings == null) {
                        continue;
                    }
                    List<CFSubmission> submissions = standings.contest.phase == CFContest.CFContestPhase.BEFORE ? null :
                            central.getStatus();
                    System.err.println("Data received");
                    contestInfo.update(standings, submissions);
                }
            } catch (InterruptedException e) {
                log.error("error", e);
                interrupted = true;
            }
        }
    }

    @Override
    public CFContestInfo getContestData() {
        return contestInfo;
    }
}
