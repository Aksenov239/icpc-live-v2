package ru.ifmo.acm.datapassing;

import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Created by Aksenov239 on 15.11.2015.
 */
public class DataRequestHandler implements RequestHandler {
    private static final Logger log = LogManager.getLogger(DataRequestHandler.class);

    private static DataLoader loader = new DataLoader();

    public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response) throws IOException {
        if ("/data".equals(request.getPathInfo())) {
            String language = request.getParameter("language");
            log.info("Language query " + language);

            response.setContentType("text/plain");

            response.getWriter().println(loader.getDataFrontend());

            return true;
        } else {
            return false;
        }
    }
}
