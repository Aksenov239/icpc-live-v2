package org.icpclive.webadmin.creepingline;

import com.vaadin.data.Item;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;
import org.icpclive.webadmin.mainscreen.loaders.TwitterLoader;

import java.util.ArrayList;

/**
 * Created by Aksenov239 on 14.11.2015.
 */
public class MessageForm {

    final MessageData messageData;
    final CreepingLineView parent;
    Message messageObject;

    final TextField message;
    final ComboBox timeBox;
    final CheckBox advertBox;

    VerticalLayout form;

    final String[] timeBoxValues;

    final ArrayList<Component> toHide = new ArrayList<>();

    Component component;

    public void setVisible(boolean visible) {
        for (Component c : toHide) {
            c.setVisible(visible);
        }
    }

    public MessageForm(CreepingLineView parent) {
        this.parent = parent;

        //VerticalLayout panel = new VerticalLayout();

        Button newMessage = new Button("New Message");
        newMessage.addClickListener(event -> {
            parent.messageList.setValue(null);
            this.edit(null);
        });
//        newMessage.setSizeUndefined();

        messageData = MessageData.getMessageData();

//        form.setVisible(false);

//        form.setSizeUndefined();
//        form.setMargin(true);

        message = new TextField("Message:");
        timeBox = new ComboBox("Duration:");
        timeBoxValues = new String[]{"30 seconds", "1 minute", "2 minutes", "5 minutes", "10 minutes", "15 minutes", "30 minutes", "infinity milliseconds"};
        final int[] duration = new int[]{30000, 60000, 120000, 300000, 600000, 900000, 1800000, Integer.MAX_VALUE};
        timeBox.addItems(timeBoxValues);
        timeBox.setNullSelectionAllowed(false);
        timeBox.setValue(timeBoxValues[0]);
        advertBox = new CheckBox("Advertisement");

        Button save = new Button("Save", event -> {
            int time = 0;
            for (int i = 0; i < duration.length; i++) {
                if (timeBoxValues[i].equals(timeBox.getValue())) {
                    time = duration[i];
                }
            }

            if (messageObject == null) {
                messageData.addMessage(new Message(
                        message.getValue(),
                        System.currentTimeMillis(),
                        time,
                        advertBox.getValue()
                ));
            } else {
                synchronized (messageData.messageList.getContainer()) {
                    Item messageBean = messageData.messageList.getItem(messageObject);
//                messageObject.setMessage(message.getValue());
//                messageObject.setIsAdvertisement(advertBox.getValue());
                    messageBean.getItemProperty("message").setValue(message.getValue());
                    messageBean.getItemProperty("isAdvertisement").setValue(advertBox.getValue());
//                parent.messageList.refreshRowCache();
                }
            }

            message.clear();

            setVisible(false);

            parent.messageList.setValue(null);

            if (messageObject == null) {
                Notification.show("Created new message", Type.TRAY_NOTIFICATION);
            } else {
                Notification.show("Edit message", Type.TRAY_NOTIFICATION);
            }
        });
        save.setStyleName(ValoTheme.BUTTON_PRIMARY);
        save.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        Button delete = new Button("Delete", event -> {
            messageData.removeMessage((Message) parent.messageList.getValue());

            setVisible(false);

            Notification.show("Deleted", Type.TRAY_NOTIFICATION);
        });

        Button cancel = new Button("Cancel", event -> {
            setVisible(false);

            parent.messageList.setValue(null);

            Notification.show("Cancelled", Type.TRAY_NOTIFICATION);
        });

        Button post = new Button("Tweet", event -> {
            TwitterLoader.getInstance().postMessage(message.getValue());
            message.clear();
            setVisible(false);

            parent.messageList.setValue(null);

            Notification.show("Tweeted message", Type.TRAY_NOTIFICATION);
        });

        toHide.add(save);
        toHide.add(delete);
        toHide.add(cancel);
        toHide.add(post);
        toHide.add(message);
        toHide.add(timeBox);
        toHide.add(advertBox);

        HorizontalLayout actions = new HorizontalLayout(newMessage, save, delete, cancel, post);
        actions.setSpacing(true);

        form = new VerticalLayout(actions, message, timeBox, advertBox);
        message.setSizeFull();
        timeBox.setSizeFull();
//        advertBox.setSizeUndefined();
        form.setSizeFull();
        form.setSpacing(true);

        setVisible(false);

        component = form;
    }

    public void edit(Message message) {
        messageObject = message;
        setVisible(true);
        if (message != null) {
            this.message.setValue(message.getMessage());
            advertBox.setValue(message.getIsAdvertisement());
            timeBox.setVisible(false);
        } else {
            this.message.clear();
            timeBox.setVisible(true);
            timeBox.setValue(timeBoxValues[0]);
            advertBox.setValue(false);
        }
    }

    public void editFromFlow(Message message) {
        if (message == null) return;
        messageObject = null;
        this.message.clear();
//        timeBox.setVisible(true);
        timeBox.setValue(timeBoxValues[0]);
        this.message.setValue((message.getSource() == null || message.getSource().isEmpty() ? "" : message.getSource() + ": ") + message.getMessage());
        advertBox.setValue(message.getIsAdvertisement());
        setVisible(true);
    }

}
