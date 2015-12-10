package ru.ifmo.acm;

import ru.ifmo.acm.datapassing.DataLoader;
import ru.ifmo.acm.events.PCMS.PCMSEventsLoader;
import ru.ifmo.acm.mainscreen.MainScreenData;
import ru.ifmo.acm.mainscreen.Utils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


@WebListener("Context listener for doing something or other.")
public class ContextListener implements ServletContextListener {
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
                        e.printStackTrace();
                    }
                }
            }
        });
        dataLoader.setDaemon(true);
        dataLoader.start();

        MainScreenData.getMainScreenData();

        PCMSEventsLoader.getInstance();

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