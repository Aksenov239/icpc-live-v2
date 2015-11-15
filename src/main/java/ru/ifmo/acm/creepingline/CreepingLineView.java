package ru.ifmo.acm.creepingline;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;

/**
 * Created by Aksenov239 on 14.11.2015.
 */
public class CreepingLineView extends CustomComponent implements View {
    public static final String NAME = "creepingLine";

    final MessageData messageData;
    final MessageForm messageForm;
    final BeanItemContainer<Message> container;
    Table messageList;
//    Button newMessage;

    public CreepingLineView() {
        messageData = MessageData.getMessageData();
        messageForm = new MessageForm(this);

//        newMessage = new Button("New Message");

//        newMessage.addClickListener(event -> {
//            messageList.setValue(null);
//            messageForm.edit(null);
//        });

        container = messageData.messageList;
        messageList = new Table();
        messageList.setContainerDataSource(container);
        messageList.addGeneratedColumn("time", (Table source, Object itemId, Object columnId) -> {
            Message message = (Message) itemId;
            Label label = new Label();
            label.setValue("" + (message.getEndTime() - System.currentTimeMillis()) / 1000);
            return label;
        });
        String[] columns = {"message", "isAdvertisement", "time"};
        messageList.setVisibleColumns(columns);
        messageList.setSelectable(true);
        messageList.setMultiSelect(false);
        messageList.addValueChangeListener(event -> {
            //System.err.println(messageList.getValue());
            if (messageList.getValue() == null) {
                messageForm.form.setVisible(false);
                return;
            }
            messageForm.edit((Message) messageList.getValue());
        });
        messageList.setImmediate(true);
        float[] ratio = new float[]{4, 1f, 1};
        for (int i = 0; i < columns.length; i++) {
            messageList.setColumnExpandRatio(columns[i], ratio[i]);
        }

//        HorizontalLayout actions = new HorizontalLayout(newMessage);
        VerticalLayout left = new VerticalLayout(messageList);
        left.setSizeFull();
        messageList.setSizeFull();
        left.setExpandRatio(messageList, 1);

        HorizontalLayout mainLayout = new HorizontalLayout(left, messageForm);
        mainLayout.setSizeFull();
//        messageForm.setWidth("50%");
        mainLayout.setExpandRatio(left, 1);
        mainLayout.setExpandRatio(messageForm, 1);

        setCompositionRoot(mainLayout);
    }

    public void refresh() {
        messageList.refreshRowCache();
    }

    public void enter(ViewChangeEvent event) {

    }
}
