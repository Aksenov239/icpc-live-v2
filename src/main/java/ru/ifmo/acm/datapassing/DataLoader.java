package ru.ifmo.acm.datapassing;

import com.google.gson.Gson;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class DataLoader {
    public static Gson gson = new Gson();

    String link;

    public DataLoader() {
        //TODO initialize link from properties and run thread here on AtomicReference if frontend = false
    }

    public String getDataFrontend() {
        return gson.toJson(new Data().initialize());
    }

    public Data getDataBackend() {
        return null;
    }

}
