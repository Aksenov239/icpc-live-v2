package ru.ifmo.acm.mainscreen.BreakingNews;

import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import ru.ifmo.acm.backend.player.widgets.TeamWidget;
import ru.ifmo.acm.mainscreen.MainScreenData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static ru.ifmo.acm.mainscreen.Utils.createGroupLayout;

public class BreakingNewsForm extends FormLayout {
    VerticalLayout form;

    final MainScreenBreakingNews parent;

    ComboBox predefinedMessages;
    TextField newPattern;
    Button save;

    Label breakingNewsStatus;
    CheckBox isLive;
    OptionGroup types;

    Label messageToShow;

    TextField team;
    TextField problem;
    ComboBox outcomes;
    TextField time;

    Button show;
    Button hide;

    int currentRunId;

    public BreakingNewsForm(MainScreenBreakingNews parent) {
        this.parent = parent;

        breakingNewsStatus = new Label(getBreakingNewsStatus());
        breakingNewsStatus.addStyleName("large");

        isLive = new CheckBox("Is live");
        isLive.addValueChangeListener(event -> {
            types.setEnabled(isLive.getValue());
            breakingNewsStatus.setValue(getBreakingNewsStatus());
        });
        isLive.setValue(false);

        types = new OptionGroup();
        types.addItems(TeamWidget.types);
        types.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        types.setValue(TeamWidget.types[0]);
        types.setEnabled(false);

        messageToShow = new Label("Message");

        predefinedMessages = new ComboBox("Patterns");
        predefinedMessages.setFilteringMode(FilteringMode.CONTAINS);
        predefinedMessages.setTextInputAllowed(true);


        Path patternsFile = Paths.get(MainScreenData.getProperties().breakingNewsPatternsFilename);
        if (Files.exists(patternsFile)) {
            try {
                Files.readAllLines(patternsFile).forEach(s -> predefinedMessages.addItems(s));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        newPattern = new TextField("New pattern");
        newPattern.setValue("");
        save = new Button("Save");
        save.addClickListener(event -> {
            if (newPattern.isEmpty()) {
                Notification.show("Message should not be empty");
            } else {
                try {
                    StandardOpenOption appendOrCreate = Files.exists(patternsFile) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE;
                    Files.write(patternsFile, newPattern.getValue().getBytes(), appendOrCreate);

                    predefinedMessages.addItems(newPattern.getValue());
                    newPattern.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        CssLayout pattern = createGroupLayout(newPattern, save);

        newPattern.setSizeUndefined();

        team = new TextField("Team");
        problem = new TextField("Problem");
        time = new TextField("Time");

        outcomes = new ComboBox("Outcome");
        outcomes.addItems("AC", "WA", "TL", "RE", "PE", "CE", "Frozen");
        outcomes.setFilteringMode(FilteringMode.CONTAINS);
        outcomes.setNullSelectionAllowed(true);

        team.addValueChangeListener(event -> updateMessageField());
        problem.addValueChangeListener(event -> updateMessageField());
        time.addValueChangeListener(event -> updateMessageField());

        outcomes.addValueChangeListener(event -> updateMessageField());

        HorizontalLayout parameters = new HorizontalLayout(team, problem, time, outcomes);
        parameters.setSpacing(true);

        show = new Button("Show");
        show.addClickListener(event -> {
            if (team.getValue().equals("") || problem.getValue().equals("")) {
                Notification.show("It requires team id and problem id");
            } else {
                int teamId = Integer.parseInt(team.getValue()) - 1;
                int problemId = problem.getValue().charAt(0) - 'A';
                updateMessageField();

                boolean isSet = parent.mainScreenData.breakingNewsData.setNewsVisible(
                        true, (String) types.getValue(), isLive.getValue(), messageToShow.getValue(),
                        teamId, problemId, currentRunId
                );

                if (!isSet) {
                    Notification.show(
                            String.format("You need to wait while current breaking news is shown"),
                            Notification.Type.WARNING_MESSAGE
                    );
                } else {
                    team.clear();
                    problem.clear();
                    outcomes.clear();
                    predefinedMessages.clear();

                    newPattern.setValue("");
                }

                breakingNewsStatus.setValue(getBreakingNewsStatus());
            }
        });

        hide = new Button("Hide");
        hide.addClickListener(event -> {
            parent.mainScreenData.breakingNewsData.setNewsVisible(false, null, isLive.getValue(), "", -1, -1, -1);
            breakingNewsStatus.setValue(getBreakingNewsStatus());
        });

        HorizontalLayout actions = new HorizontalLayout(show, hide);
        actions.setSpacing(true);

        form = new VerticalLayout();
        form.setMargin(true);

        form.addComponents(breakingNewsStatus, isLive, types, messageToShow, predefinedMessages, pattern, parameters, actions);

        pattern.setSizeFull();
        predefinedMessages.setSizeFull();
        predefinedMessages.addValueChangeListener(event -> {
            newPattern.setValue((String) predefinedMessages.getValue());

            updateMessageField();
        });

        isLive.setSizeUndefined();

        form.setSizeFull();
        form.setSpacing(true);
        form.setVisible(true);

        addComponents(form);

        setSizeFull();
        setVisible(true);
    }

    public void update(BreakingNews news) {
        if (news == null) {
            team.clear();
            problem.clear();
            time.clear();
            outcomes.clear();
        } else {
            team.setValue(String.valueOf(news.getTeam()));
            problem.setValue(news.getProblem());
            time.setValue(String.valueOf(news.getTimestamp()));
            outcomes.setValue(news.getOutcome());
            currentRunId = news.getRunId();

            updateMessageField();
        }
    }

    public void updateMessageField() {
        String result = newPattern.isEmpty() ? (predefinedMessages.isEmpty() ? "" : (String)predefinedMessages.getValue()) : newPattern.getValue();
        if (!team.isEmpty()) {
            int teamId = Integer.parseInt(team.getValue()) - 1;

            String teamName = (teamId == -1) ? "" : MainScreenData.getProperties().contestInfo.getParticipant(teamId).getName();
            result = result.replace("%team", teamName);
        }

        if (!problem.isEmpty()) {
            result = result.replace("%problem", problem.getValue());
        }

        if (!time.isEmpty()) {
            result = result.replace("%time", time.getValue());
        }

        if (!outcomes.isEmpty()) {
            result = result.replace("%outcome", (String)outcomes.getValue());
        }

        messageToShow.setValue(result);
    }

    public void refresh() {
        breakingNewsStatus.setValue(getBreakingNewsStatus());
    }

    public String getBreakingNewsStatus() {
        return MainScreenData.getMainScreenData().breakingNewsData.getStatus();
    }
}
