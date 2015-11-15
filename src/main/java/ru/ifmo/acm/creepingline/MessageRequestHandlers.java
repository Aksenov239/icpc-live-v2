package ru.ifmo.acm.creepingline;

import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by Aksenov239 on 15.11.2015.
 */
public class MessageRequestHandlers implements RequestHandler {
    public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response) throws IOException {
        if ("/messages".equals(request.getPathInfo())) {
            String language = request.getParameter("language");
            System.err.println("Language query " + language);

            response.setContentType("text/plain");

            for (Message message : MessageData.getMessageData().getMessages()) {
                response.getWriter().println((message.getIsAdvertisement() ? "!" : "+") + message.getMessage());
            }

            return true;
        } else {
            return false;
        }
    }
}
