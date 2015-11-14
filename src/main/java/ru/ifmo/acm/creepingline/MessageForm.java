package ru.ifmo.acm.creepingline;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.Notification.Type;

/**
 * Created by Aksenov239 on 14.11.2015.
 */
public class MessageForm extends FormLayout {

    BeanFieldGroup<Message> formFieldBindings;
    final MessageData messageData;
    Message message;

    public MessageForm() {
        messageData = MessageData.getMessageData();

        setVisible(false);

        setSizeUndefined();
        setMargin(true);

        final TextField message = new TextField("Message");
        final ComboBox timeBox = new ComboBox();
        final String[] timeBoxValues = new String[]{"30 seconds", "1 minute", "2 minutes", "5 minutes", "infinity milliseconds"};
        final int[] duration = new int[]{30000, 60000, 120000, 3000000, Integer.MAX_VALUE};
        timeBox.addItems(timeBoxValues);
        final CheckBox advertBox = new CheckBox();

        Button save = new Button("Save", event -> {
            int time = 0;
            for (int i = 0; i < duration.length; i++) {
                if (timeBoxValues[i].equals(timeBox.getValue())) {
                    time = duration[i];
                }
            }

            messageData.addMessage(new Message(
                    message.getValue(),
                    System.currentTimeMillis(),
                    time,
                    advertBox.getValue()
            ) );

            message.clear();

            Notification.show("Created new advertisement", Type.TRAY_NOTIFICATION);
        });
        save.setStyleName(ValoTheme.BUTTON_PRIMARY);
        save.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        Button cancel = new Button("Cancel", event -> {
            Notification.show("Cancelled", Type.TRAY_NOTIFICATION);
        });


        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setSpacing(true);

        addComponents(actions);
    }

}
