package ru.ifmo.acm.datapassing;

import ru.ifmo.acm.creepingline.Message;
import ru.ifmo.acm.creepingline.MessageData;

import java.util.List;
//import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class CreepingLineData extends CachedData {
    public List<Message> messages;

    public CreepingLineData initialize() {
        messages = MessageData.getMessageData().getMessages();
        return this;
    }
}
