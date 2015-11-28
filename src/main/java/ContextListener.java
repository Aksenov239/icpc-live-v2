import ru.ifmo.acm.datapassing.DataLoader;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener("Context listener for doing something or other.")
public class ContextListener implements ServletContextListener {

    Thread dataLoader;

    // Vaadin app deploying/launching.
    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        ServletContext context = contextEvent.getServletContext();
        dataLoader = new Thread(() -> {
            while (true) {
                DataLoader.iterateFrontend();
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        dataLoader.setDaemon(true);
        dataLoader.start();
    }

    // Vaadin app un-deploying/shutting down.
    @Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
        ServletContext context = contextEvent.getServletContext();
        if (dataLoader != null && dataLoader.isAlive()) {
            dataLoader.stop();
        }
        DataLoader.free();
    }

}