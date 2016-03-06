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

    private String getTeamStatus() {
        String status = mainScreenData.teamStatus.infoStatus();
        String[] z = status.split("\n");

        if (z[1].equals("true")) {
            for (int i = 0; i < types.length; i++) {
                if (types[i].equals(z[2])) {
                    return "Now showing " + z[2] + " of team " + z[3] + " for " + (System.currentTimeMillis() - Long.parseLong(z[0])) / 1000 + " seconds";
                }
            }
            return "Some error happened";
        } else {
            return "No team view is shown";
        }
    }

    public Component getControllerTeam() {
        teamStatus = new Label(getTeamStatus());

        type = new ComboBox();
        type.addItems(types);
        type.setNullSelectionAllowed(false);
        type.setValue(types[0]);

        //teamSelection = new ListSelect();
        //teamSelection.addItems(mainScreenData.teamStatus.teamNames);
        //teamSelection.setNullSelectionAllowed(false);
        //teamSelection.setHeight("100%");
        //teamSelection.setSizeFull();
        //teamSelection.setRows(mainScreenData.teamStatus.teamNames.length);

        teamSelection = new OptionGroup();
        for (String name : mainScreenData.teamStatus.teamNames) {
            teamSelection.addItem(name);
        }
        teamSelection.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        teamSelection.setWidth("100%");

        teamSelection.addValueChangeListener(event -> {
            if (mainScreenData.teamStatus.isVisible() &&
                    !mainScreenData.teamStatus.setInfoVisible(true, (String) type.getValue(), (String) teamSelection.getValue())) {
                teamSelection.setValue(mainScreenData.teamStatus.getTeamString());
                Notification.show("You need to wait 30 seconds first", Type.WARNING_MESSAGE);
            }
        });

        teamShow = new Button("Show info");
        teamShow.addClickListener(event -> {
            if (!mainScreenData.teamStatus.setInfoVisible(true, (String) type.getValue(), (String) teamSelection.getValue())) {
                Notification.show("You need to wait 30 seconds first", Type.WARNING_MESSAGE);
            }
        });

        teamHide = new Button("Hide info");
        teamHide.addClickListener(event -> {
            mainScreenData.teamStatus.setInfoVisible(false, null, null);
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

        Component teamInfoComponent = getControllerTeam();

        Component main = teamInfoComponent;
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
