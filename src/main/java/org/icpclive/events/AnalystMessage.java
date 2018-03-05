package org.icpclive.events;

import org.icpclive.events.WF.old.WFAnalystMessage;

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
