package ru.ifmo.acm.mainscreen;

/**
 * Created by Aksenov239 on 15.11.2015.
 */
public class Person {
    private String name;
    private String position;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Person(String name, String position) {
        this.name = name;
        this.position = position;
    }

}
