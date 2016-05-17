package ru.ifmo.acm.events;

import ru.ifmo.acm.events.WF.WFAnalystMessage;

/**
 * @author egor@egork.net
 */
public interface AnalystMessage {
    int getId();

    int getTeam();

    int getTime();

    int getPriority();

    int getProblem();

    int getRunId();

    WFAnalystMessage.WFAnalystMessageCategory getCategory();

    String getMessage();
}
