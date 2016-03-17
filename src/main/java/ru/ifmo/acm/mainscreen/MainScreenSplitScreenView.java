package ru.ifmo.acm.mainscreen;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import ru.ifmo.acm.backend.player.widgets.TeamWidget;

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

        Component main = new VerticalLayout(
                new HorizontalLayout(controllers[0], controllers[1]),
                new HorizontalLayout(controllers[2], controllers[3])
        );

        main.setSizeFull();

        setCompositionRoot(main);
    }

    public static String getTeamStatus(int controllerId) {
        String status = MainScreenData.getMainScreenData().splitScreenData.infoStatus(controllerId);

        return Utils.getTeamStatus(status);
    }

    public Component getOneControllerTeam(int id) {
        labels[id] = new Label("Controller " + (id + 1) + "(" + getTeamStatus(id) + ")");
        automated[id] = new CheckBox("Automated");

        types[id] = new OptionGroup();
        types[id].addItems(TeamWidget.types);
        types[id].setNullSelectionAllowed(false);
        types[id].setValue(TeamWidget.types[0]);
        types[id].addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        types[id].setWidth("50%");

        teams[id] = new TextField("Team: ");

        shows[id] = new Button("Show");
        shows[id].addClickListener(event -> {
            mainScreenData.splitScreenData.isAutomatic[id] = !automated[id].isEmpty();
            if (!automated[id].isEmpty()) {
                Notification.show("You can not use this button in automatic mode");
            } else {
                int teamId = Integer.parseInt(teams[id].getValue());
                String teamName = mainScreenData.getProperties().contestInfo.getParticipant(teamId).getName();
                if (!mainScreenData.splitScreenData.setInfoVisible(id, true, (String) types[id].getValue(), teamName)) {
                    Notification.show("You need to wait 30 seconds first", Notification.Type.WARNING_MESSAGE);
                }
            }}
        );

        hides[id] = new Button("Hide");
        hides[id].addClickListener(event -> {
            if (!automated[id].isEmpty()) {
                Notification.show("You can not use this button in automatic mode");
            } else {
                mainScreenData.splitScreenData.setInfoVisible(id, false, null, null);
            }}
        );

        Component team = createGroupLayout(teams[id], shows[id], hides[id]);
        VerticalLayout result = new VerticalLayout(labels[id], automated[id], types[id], team);
        result.setSpacing(false);

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
