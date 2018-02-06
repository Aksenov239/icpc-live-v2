package org.icpclive.backup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.ContextListener;
import org.icpclive.datapassing.TeamData;
import org.icpclive.mainscreen.Utils;
import org.icpclive.utils.SynchronizedBeanItemContainer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class BackUp<T> {
    private static final Logger log = LogManager.getLogger(BackUp.class);

    public BackUp(Class<T> type, String backupFileName) {
        this.type = type;
        this.data = new SynchronizedBeanItemContainer<T>(type);
        this.backupFile = Paths.get(backupFileName);

        reload();

        Utils.StoppedThread schedule = new Utils.StoppedThread(new Utils.StoppedRunnable() {
            public void run() {
                while (!stop) {
                    backup();
                    try {
                        Thread.sleep(60000L);
                    } catch (InterruptedException e) {
                        log.error("error", e);
                    }
                }
            }
        });
        schedule.start();
        ContextListener.addThread(schedule);

//        //TODO Add this thread to contextListener queue
//        new Timer().scheduleAtFixedRate(
//                new TimerTask() {
//                    public void run() {
//                        backup();
//                    }
//                },
//                0L,
//                60000L);
    }

    public void reload() {
        synchronized (data) {
            data.removeAllItems();
            if (Files.exists(backupFile)) {
                try {
                    Files.readAllLines(backupFile).forEach(s -> data.addBean(gson.fromJson(s, type)));
                } catch (IOException e) {
                    log.error("error", e);
                }
            }
        }
    }

    public void backup() {
        try {
            Path tmpFile = Paths.get(backupFile.toString() + ".tmp");
            PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tmpFile.toFile()), StandardCharsets.UTF_8));
            synchronized (data) {
                data.getItemIds().forEach(v -> out.println(gson.toJson(v)));
            }
            out.close();

            Files.move(tmpFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("error", e);
        }
    }

    public void addItem(T item) {
        synchronized (data) {
            data.addItem(item);
        }
    }

    public void addItemAt(int index, T item) {
        synchronized (data) {
            data.addItemAt(index, item);
        }
    }

    public void removeItem(T item) {
        synchronized (data) {
            data.removeItem(item);
        }
    }

    public void setProperty(Object item, String property, String value) {
        synchronized (data) {
            data.getItem(item).getItemProperty(property).setValue(value);
        }
    }

    public List<T> getData() {
        synchronized (data) {
            return data.getItemIds();
        }
    }

    public BeanItemContainer<T> getContainer() {
        return data;
    }

    public BeanItem<T> getItem(Object itemId) {
        return data.getItem(itemId);
    }

    BeanItemContainer<T> data;
    Path backupFile;
    //static final Gson gson = new GsonBuilder().create();
    static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(TeamData.class, new TeamData.TeamDataDeserializer())
            .registerTypeAdapter(TeamData.class, new TeamData.TeamDataSerializer())
            .create();
    final Class<T> type;
}
