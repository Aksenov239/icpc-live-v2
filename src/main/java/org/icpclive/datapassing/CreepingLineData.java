package org.icpclive.datapassing;

import org.icpclive.webadmin.creepingline.Message;
import org.icpclive.webadmin.creepingline.MessageData;
import org.icpclive.webadmin.mainscreen.Advertisement;
import org.icpclive.webadmin.mainscreen.MainScreenData;

import java.util.ArrayList;
import java.util.List;
//import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class CreepingLineData extends CachedData {
    public boolean isVisible;
    public List<Message> messages;
    public List<Advertisement> logos;

    public CreepingLineData() {
        isVisible = true;
        messages = new ArrayList<>();
        logos = new ArrayList<>();
    }

    public CreepingLineData initialize() {
        messages = MessageData.getMessageData().getMessages();
        logos = MessageData.getMessageData().getLogos();
        isVisible = MessageData.getMessageData().isVisible();

        return this;
    }
}
