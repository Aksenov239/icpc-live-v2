package ru.ifmo.acm.mainscreen.statuses;

import ru.ifmo.acm.backup.BackUp;
import ru.ifmo.acm.mainscreen.Person;

public class PersonStatus {
    public PersonStatus(String backupFilename) {
        this.backupFilename = backupFilename;
        persons = new BackUp<>(Person.class, backupFilename);
    }

    public synchronized void setLabelVisible(boolean visible, Person label, int id) {
        labelsTimestamps[id] = System.currentTimeMillis();
        isLabelsVisible[id] = visible;
        labelsValues[id] = label;
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

    private long[] labelsTimestamps;
    private boolean[] isLabelsVisible;
    private Person[] labelsValues;
    final private Object[] labelsLock = {new Object(), new Object()};

    final BackUp<Person> persons;
    String backupFilename;
}
