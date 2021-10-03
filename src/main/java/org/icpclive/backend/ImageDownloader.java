package org.icpclive.backend;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.icpclive.Config;
import org.icpclive.events.ContestInfo;
import org.icpclive.events.EventsLoader;

import java.io.*;
import java.net.URL;
import java.util.Properties;

import static org.apache.logging.log4j.core.impl.ThrowableFormatOptions.FILE_NAME;

/**
 * Created by icpclive on 4/1/2019.
 */
public class ImageDownloader {
    private String login;
    private String password;

    public String readJsonArray(String url) throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Preparation.openAuthorizedStream(url, login, password)));
        String json = "";
        String line;
        while ((line = br.readLine()) != null) {
            json += line.trim();
        }
        return json;
    }

    public void run() throws IOException {
        Properties properties = Config.loadProperties("events");

        login = properties.getProperty("login");
        password = properties.getProperty("password");

        Preparation.prepareNetwork(login, password);

        String url = properties.getProperty("url");

        JsonArray jsonTeams = new Gson().fromJson(
                readJsonArray(url + "/teams"), JsonArray.class);

        for (int i = 0; i < jsonTeams.size(); i++) {
            JsonObject jsonObject = jsonTeams.get(i).getAsJsonObject();
            String id = jsonObject.get("id").getAsString();
            String organization_id = jsonObject.get("organization_id").getAsString();
            String logo_url = String.format("https://192.168.1.207/api/contests/test1/organizations/%s/logo.160x160", organization_id);
            System.err.println(logo_url);
            try (BufferedInputStream in = new BufferedInputStream(new URL(logo_url).openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(String.format("teamData_new/%s.png", id))) {
                byte dataBuffer[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
            } catch (IOException e) {

            }
        }
    }

    public static void main(String[] args) throws IOException {
        new ImageDownloader().run();
    }
}

