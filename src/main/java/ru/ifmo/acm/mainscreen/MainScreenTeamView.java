package ru.ifmo.acm.mainscreen;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;
import ru.ifmo.acm.backend.player.widgets.TeamWidget;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class MainScreenTeamView extends CustomComponent implements View {
    public static String NAME = "mainscreen-team";

    Label teamStatus;
    Button teamShow;
    Button teamHide;
    final String[] types = TeamWidget.types;
    ComboBox type;
    //    ListSelect teamSelection;
    OptionGroup teamSelection;

    public static String getTeamStatus() {
        String status = MainScreenData.getMainScreenData().teamData.infoStatus();
        return Utils.getTeamStatus(status);
//        String[] z = status.split("\n");
//
//        if (z[1].equals("true")) {
//            for (String type1 : TeamWidget.types) {
//                if (type1.equals(z[2])) {
//                    return "Now showing " + z[2] + " of team " + z[3] + " for " + (System.currentTimeMillis() - Long.parseLong(z[0])) / 1000 + " seconds";
//                }
//            }
//            return "Some error happened";
//        } else {
//            return "No team view is shown";
//        }
    }

    public Component getControllerTeam() {
        teamStatus = new Label(getTeamStatus());

        type = new ComboBox();
        type.addItems(types);
        type.setNullSelectionAllowed(false);
        type.setValue(types[0]);

        /*type.addValueChangeListener(event -> {
            if (teamSelection.getValue() == null)
                return;
            if (mainScreenData.teamData.isVisible() &&
                    !mainScreenData.teamData.setInfoVisible(true, (String) type.getValue(), (String) teamSelection.getValue())) {
                type.setValue(mainScreenData.teamData.infoType);
                Notification.show("You need to wait 30 seconds first", Type.WARNING_MESSAGE);
            }
        });*/

        //teamSelection = new ListSelect();
        //teamSelection.addItems(mainScreenData.teamStatus.teamNames);
        //teamSelection.setNullSelectionAllowed(false);
        //teamSelection.setHeight("100%");
        //teamSelection.setSizeFull();
        //teamSelection.setRows(mainScreenData.teamStatus.teamNames.length);

        teamSelection = new OptionGroup();
        for (String name : MainScreenData.getProperties().teamNames) {
            teamSelection.addItem(name);
        }
        teamSelection.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        teamSelection.setWidth("100%");

        teamSelection.addValueChangeListener(event -> {
            if (mainScreenData.teamData.isVisible() &&
                    !mainScreenData.teamData.setInfoVisible(true, (String) type.getValue(), (String) teamSelection.getValue())) {
                teamSelection.setValue(mainScreenData.teamData.getTeamString());
                Notification.show("You need to wait 30 seconds first", Type.WARNING_MESSAGE);
            }
        });

        teamShow = new Button("Show info");
        teamShow.addClickListener(event -> {
            if (!mainScreenData.teamData.setInfoVisible(true, (String) type.getValue(), (String) teamSelection.getValue())) {
                Notification.show("You need to wait 30 seconds first", Type.WARNING_MESSAGE);
            }
        });

        teamHide = new Button("Hide info");
        teamHide.addClickListener(event -> {
            mainScreenData.teamData.setInfoVisible(false, null, null);
        });


        Component controlGroup = createGroupLayout(teamShow, teamHide, type);
        VerticalLayout result = new VerticalLayout(teamStatus, controlGroup, teamSelection);
        result.setSpacing(true);
        result.setSizeFull();
        result.setHeight("100%");
        result.setComponentAlignment(teamStatus, Alignment.MIDDLE_CENTER);
        result.setComponentAlignment(controlGroup, Alignment.MIDDLE_CENTER);

        return result;
    }

    /* Main screen */
    MainScreenData mainScreenData;

    public MainScreenTeamView() {
        mainScreenData = MainScreenData.getMainScreenData();

        Component main = getControllerTeam();
        main.setSizeFull();
        main.setHeight("100%");

        setCompositionRoot(main);
    }

    public void refresh() {
        teamStatus.setValue(getTeamStatus());
//        mainScreenData.update();
    }

    public void enter(ViewChangeEvent event) {

    }


    private CssLayout createGroupLayout(Component... components) {
        CssLayout layout = new CssLayout();
        layout.addStyleName("v-component-group");
        layout.addComponents(components);

        return layout;
    }
}
