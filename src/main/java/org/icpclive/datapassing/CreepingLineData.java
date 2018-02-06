package org.icpclive.datapassing;

import org.icpclive.creepingline.Message;
import org.icpclive.creepingline.MessageData;
import org.icpclive.mainscreen.Advertisement;

import java.util.List;
//import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class CreepingLineData extends CachedData {
    public List<Message> messages;
    public List<Advertisement> logos;

    public CreepingLineData initialize() {
        messages = MessageData.getMessageData().getMessages();
        logos = MessageData.getMessageData().getLogos();

        return this;
    }
}
