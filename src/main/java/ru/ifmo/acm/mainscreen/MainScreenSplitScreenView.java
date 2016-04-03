package ru.ifmo.acm.mainscreen;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import ru.ifmo.acm.backend.player.widgets.TeamWidget;
import ru.ifmo.acm.events.TeamInfo;

/**
 * Created by forgotenn on 3/16/16.
 */
public class MainScreenSplitScreenView extends com.vaadin.ui.CustomComponent implements View {
    public final static String NAME = "mainscreen-split";
    Label[] labels = new Label[4];
    CheckBox[] automated = new CheckBox[4];
    OptionGroup[] types = new OptionGroup[4];
    TextField[] teams = new TextField[4];
    Button[] shows = new Button[4];
    Button[] hides = new Button[4];

    MainScreenData mainScreenData;

    public MainScreenSplitScreenView() {
        mainScreenData = MainScreenData.getMainScreenData();

        Component[] controllers = new Component[4];
        for (int i = 0; i < 4; i++) {
            controllers[i] = getOneControllerTeam(i);
        }

        HorizontalLayout row1 = new HorizontalLayout(controllers[0], controllers[1]);
        row1.setSizeFull();
        row1.setComponentAlignment(controllers[0], Alignment.MIDDLE_CENTER);
        row1.setComponentAlignment(controllers[1], Alignment.MIDDLE_CENTER);
        HorizontalLayout row2 = new HorizontalLayout(controllers[2], controllers[3]);
        row2.setSizeFull();
        row2.setComponentAlignment(controllers[2], Alignment.MIDDLE_CENTER);
        row2.setComponentAlignment(controllers[3], Alignment.MIDDLE_CENTER);
        VerticalLayout main = new VerticalLayout(
                row1,
                row2
        );
        row1.setMargin(true);
        row2.setMargin(true);

        main.setSizeFull();
        main.setMargin(true);
        main.setSpacing(true);
        main.setComponentAlignment(row1, Alignment.MIDDLE_CENTER);
        main.setComponentAlignment(row2, Alignment.MIDDLE_CENTER);

        setCompositionRoot(main);
    }

    public static String getTeamStatus(int controllerId) {
        String status = MainScreenData.getMainScreenData().splitScreenData.infoStatus(controllerId);

        return Utils.getTeamStatus(status);
    }

    public Component getOneControllerTeam(int id) {
        labels[id] = new Label("Controller " + (id + 1) + " (" + getTeamStatus(id) + ")");
        automated[id] = new CheckBox("Automated");
        automated[id].setValue(true);

        automated[id].addValueChangeListener(event -> {
            boolean auto = automated[id].getValue();
            types[id].setValue(auto ? null : TeamWidget.types[0]);
            types[id].setEnabled(!auto);

            shows[id].setEnabled(!auto);
            hides[id].setEnabled(!auto);
        });

        types[id] = new OptionGroup();
        types[id].addItems(TeamWidget.types);
        types[id].setNullSelectionAllowed(false);
        types[id].setValue(TeamWidget.types[0]);
        types[id].addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        types[id].setWidth("50%");
        types[id].setEnabled(false);

        teams[id] = new TextField("Team: ");
        teams[id].setSizeFull();

        shows[id] = new Button("Show");
        shows[id].addClickListener(event -> {
                    mainScreenData.splitScreenData.isAutomatic[id] = !automated[id].isEmpty();
//                    if (!automated[id].isEmpty()) {
//                        Notification.show("You can not use this button in automatic mode");
//                    } else {
                    try {
                        int teamId = Integer.parseInt(teams[id].getValue());
                        TeamInfo team = MainScreenData.getProperties().contestInfo.getParticipant(teamId);
                        if (team == null) {
                            Notification.show("There is no team with id " + teamId);
                        }
                        if (!mainScreenData.splitScreenData.setInfoVisible(id, true, (String) types[id].getValue(), team)) {
                            Notification.show("You need to wait 30 seconds first", Notification.Type.WARNING_MESSAGE);
                        }
                    } catch (NumberFormatException e) {

                    }
                    //    }
                }
        );
        shows[id].setEnabled(false);

        hides[id] = new Button("Hide");
        hides[id].addClickListener(event -> {
                    if (!automated[id].isEmpty()) {
                        Notification.show("You can not use this button in automatic mode");
                    } else {
                        mainScreenData.splitScreenData.setInfoVisible(id, false, null, null);
                    }
                }
        );                          hides[id].setEnabled(false);

        CssLayout team = createGroupLayout(teams[id], shows[id], hides[id]);
        VerticalLayout result = new VerticalLayout(labels[id], automated[id], types[id], team);
        result.setSpacing(true);

        return result;
    }

    public void refresh() {
        for (int i = 0; i < 4; i++) {
            labels[i].setValue("Controller " + (i + 1) + " (" + getTeamStatus(i) + ")");
        }
    }

    private CssLayout createGroupLayout(Component... components) {
        CssLayout layout = new CssLayout();
        layout.addStyleName("v-component-group");
        layout.addComponents(components);

        return layout;
    }

    @Override
    public void enter(ViewChangeEvent event) {

    }
}
