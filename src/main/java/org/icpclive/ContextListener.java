package org.icpclive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.creepingline.MessageData;
import org.icpclive.datapassing.DataLoader;
import org.icpclive.mainscreen.MainScreenData;
import org.icpclive.mainscreen.loaders.TwitchLoader;
import org.icpclive.mainscreen.loaders.TwitterLoader;
import org.icpclive.events.EventsLoader;
import org.icpclive.mainscreen.Utils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


@WebListener("Context listener for doing something or other.")
public class ContextListener implements ServletContextListener {
    private static final Logger log = LogManager.getLogger(ContextListener.class);

    // Vaadin app deploying/launching.
    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        Utils.StoppedThread dataLoader;

        ServletContext context = contextEvent.getServletContext();
        dataLoader = new Utils.StoppedThread(new Utils.StoppedRunnable() {
            public void run() {
                while (!stop) {
                    DataLoader.iterateFrontend();
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        log.error("error", e);
                    }
                }
            }
        });
        dataLoader.setDaemon(true);
        dataLoader.start();

        MainScreenData.getMainScreenData();

        EventsLoader.getInstance();

        MessageData.getMessageData();

        TwitchLoader.start();

        TwitterLoader.start();

        threadsList.add(dataLoader);
    }

    // Vaadin app un-deploying/shutting down.
    @Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
        ServletContext context = contextEvent.getServletContext();

        DataLoader.free();
        threadsList.forEach(thread -> {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
        });
    }

    public static void addThread(Utils.StoppedThread thread) {
        threadsList.add(thread);
    }

    static private BlockingQueue<Utils.StoppedThread> threadsList = new LinkedBlockingQueue<>();
}