package ru.ifmo.acm.mainscreen;

import com.vaadin.data.util.BeanItemContainer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Aksenov239 on 15.11.2015.
 */
public class MainScreenData {
    private static MainScreenData mainScreenData;

    public static MainScreenData getMainScreenData() {
        if (mainScreenData == null) {
            mainScreenData = new MainScreenData();
        }
        return mainScreenData;
    }

    private long clockTimestamp;
    boolean isClockVisible;
    final private Object clock = new Object();

    private long standingsTimestamp;
    private boolean isStandingsVisible;
    private long standingsType;
    final private Object standings = new Object();

    private long[] labelsTimestamps;
    private boolean[] isLabelsVisible;
    private String[] labelsValues;
    final private Object[] labels = {new Object(), new Object(), new Object()};

    private long infoTimestamp;
    private boolean isInfoVisible;
    private String infoType;
    private int infoTeamNumber;
    final private Object info = new Object();

    final BeanItemContainer<Person> persons;

    String backupPersons;

    private MainScreenData() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/persons.properties"));
            backupPersons = properties.getProperty("backup.file.name");
        } catch (IOException e) {
            e.printStackTrace();
        }

        persons = new BeanItemContainer<>(Person.class);

        reload();

        new Timer().scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        backup();
                    }
                },
                0L,
                60000L);
    }

    public void setClockVisible(boolean visible) {
        synchronized (clock) {
            clockTimestamp = System.currentTimeMillis();
            isClockVisible = visible;
        }
    }

    public boolean isClockVisible() {
        synchronized (clock) {
            return isClockVisible;
        }
    }

    public String clockStatus() {
        synchronized (clock) {
            return clockTimestamp + " " + isClockVisible;
        }
    }

    public void setStandingsVisible(boolean visible, int type) {
        synchronized (standings) {
            standingsTimestamp = System.currentTimeMillis();
            isStandingsVisible = visible;
            standingsType = type;
        }
    }

    public String standingsStatus() {
        synchronized (standings) {
            return standingsTimestamp + " " + isStandingsVisible + " " + standingsType;
        }
    }

    public void setLabelVisible(boolean visible, String label, int id) {
        synchronized (labels[id]) {
            labelsTimestamps[id] = System.currentTimeMillis();
            isLabelsVisible[id] = visible;
            labelsValues[id] = label;
        }
    }

    public String labelsStatus() {
        String result = "";
        for (int i = 0; i < labels.length; i++) {
            synchronized (labels[i]) {
                result += labelsTimestamps[i] + " " + isLabelsVisible[i] + " " + labelsValues[i] + "\n";
            }
        }
        return result;
    }

    public void setInfoVisible(boolean visible, String type, int teamId) {
        synchronized (info) {
            infoTimestamp = System.currentTimeMillis();
            isInfoVisible = visible;
            infoType = type;
            infoTeamNumber = teamId;
        }
    }

    public String infoStatus() {
        return infoTimestamp + " " + isInfoVisible + " " + infoType + " " + infoTeamNumber;
    }

    public void reload() {
        synchronized (persons) {
            persons.removeAllItems();
            File file = new File(backupPersons);
            if (file.exists()) {
                try {
                    Scanner sc = new Scanner(file);//getClass().getResourceAsStream("/" + backup));
                    while (sc.hasNextLine()) {
                        persons.addBean(new Person(sc.nextLine(), sc.nextLine()));
                    }
                    sc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void backup() {
        try {
            String path = backupPersons;//getClass().getResource(backup).getPath();

            String tmpFile = path + ".tmp";

            PrintWriter out = new PrintWriter(tmpFile);
            synchronized (persons) {
                for (Person person : persons.getItemIds()) {
                    out.println(person.getName());
                    out.println(person.getPosition());
                }
            }
            out.close();

            Files.move(new File(tmpFile).toPath(), new File(backupPersons).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPerson(Person person) {
        synchronized (persons) {
            persons.addBean(person);
        }
    }

    public void removePerson(Person person) {
        synchronized (persons) {
            persons.removeItem(person);
        }
    }

}
