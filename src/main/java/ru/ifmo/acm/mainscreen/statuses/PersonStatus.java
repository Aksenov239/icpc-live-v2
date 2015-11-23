package ru.ifmo.acm.mainscreen.statuses;

import com.vaadin.data.util.BeanItemContainer;
import ru.ifmo.acm.backup.BackUp;
import ru.ifmo.acm.mainscreen.Person;
import ru.ifmo.acm.datapassing.Data;

public class PersonStatus {
    public PersonStatus(String backupFilename, long timeToShow) {
        this.backupFilename = backupFilename;
        persons = new BackUp<>(Person.class, backupFilename);
        labelsTimestamps = new long[2];
        isLabelsVisible = new boolean[2];
        labelsValues = new Person[2];
        this.timeToShow = timeToShow;
    }

    private void recache() {
        //Data.cache.refresh(PersonData.class);
    }

    public void setLabelVisible(boolean visible, Person label, int id) {
        //System.err.println("Set visible " + visible + " " + labelsValues[id] + " " + label);
        synchronized (labelsLock[id]) {
            labelsTimestamps[id] = System.currentTimeMillis();
            isLabelsVisible[id] = visible;
            labelsValues[id] = label;
        }
        recache();
    }

    public void update() {
        for (int id = 0; id < 2; id++) {
            synchronized (labelsLock[id]) {
                if (labelsTimestamps[id] > System.currentTimeMillis() + timeToShow) {
                    isLabelsVisible[id] = false;
                }
            }
        }
        recache();
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
            if (labelsValues[id] == null) {
                setLabelVisible(false, null , id);
            }
            return labelsTimestamps[id] + "\n" + isLabelsVisible[id] + "\n" + (labelsValues[id] != null ? labelsValues[id].getName() : "");
        }
    }

    public void addPerson(Person person) {
//        synchronized (persons) {
//            persons.addBean(person);
//        }
        persons.addItem(person);
    }

    public void removePerson(Person person) {
//        synchronized (persons) {
//            persons.removeItem(person);
//        }
        persons.removeItem(person);
    }

    public void setValue(Object key, String property, String value) {
        persons.setProperty(key, property, value);
    }

    public BeanItemContainer<Person> getContainer() {
        return persons.getContainer();
    }

    private long[] labelsTimestamps;
    private boolean[] isLabelsVisible;
    private Person[] labelsValues;
    final private Object[] labelsLock = {new Object(), new Object()};

    final BackUp<Person> persons;
    String backupFilename;
    long timeToShow;
}
