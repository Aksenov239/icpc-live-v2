package org.icpclive.mainscreen.Polls;

import com.vaadin.data.Item;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.icpclive.events.EventsLoader;

import java.util.ArrayList;

/**
 * Created by Aksenov239 on 13.03.2017.
 */
public class PollForm extends FormLayout {
    private final MainScreenPollView parent;

    private Button newPoll;

    private Button savePoll;
    private Button deletePoll;
    private Button cancelPoll;

    private Poll pollOnEdit;

    private TextField question;
    private TextField hashtag;

    private CheckBox teamOptions;

    private Table optionsTable;
    private Button addOption;
    private Button removeOption;
    private VerticalLayout optionsManager;

    VerticalLayout editForm;

    public PollForm(MainScreenPollView parent) {
        this.parent = parent;
        PollsData pollsData = PollsData.getInstance();

        newPoll = new Button("New Poll");
        newPoll.addClickListener(event -> {
            parent.pollTable.setValue(null);
            this.edit(null);
        });

        editForm = new VerticalLayout();
        editForm.setVisible(false);
        editForm.setMargin(true);

        savePoll = new Button("Save");
        savePoll.addClickListener(event -> {
            if (pollOnEdit == null) {
                Poll poll;
                if (teamOptions.getValue()) {
                    poll = new Poll(question.getValue(), hashtag.getValue(), true);
                } else {
                    ArrayList<String> optionsHashtags = new ArrayList<>();

                    for (Object id : optionsTable.getItemIds()) {
                        optionsHashtags.add((String) optionsTable.getContainerProperty(id, "Option").getValue());
                    }
                    poll = new Poll(question.getValue(), hashtag.getValue(), optionsHashtags.toArray(new String[0]));
                }
                pollsData.addPoll(poll);
            } else {
                synchronized (parent.pollContainer) {
                    Item pollBean = parent.pollContainer.getItem(pollOnEdit);
                    pollBean.getItemProperty("question").setValue(question.getValue());
                    if (!hashtag.getValue().equals(pollOnEdit.getHashtag())) {
                        pollsData.updateHashtag(pollOnEdit, hashtag.getValue());
                    }
                    pollBean.getItemProperty("hashtag").setValue(hashtag.getValue());
                    if (teamOptions.getValue()) {
                        if (!pollOnEdit.teamOptions) {
                            pollOnEdit.setOptions(EventsLoader.getInstance().getContestData().getHashTags());
                        }
                    } else {
                        ArrayList<String> optionsHashtags = new ArrayList<>();
                        for (Object id : optionsTable.getItemIds()) {
                            optionsHashtags.add((String) optionsTable.getContainerProperty(id, "Option").getValue());
                        }
                        pollOnEdit.setOptions(optionsHashtags.toArray(new String[0]));
                    }
                    pollBean.getItemProperty("teamOptions").setValue(teamOptions.getValue());
                }
            }
            editForm.setVisible(false);
            parent.pollTable.setValue(false);
            newPoll.setVisible(true);

            if (pollOnEdit == null) {
                Notification.show("New poll is created", Notification.Type.TRAY_NOTIFICATION);
            } else {
                Notification.show("The poll is edited", Notification.Type.TRAY_NOTIFICATION);
            }
        });
        savePoll.setStyleName(ValoTheme.BUTTON_PRIMARY);
        savePoll.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        deletePoll = new Button("Delete");
        deletePoll.addClickListener(event -> {
            pollsData.removePoll(pollOnEdit);
            editForm.setVisible(false);
            newPoll.setVisible(true);
            Notification.show("The poll is deleted", Notification.Type.TRAY_NOTIFICATION);
        });

        cancelPoll = new Button("Cancel");
        cancelPoll.addClickListener(event -> {
            parent.pollTable.setValue(false);
            editForm.setVisible(false);
            newPoll.setVisible(true);
            Notification.show("The editing is canceled", Notification.Type.TRAY_NOTIFICATION);
        });

        question = new TextField("Question");
        hashtag = new TextField("Question's hashtag");

        teamOptions = new CheckBox("Use teams hashtags as options");
        teamOptions.addValueChangeListener(event -> {
            if (teamOptions.getValue()) {
                optionsManager.setVisible(false);
                optionsTable.removeAllItems();
            } else {
                optionsManager.setVisible(true);
            }
        });

        optionsTable = new Table();
        optionsTable.addContainerProperty("Option", String.class, null);
        optionsTable.setEditable(true);
        optionsTable.setSelectable(true);
        optionsTable.setSizeFull();

        addOption = new Button("Add option");
        addOption.addClickListener(event -> {
            optionsTable.addItem();
        });
        removeOption = new Button("Remove option");
        removeOption.addClickListener(event -> {
            optionsTable.removeItem(optionsTable.getValue());
            optionsTable.setValue(false);
        });

        HorizontalLayout actions = new HorizontalLayout(newPoll, savePoll, deletePoll, cancelPoll);
        actions.setSpacing(true);

        HorizontalLayout optionsActions = new HorizontalLayout(addOption, removeOption);
        optionsActions.setSpacing(true);

        optionsManager = new VerticalLayout(optionsActions, optionsTable);
        optionsManager.setSizeFull();
        optionsManager.setSpacing(true);

        editForm.addComponents(actions, question, hashtag, teamOptions, optionsManager);
        editForm.setSizeFull();
        editForm.setSpacing(true);
        editForm.setVisible(false);

        addComponents(newPoll, editForm);

        setSizeFull();
        setVisible(true);
    }

    public void edit(Poll poll) {
        newPoll.setVisible(false);

        pollOnEdit = poll;
        editForm.setVisible(true);
        if (poll == null) {
            question.clear();
            hashtag.clear();
            optionsTable.removeAllItems();
            optionsManager.setVisible(true);
            teamOptions.clear();
        } else {
            question.setValue(poll.getQuestion());
            hashtag.setValue(poll.getHashtag());
            if (poll.teamOptions) {
                teamOptions.setValue(true);
                optionsManager.setVisible(false);
            } else {
                teamOptions.setValue(false);
                optionsTable.removeAllItems();
                synchronized (poll) {
                    for (String option : poll.options.keySet()) {
                        optionsTable.addItem(new Object[]{option}, option);
                    }
                }
            }
        }
    }
}
