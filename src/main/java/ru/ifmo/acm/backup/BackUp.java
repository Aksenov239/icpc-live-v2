package ru.ifmo.acm.backup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import ru.ifmo.acm.utils.SynchronizedBeanItemContainer;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BackUp<T> {
    public BackUp(Class<T> type, String backupFileName) {
        this.type = type;
        this.data = new SynchronizedBeanItemContainer<T>(type);
        this.backupFile = Paths.get(backupFileName);

        reload();

        //TODO Add this thread to contextListener queue
        new Timer().scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        backup();
                    }
                },
                0L,
                60000L);
    }

    public void reload() {
        synchronized (data) {
            data.removeAllItems();
            if (Files.exists(backupFile)) {
                try {
                    Files.readAllLines(backupFile).forEach(s -> data.addBean(gson.fromJson(s, type)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void backup() {
        try {
            Path tmpFile = Paths.get(backupFile.toString() + ".tmp");
            PrintWriter out = new PrintWriter(tmpFile.toFile());
            synchronized (data) {
                data.getItemIds().forEach(v -> out.println(gson.toJson(v)));
            }
            out.close();

            Files.move(tmpFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addItem(T item) {
        synchronized (data) {
            data.addItem(item);
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
    static final Gson gson = new GsonBuilder().create();
    final Class<T> type;
}
