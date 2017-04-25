package ru.ifmo.acm.creepingline;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.mainscreen.Utils;

/**
 * Created by Aksenov239 on 14.11.2015.
 */
public class CreepingLineView extends CustomComponent implements View {
    public static final String NAME = "creepingLine";

    final MessageData messageData;
    final MessageForm messageForm;
    final BeanItemContainer<Message> messageListContainer;
    final BeanItemContainer<Message> messageFlowContainer;
    Table messageList;
    Table messageFlow;
    ContestInfo contestInfo;
//    Button newMessage;

    public CreepingLineView() {
        messageData = MessageData.getMessageData();
        messageForm = new MessageForm(this);

//        newMessage = new Button("New Message");

//        newMessage.addClickListener(event -> {
//            messageList.setValue(null);
//            messageForm.edit(null);
//        });

        messageListContainer = messageData.messageList.getContainer();
        messageList = new Table();
        messageList.setContainerDataSource(messageListContainer);
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
        {
            float[] ratio = new float[]{4, 1f, 1};
            for (int i = 0; i < columns.length; i++) {
                messageList.setColumnExpandRatio(columns[i], ratio[i]);
            }
        }

        messageFlow = new Table();
        messageFlowContainer = messageData.messageFlow;
        messageFlow.setContainerDataSource(messageFlowContainer);
        String[] messageFlowColumns = {"source", "message"};
        messageFlow.setVisibleColumns(messageFlowColumns);
        messageFlow.setSelectable(true);
        messageFlow.setMultiSelect(false);
        messageFlow.addValueChangeListener(event -> {
            if (messageFlow.getValue() != null) {
                messageForm.editFromFlow((Message) messageFlow.getValue());
            }
        });
        messageFlow.setImmediate(true);
        {
            float[] ratio = new float[]{1, 3f};
            for (int i = 0; i < messageFlowColumns.length; i++) {
                messageFlow.setColumnExpandRatio(messageFlowColumns[i], ratio[i]);
            }
        }
//        HorizontalLayout actions = new HorizontalLayout(newMessage);

//        TwitterStreamQueryForm twitterQueryForm = new TwitterStreamQueryForm(TwitterLoader.getInstance().getTwitterQueryString());
//        TwitterSearchForm twitterSearchForm = new TwitterSearchForm();
        VerticalLayout left = new VerticalLayout(messageList, /*twitterQueryForm, twitterSearchForm,*/ messageFlow);
        left.setSizeFull();
        messageList.setSizeFull();
        messageFlow.setSizeFull();
//        twitterQueryForm.setSizeFull();
//        twitterSearchForm.setSizeFull();
        left.setExpandRatio(messageList, 1);
        left.setExpandRatio(messageFlow, 1);
//        left.setExpandRatio(twitterQueryForm, 1);
//        left.setExpandRatio(twitterSearchForm, 1);

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

    class TwitterStreamQueryForm extends FormLayout {
        private final TextField field;

        public TwitterStreamQueryForm(String query) {
            field = new TextField("Query", query);
//            field.add(new ShortcutListener("Enter", ShortcutAction.KeyCode.ENTER, null) {
//                @Override
//                public void handleAction(Object sender, Object target) {
//                    changeQuery();
//                }
//            });
            Button apply = new Button("Apply", event -> {
                changeQuery();
            });
            CssLayout layout = Utils.createGroupLayout(field, apply);
//            layout.setExpandRatio(field, 4);
//            layout.setExpandRatio(apply, 1);
            field.setSizeFull();
//            field.set
            addComponent(layout);
            layout.setSizeFull();
        }

        private void changeQuery() {
            synchronized (field) {
                TwitterLoader.changeStreamInInstance(field.getValue());
            }
        }
    }
    class TwitterSearchForm extends FormLayout {
        private final TextField field;

        public TwitterSearchForm() {
            field = new TextField("Search", "");
//            field.addShortcutListener(new ShortcutListener("Enter", ShortcutAction.KeyCode.ENTER, null) {
//                @Override
//                public void handleAction(Object sender, Object target) {
//                    searchQuery();
//                }
//            });
            Button apply = new Button("Apply", event -> {
                searchQuery();
            });
            CssLayout layout = Utils.createGroupLayout(field, apply);
//            layout.setExpandRatio(field, 4);
//            layout.setExpandRatio(apply, 1);
            field.setSizeFull();
//            field.set
            addComponent(layout);
            layout.setSizeFull();
        }

        private void searchQuery() {
            synchronized (field) {
                TwitterLoader.getInstance().addSearch(field.getValue());
            }
        }
    }
}
