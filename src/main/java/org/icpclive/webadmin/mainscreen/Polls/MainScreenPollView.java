package org.icpclive.webadmin.mainscreen.Polls;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import org.icpclive.webadmin.mainscreen.MainScreenData;
import org.icpclive.datapassing.PollData;
import org.icpclive.webadmin.mainscreen.Utils;

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
    Button hidePoll;

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

        hidePoll = new Button("Hide poll");
        hidePoll.addClickListener(event -> {
            pollData.hide();
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
                pollForm.freePoll();
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

        Component controller = Utils.createGroupLayout(showPoll, hidePoll);
        VerticalLayout rightPart = new VerticalLayout(status, controller, pollForm);
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
