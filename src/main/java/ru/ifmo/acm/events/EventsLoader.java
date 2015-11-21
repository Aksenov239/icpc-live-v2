package ru.ifmo.acm.events;

public abstract class EventsLoader extends Thread {
    public abstract void run();

    public abstract ContestInfo getContestData();
}
