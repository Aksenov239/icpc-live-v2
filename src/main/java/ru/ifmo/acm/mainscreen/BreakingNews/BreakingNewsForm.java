package ru.ifmo.acm.mainscreen.BreakingNews;

import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.acm.backend.player.urls.TeamUrls;
import ru.ifmo.acm.mainscreen.MainScreenData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static ru.ifmo.acm.mainscreen.Utils.createGroupLayout;

public class BreakingNewsForm extends FormLayout {
    private static final Logger log = LogManager.getLogger(BreakingNewsForm.class);

    VerticalLayout form;

    final MainScreenBreakingNews parent;

    ComboBox predefinedMessages;
    TextField newPattern;
    Button save;

    Label breakingNewsStatus;
    CheckBox isLive;
    OptionGroup types;

    Label messageToShow;

    TextField teamProblem;
    // TextField problem;
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
        isLive.setValue(true);

        types = new OptionGroup();
        types.addItems(TeamUrls.types);
        types.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        types.setValue(TeamUrls.types[0]);
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
                log.error("error", e);
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
                    Files.write(patternsFile, (newPattern.getValue() + "\n").getBytes(), appendOrCreate);

                    predefinedMessages.addItems(newPattern.getValue());
                    newPattern.clear();
                } catch (IOException e) {
                    log.error("error", e);
                }
            }
        });

        CssLayout pattern = createGroupLayout(newPattern, save);

        newPattern.setSizeUndefined();

        teamProblem = new TextField("Team Problem");
        // problem = new TextField("Problem");
        time = new TextField("Time");

        outcomes = new ComboBox("Outcome");
        outcomes.addItems("AC", "WA", "TLE", "RTE", "PE", "CE", "Frozen");
        outcomes.setFilteringMode(FilteringMode.CONTAINS);
        outcomes.setNullSelectionAllowed(true);

        teamProblem.addValueChangeListener(event -> {
            predefinedMessages.clear();
        });
        // problem.addValueChangeListener(event -> updateMessageField());
        time.addValueChangeListener(event -> {
            predefinedMessages.clear();
        });

        outcomes.addValueChangeListener(event -> {
            predefinedMessages.clear();
        });

        HorizontalLayout parameters = new HorizontalLayout(teamProblem, time, outcomes);
        parameters.setSpacing(true);

        show = new Button("Show");
        show.addClickListener(event -> {
            if (teamProblem.getValue().equals("")) {
                Notification.show("It requires team id and problem id");
            } else {
                int teamId, problemId;
                try {
                    String[] zz = teamProblem.getValue().split(" ");
                    teamId = Integer.parseInt(zz[0]) - 1;
                    problemId = zz[1].charAt(0) - 'A';
                } catch (Exception e) {
                    Notification.show("Field Team Problem should contain id of the team and problem alias");
                    return;
                }

//                int teamId = Integer.parseInt(team.getValue()) - 1;
//                int problemId = problem.getValue().charAt(0) - 'A';
                updateMessageField();

                String outcome = parent.mainScreenData.breakingNewsData.setNewsVisible(
                        true, (String) types.getValue(), isLive.getValue(), messageToShow.getValue(),
                        teamId, problemId, currentRunId
                );

                if (outcome != null) {
                    Notification.show(
                            outcome,
                            Notification.Type.WARNING_MESSAGE
                    );
                } else {
                    teamProblem.clear();
                    outcomes.clear();
                    time.clear();
                    predefinedMessages.clear();
                    time.clear();

                    newPattern.setValue("");
                    currentRunId = -1;
                }

                breakingNewsStatus.setValue(getBreakingNewsStatus());
            }
        });

        hide = new Button("Hide");
        hide.addClickListener(event -> {
            String outcome =
                parent.mainScreenData.breakingNewsData.setNewsVisible(false, null, isLive.getValue(), "", -1, -1, -1);
            if (outcome != null) {
                Notification.show(
                        outcome,
                        Notification.Type.WARNING_MESSAGE
                );
            }
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
            if (predefinedMessages.getValue() == null) {
                newPattern.clear();
            } else {
                newPattern.setValue((String) predefinedMessages.getValue());
            }

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
            teamProblem.clear();
            time.clear();
            outcomes.clear();
            currentRunId = -1;
        } else {
            teamProblem.setValue(news.team + " " + news.getProblem());
//            team.setValue(String.valueOf(news.getTeam()));
//            problem.setValue(news.getProblem());
            time.setValue(String.valueOf(news.getTimestamp()));
            outcomes.setValue(news.getOutcome());
            currentRunId = news.getRunId();

            updateMessageField();
        }
    }

    public void updateMessageField() {
        String result = newPattern.isEmpty() ? (predefinedMessages.isEmpty() ? "" : (String) predefinedMessages.getValue()) : newPattern.getValue();
//        if (!team.isEmpty()) {
//            int teamId = Integer.parseInt(team.getValue()) - 1;
//
//            String teamName = (teamId == -1) ? "" : MainScreenData.getProperties().contestInfo.getParticipant(teamId).getName();
//            result = result.replace("%team", teamName);
//        }
//
//        if (!problem.isEmpty()) {
//            result = result.replace("%problem", problem.getValue());
//        }
        if (!teamProblem.isEmpty()) {
            try {
                String[] zz = teamProblem.getValue().split(" ");

                int teamId = Integer.parseInt(zz[0]) - 1;
                String teamName = (teamId == -1) ? "" : MainScreenData.getProperties().contestInfo.getParticipant(teamId).getName();
                result = result.replace("%team", teamName);

                result = result.replace("%problem", zz[1]);
            } catch (Exception e) {
                Notification.show("Incorrect team-problem pair", Notification.Type.WARNING_MESSAGE);
            }
        }

        if (!time.isEmpty()) {
            result = result.replace("%time", time.getValue());
        }

        if (!outcomes.isEmpty()) {
            result = result.replace("%outcome", (String) outcomes.getValue());
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
