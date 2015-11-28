package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.mainscreen.Person;
import ru.ifmo.acm.mainscreen.MainScreenData;
import ru.ifmo.acm.mainscreen.statuses.PersonStatus;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class PersonData implements CachedData {
    public long[] timestamp;
    public boolean[] isVisible;
    public Person[] label;

    public PersonData(){
        timestamp = new long[2];
        isVisible = new boolean[2];
        label = new Person[2];
    }

    public PersonData initialize() {
        PersonStatus status = MainScreenData.getMainScreenData().personStatus;
        status.initialize(this);
        return this;
    }
}
