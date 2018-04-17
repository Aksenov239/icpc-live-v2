package org.icpclive.webadmin.creepingline;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.AlignmentInfo;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import org.icpclive.events.ContestInfo;
import org.icpclive.webadmin.mainscreen.MainScreenData;
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

    Label creepingLineStatus;
    Button showCreepingLine;
    Button hideCreepingLine;

    public Component creepingLineVisibility() {
        creepingLineStatus = new Label(getVisibilityStatus());

        showCreepingLine = createVisibilityButton("Show creeping line", true, 1);
        hideCreepingLine = createVisibilityButton("Hide creeping line", false, 0);

        CssLayout group = Utils.createGroupLayout(showCreepingLine, hideCreepingLine);
        VerticalLayout panel = new VerticalLayout(creepingLineStatus, group);

        Utils.setPanelDefaults(panel);

        return panel;
    }

    public String getVisibilityStatus() {
        return MessageData.getMessageData().isVisible() ? "Creeping line is shown"
                : "Creeping line is not shown";
    }

    public Button createVisibilityButton(String label, boolean visibility, int status) {
        Button button = new Button(label);
        button.addClickListener(e -> {
            MessageData.getMessageData().setVisible(visibility);
            creepingLineStatus.setValue(getVisibilityStatus());
        });
        return button;
    }

    final MessageData messageData;
    final MessageForm messageForm;
    final BeanItemContainer<Message> messageListContainer;
    final BeanItemContainer<Message> messageFlowContainer;
    Table messageList;
    Table messageFlow;

    /* Fact data */
    Label factStatus;
    TextField factTitle;
    TextField factText;
    Button factShow;
    Button factHide;

    public Component getFactController() {
        factStatus = new Label();
        factTitle = new TextField("Title:");
        factText = new TextField("Text:");
        factShow = new Button("Show fact");
        factHide = new Button("Hide fact");

        factStatus.setValue(MainScreenData.getMainScreenData().factData.toString());

        factShow.addClickListener(event -> {
            String result =
                    MainScreenData.getMainScreenData().
                            factData.show(factTitle.getValue(), factText.getValue());
            if (result != null) {
                Notification.show(result, Notification.Type.WARNING_MESSAGE);
            }
        });

        factHide.addClickListener(event -> {
            MainScreenData.getMainScreenData().factData.hide();
        });

        HorizontalLayout actions = new HorizontalLayout(factShow, factHide);

        VerticalLayout component = new VerticalLayout(
                factStatus,
                factTitle,
                factText,
                actions
        );

        component.setMargin(new MarginInfo(false, false, true, false));
        factTitle.setSizeFull();
        factText.setSizeFull();
        return component;
    }


    public CreepingLineView() {
        Component creepingLineVisibility = creepingLineVisibility();

        messageData = MessageData.getMessageData();
        messageForm = new MessageForm(this);

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
                messageForm.setVisible(false);
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

        messageFlow.addValueChangeListener(event -> {
            if (messageFlow.getValue() != null) {
                Message message = (Message) messageFlow.getValue();
                factTitle.setValue(message.getSource());
                factText.setValue(message.getMessage());
                messageForm.editFromFlow(message);
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

        HorizontalLayout buttonAndForm =
                new HorizontalLayout(creepingLineVisibility, messageForm.component);
        buttonAndForm.setSpacing(true);
        messageForm.component.setSizeFull();

        GridLayout messageFlowButtons = new GridLayout(3, 1);
        messageFlowButtons.addComponent(sourceFilter, 0, 0);
        messageFlowButtons.setComponentAlignment(sourceFilter,
                new Alignment(AlignmentInfo.Bits.ALIGNMENT_HORIZONTAL_CENTER | AlignmentInfo.Bits.ALIGNMENT_BOTTOM));
        messageFlowButtons.addComponent(load, 1, 0);
        messageFlowButtons.setComponentAlignment(load,
                new Alignment(AlignmentInfo.Bits.ALIGNMENT_HORIZONTAL_CENTER | AlignmentInfo.Bits.ALIGNMENT_BOTTOM));
        messageFlowButtons.addComponent(loadButton, 2, 0);
        messageFlowButtons.setComponentAlignment(loadButton,
                new Alignment(AlignmentInfo.Bits.ALIGNMENT_HORIZONTAL_CENTER | AlignmentInfo.Bits.ALIGNMENT_BOTTOM));

        VerticalLayout messageFlowLayout = new VerticalLayout(
                messageFlowButtons,
                messageFlow
        );
        messageFlow.setSizeFull();
        messageFlowLayout.setSizeFull();

        VerticalLayout left = new VerticalLayout(creepingLineVisibility, messageFlowLayout);
        left.setSizeFull();

        Component factController = getFactController();
        VerticalLayout right = new VerticalLayout(
                factController, messageForm.component, messageList);
        messageList.setSizeFull();
        right.setSizeFull();

        HorizontalLayout mainLayout = new HorizontalLayout(left, right);
        mainLayout.setExpandRatio(left, 0.6f);
        mainLayout.setExpandRatio(right, 0.4f);

        mainLayout.setSizeFull();

//        HorizontalLayout messageStorages =
//                new HorizontalLayout(messageList, messageFlowLayout);
//        messageStorages.setSizeFull();
//
//        messageList.setSizeFull();
//        messageFlowLayout.setSizeFull();
//        messageStorages.setExpandRatio(messageList, 0.4f);
//        messageStorages.setExpandRatio(messageFlowLayout, 0.6f);
//
//        VerticalLayout mainLayout = new VerticalLayout(buttonAndForm,
//                messageStorages);
//        buttonAndForm.setSizeFull();
//        mainLayout.setSizeFull();

//        messageForm.form.setVisible(false);

        setCompositionRoot(mainLayout);
    }

    public void refresh() {
        messageList.refreshRowCache();
        factStatus.setValue(MainScreenData.getMainScreenData().factData.toString());
    }

    public void enter(ViewChangeEvent event) {

    }
}
