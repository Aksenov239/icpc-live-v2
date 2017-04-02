package ru.ifmo.acm.mainscreen.Polls;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import ru.ifmo.acm.datapassing.PollData;
import ru.ifmo.acm.mainscreen.MainScreenData;

/**
 * Created by Aksenov239 on 12.03.2017.
 */
public class MainScreenPollView extends CustomComponent implements View {
    public static final String NAME = "pollManager";

    PollsData pollsData;
    Table pollTable;
    final BeanItemContainer<Poll> pollContainer;
    PollForm pollForm;

    PollData pollData;
    Label status;
    Button showPoll;

    public MainScreenPollView() {
        pollsData = PollsData.getInstance();
        pollForm = new PollForm(this);

        pollData = MainScreenData.getMainScreenData().pollData;
        status = new Label("Poll overlay status: ");
        showPoll = new Button("Show poll");
        showPoll.addClickListener(event -> {
            Poll pollToShow = (Poll) pollTable.getValue();
            if (pollToShow == null) {
                Notification.show("You need to choose the poll in the table", Notification.Type.WARNING_MESSAGE);
                return;
            }
            String result = pollData.setPollVisible(pollToShow);
            if (result != null) {
                Notification.show(result, Notification.Type.WARNING_MESSAGE);
            }
        });

        pollContainer = pollsData.pollList.getContainer();
        pollTable = new Table();
        pollTable.setContainerDataSource(pollContainer);
        String[] columns = {"question", "hashtag", "teamOptions"};
        pollTable.setVisibleColumns(columns);
        pollTable.setSelectable(true);
        pollTable.setMultiSelect(false);
        pollTable.addValueChangeListener(event -> {
            if (pollTable.getValue() == null) {
                pollForm.editForm.setVisible(false);
            } else {
                pollForm.edit((Poll) pollTable.getValue());
            }
        });
        pollTable.setImmediate(true);
        {
            float[] ratio = {6, 3, 1};
            for (int i = 0; i < ratio.length; i++) {
                pollTable.setColumnExpandRatio(columns[i], ratio[i]);
            }
        }

        VerticalLayout rightPart = new VerticalLayout(status, showPoll, pollForm);
        pollForm.setSizeFull();
        rightPart.setSpacing(true);
        rightPart.setMargin(true);

        HorizontalLayout mainLayout = new HorizontalLayout(pollTable, rightPart);
        mainLayout.setExpandRatio(pollTable, 0.5f);
        mainLayout.setExpandRatio(rightPart, 0.5f);

        pollTable.setSizeFull();
        rightPart.setSizeFull();
        mainLayout.setSizeFull();

        setCompositionRoot(mainLayout);
    }

    public void refresh() {
        status.setValue(pollData.toString());
    }

    public void enter(ViewChangeListener.ViewChangeEvent event) {
    }
}
