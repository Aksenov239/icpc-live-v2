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
    final private Object clockLock = new Object();

    private long standingsTimestamp;
    private boolean isStandingsVisible;
    private long standingsType;
    final private Object standingsLock = new Object();

    private long advertisementTimestamp;
    private boolean isAdvertisementVisible;
    private Advertisement advertisementValue;
    final private Object advertisementLock = new Object();

    final BeanItemContainer<Advertisement> advertisements;

    private long[] labelsTimestamps;
    private boolean[] isLabelsVisible;
    private Person[] labelsValues;
    final private Object[] labelsLock = {new Object(), new Object()};

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
        advertisements = new BeanItemContainer<>(Advertisement.class);
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
        synchronized (clockLock) {
            clockTimestamp = System.currentTimeMillis();
            isClockVisible = visible;
        }
    }

    public boolean isClockVisible() {
        synchronized (clockLock) {
            return isClockVisible;
        }
    }

    public String clockStatus() {
        synchronized (clockLock) {
            return clockTimestamp + "\n" + isClockVisible;
        }
    }

    public void setStandingsVisible(boolean visible, int type) {
        synchronized (standingsLock) {
            standingsTimestamp = System.currentTimeMillis();
            isStandingsVisible = visible;
            standingsType = type;
        }
    }

    public String standingsStatus() {
        synchronized (standingsLock) {
            return standingsTimestamp + "\n" + isStandingsVisible + "\n" + standingsType;
        }
    }

    public void setAdvertisementVisible(boolean visible, Advertisement advertisement){
        synchronized (advertisementLock) {
            advertisementTimestamp = System.currentTimeMillis();
            isAdvertisementVisible = visible;
            advertisementValue = advertisement;
        }
    }

    public String advertisementStatus(){
        synchronized (advertisementLock) {
            return advertisementTimestamp + "\n" + isAdvertisementVisible + "\n" + advertisementValue;
        }
    }

    public void setLabelVisible(boolean visible, Person label, int id) {
        synchronized (labelsLock[id]) {
            labelsTimestamps[id] = System.currentTimeMillis();
            isLabelsVisible[id] = visible;
            labelsValues[id] = label;
        }
    }

    public String labelsStatus() {
        String result = "";
        for (int i = 0; i < labelsLock.length; i++) {
            synchronized (labelsLock[i]) {
                result += labelsTimestamps[i] + "\n" + isLabelsVisible[i] + "\n" + labelsValues[i].toString() + " " + "\n";
            }
        }
        return result;
    }

    public String labelStatus(int id) {
        synchronized (labelsLock[id]) {
            return labelsTimestamps[id] + " " + isLabelsVisible[id] + " " + labelsValues[id];
        }
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
        return infoTimestamp + "\n" + isInfoVisible + "\n" + infoType + "\n" + infoTeamNumber;
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

    public void addAdvertisement(Advertisement advertisement) {
        synchronized (advertisements) {
            advertisements.addBean(advertisement);
        }
    }

    public void removeAdvertisement(Advertisement advertisement) {
        synchronized (advertisements) {
            advertisements.removeItem(advertisement);
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
