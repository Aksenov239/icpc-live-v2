package org.icpclive.backend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.datapassing.DataLoader;
import org.icpclive.events.EventsLoader;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.io.InputStream;

/**
 * Created by aksenov on 15.04.2015.
 */
public class Preparation {
    private static final Logger log = LogManager.getLogger(Preparation.class);

    public static EventsLoader eventsLoader;

    public static void prepareEventsLoader() {
        eventsLoader = EventsLoader.getInstance();
        eventsLoader.start();
    }

    public static DataLoader dataLoader;

    public static void prepareDataLoader() throws IOException {
        dataLoader = new DataLoader();
        dataLoader.backendInitialize();
    }

    public static void prepareNetwork(final String login, final String password) {
        if (login == null || password == null)
            return;
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }

                }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("error", e);
        }


        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        Authenticator.setDefault(
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(login, password.toCharArray());
                    }
                });

        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

//        System.setProperty("javax.net.ssl.keyStore", "C:/work/icpc-live/resources/key.jks");
//        System.setProperty("javax.net.ssl.trustStore", "C:/work/icpc-live/resources/key.jks");
    }

    public static InputStream openAuthorizedStream(String url, String login, String password) throws IOException {
        if (!url.contains("http")){
            return new FileInputStream(url);
        }

        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestProperty("Authorization",
                "Basic " + Base64.getEncoder().encodeToString((login + ":" + password).getBytes()));
        con.connect();
        log.debug(con.getHeaderFields());
        return con.getInputStream();
    }
}
