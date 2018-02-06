package org.icpclive.creepingline;

import com.vaadin.data.Item;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;
import org.icpclive.mainscreen.loaders.TwitterLoader;

/**
 * Created by Aksenov239 on 14.11.2015.
 */
public class MessageForm extends FormLayout {

    final MessageData messageData;
    final CreepingLineView parent;
    Message messageObject;

    final TextField message;
    final ComboBox timeBox;
    final CheckBox advertBox;

    VerticalLayout form;

    final String[] timeBoxValues;

    public MessageForm(CreepingLineView parent) {
        this.parent = parent;

        //VerticalLayout panel = new VerticalLayout();

        Button newMessage = new Button("New Message");
        newMessage.addClickListener(event -> {
            parent.messageList.setValue(null);
            this.edit(null);
        });
        newMessage.setSizeUndefined();

        form = new VerticalLayout();

        messageData = MessageData.getMessageData();

        form.setVisible(false);

        form.setSizeUndefined();
        form.setMargin(true);

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

            form.setVisible(false);

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

            form.setVisible(false);

            Notification.show("Deleted", Type.TRAY_NOTIFICATION);
        });

        Button cancel = new Button("Cancel", event -> {
            form.setVisible(false);

            parent.messageList.setValue(null);

            Notification.show("Cancelled", Type.TRAY_NOTIFICATION);
        });

        Button post = new Button("Tweet", event -> {
            TwitterLoader.getInstance().postMessage(message.getValue());
            message.clear();
            form.setVisible(false);

            parent.messageList.setValue(null);

            Notification.show("Tweeted message", Type.TRAY_NOTIFICATION);
        });

        HorizontalLayout actions = new HorizontalLayout(save, delete, cancel, post);
        actions.setSpacing(true);

        form.addComponents(actions, message, timeBox, advertBox);
        message.setSizeFull();
        advertBox.setSizeUndefined();
        form.setSizeFull();
        form.setSpacing(true);

        addComponents(newMessage, form);

        setSizeFull();
        setVisible(true);
    }

    public void edit(Message message) {
        messageObject = message;
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
        form.setVisible(true);
    }

    public void editFromFlow(Message message) {
        if (message == null) return;
        messageObject = null;
        this.message.clear();
        timeBox.setVisible(true);
        timeBox.setValue(timeBoxValues[0]);
        this.message.setValue((message.getSource() == null || message.getSource().isEmpty() ? "" : message.getSource() + ": ") + message.getMessage());
        advertBox.setValue(message.getIsAdvertisement());
        form.setVisible(true);
    }

}
