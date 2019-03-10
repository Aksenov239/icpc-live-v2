package org.icpclive.webadmin.mainscreen;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import net.egork.teaminfo.data.Team;
import org.icpclive.events.TeamInfo;

import java.util.Set;

public class MainScreenPvPView extends CustomComponent implements View {
    public static String NAME = "mainscreen-pvp";

    MainScreenData mainScreenData;

    Label status;

    OptionGroup teamSelection;

    Button show;
    Button hide;

    Label firstTeam;
    Label secondTeam;

    public Component getController() {
        status = new Label();
        status.setValue(mainScreenData.pvpData.getStatus());

        teamSelection = new OptionGroup();
        teamSelection.setMultiSelect(true);
        teamSelection.setHtmlContentAllowed(true);
        teamSelection.addStyleName("team-optiongroup");

        Set<String> topTeamsIds = MainScreenProperties.topteamsids;
        for (TeamInfo team : MainScreenData.getProperties().teamInfos) {
            teamSelection.addItem(team);
            String teamHtml = topTeamsIds.contains(team.getAlias()) ?
                    "<b>" + team.toString() + "</b>" : team.toString();
            teamSelection.setItemCaption(team, teamHtml);
        }
        teamSelection.setValue(MainScreenData.getProperties().teamInfos[0]);
        teamSelection.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        teamSelection.setWidth("100%");

        teamSelection.addValueChangeListener(event -> {
            Set<TeamInfo> selection = (Set<TeamInfo>) teamSelection.getValue();
            Set<TeamInfo> left = mainScreenData.pvpData.setTeams(selection);
            if (selection.size() >= 3) {
                teamSelection.setValue(left);
                Notification.show("You can choose only 2 teams", Notification.Type.WARNING_MESSAGE);
            }
            firstTeam.setValue(mainScreenData.pvpData.getTeam(0));
            secondTeam.setValue(mainScreenData.pvpData.getTeam(1));
        });

        show = new Button("Show");
        show.setStyleName(ValoTheme.BUTTON_PRIMARY);
        show.addClickListener(e -> {
            String outcome = mainScreenData.pvpData.setVisible();
            if (outcome != null) {
                Notification.show(outcome, Notification.Type.WARNING_MESSAGE);
            }
        });
        hide = new Button("Hide");
        hide.addClickListener(e -> {
            mainScreenData.pvpData.hide();
        });

        firstTeam = new Label(mainScreenData.pvpData.getTeam(0), ContentMode.HTML);
        secondTeam = new Label(mainScreenData.pvpData.getTeam(1), ContentMode.HTML);
        firstTeam.setWidthUndefined();
        secondTeam.setWidthUndefined();

        Component buttonGroup = createGroupLayout(show, hide);
        VerticalLayout firstTeamComponent = new VerticalLayout(firstTeam);
        firstTeamComponent.setMargin(true);
        firstTeamComponent.setComponentAlignment(firstTeam, Alignment.MIDDLE_RIGHT);
        firstTeamComponent.setSizeFull();
        firstTeamComponent.setWidth("100%");
        VerticalLayout secondTeamComponent = new VerticalLayout(secondTeam);
        secondTeamComponent.setMargin(true);
        secondTeamComponent.setComponentAlignment(secondTeam, Alignment.MIDDLE_LEFT);
        secondTeamComponent.setSizeFull();
        secondTeamComponent.setWidth("100%");
        HorizontalLayout teamGroup = new HorizontalLayout(firstTeamComponent, secondTeamComponent);
        teamGroup.setSizeFull();
        teamGroup.setWidth("100%");
//        teamGroup.setComponentAlignment(firstTeam, Alignment.MIDDLE_RIGHT);
//        teamGroup.setComponentAlignment(secondTeam, Alignment.MIDDLE_LEFT);
        VerticalLayout result = new VerticalLayout(
                status,
                buttonGroup,
                teamGroup,
                teamSelection
        );
        result.setSizeFull();
        result.setHeight("100%");
        result.setComponentAlignment(buttonGroup, Alignment.MIDDLE_CENTER);
        result.setComponentAlignment(teamGroup, Alignment.MIDDLE_CENTER);
        return result;
    }

    public MainScreenPvPView() {
        mainScreenData = MainScreenData.getMainScreenData();

        Component main = getController();
        main.setSizeFull();
        main.setHeight("100%");

        setCompositionRoot(main);
    }

    public void refresh() {
        status.setValue(mainScreenData.pvpData.getStatus());
    }

    public void enter(ViewChangeListener.ViewChangeEvent event) {
    }

    private CssLayout createGroupLayout(Component... components) {
        CssLayout layout = new CssLayout();
        layout.addStyleName("v-component-group");
        layout.addComponents(components);

        return layout;
    }

}
