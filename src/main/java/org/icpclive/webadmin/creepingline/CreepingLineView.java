package org.icpclive.webadmin.creepingline;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import org.icpclive.events.ContestInfo;
import org.icpclive.webadmin.mainscreen.loaders.TwitterLoader;
import org.icpclive.webadmin.mainscreen.Utils;
import twitter4j.TwitterException;

import java.util.Arrays;
import java.util.stream.Stream;

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
        messageFlow.setEditable(false);
        messageFlow.setSizeFull();
        messageFlow.setWidth("1800px");
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

        TextField sourceFilter = new TextField("Filter source");
        sourceFilter.addTextChangeListener(e -> {
            messageFlowContainer.removeAllContainerFilters();
            messageFlowContainer.addContainerFilter("source", e.getText(), true, true);
        });

        TextField load = new TextField("Load field");
        Button loadButton = new Button("Load");
        loadButton.addClickListener(e -> {
            String text = load.getValue();
            Arrays.stream(text.split(","))
                    .flatMap(q -> {
                                try {
                                    return TwitterLoader.getInstance().loadByQuery(q).stream();
                                } catch (TwitterException exception) {
                                    Notification.show("TwitterException: " + exception.getMessage(), Notification.Type.TRAY_NOTIFICATION);
                                    return Stream.empty();
                                }
                            }
                    )
                    .forEach(MessageData::processTwitterMessage);
        });

//        Button applySourceFilter = new Button("Apply filter");
//        Button withdrawSourceFilter = new Button("Withdraw filter");
//
//        applySourceFilter.addClickListener(e -> {
//            messageFlowContainer.removeAllContainerFilters();
//            messageFlowContainer.addContainerFilter("source", sourceFilter.getValue(), true, true);
//        });

//        withdrawSourceFilter.addClickListener(e -> {
//            messageFlowContainer.removeAllContainerFilters();
//        });

        Panel messageFlowPanel = new Panel(messageFlow);

        VerticalLayout left = new VerticalLayout(messageList,
                sourceFilter,
                Utils.createGroupLayout(load, loadButton),
        /*twitterQueryForm, twitterSearchForm,*/
                messageFlowPanel
        );
//        left.setSizeFull();
        messageList.setSizeFull();
//        messageFlowPanel.setSizeFull();
//        left.setExpandRatio(messageList, 1);
//        left.setExpandRatio(messageFlow, 1);

        HorizontalLayout mainLayout = new HorizontalLayout(left, messageForm);
        mainLayout.setSizeFull();
//        messageForm.setWidth("50%");
//        mainLayout.setExpandRatio(left, 1);
//        mainLayout.setExpandRatio(messageForm, 1);

        setCompositionRoot(mainLayout);
    }

    public void refresh() {
        messageList.refreshRowCache();
    }

    public void enter(ViewChangeEvent event) {

    }
}
