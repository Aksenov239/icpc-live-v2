package org.icpclive.webadmin.mainscreen;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.icpclive.backend.player.widgets.locator.LocatorCamera;
import org.icpclive.backend.player.widgets.locator.LocatorsData;
import org.icpclive.events.ContestInfo;
import org.icpclive.events.EventsLoader;
import org.icpclive.events.TeamInfo;

import java.util.Set;

public class MainScreenLocatorView extends CustomComponent implements View {
    public static String NAME = "mainscreen-locator";

    MainScreenData mainScreenData;

    Label status;

    OptionGroup teamSelection;
    OptionGroup locatorSelection;

    Button show;
    Button hide;
    Button clear;

    Label teamsSelected;

    public Component getController() {
        status = new Label();
        status.setValue(mainScreenData.locatorData.getStatus());

        teamSelection = new OptionGroup();
        teamSelection.setMultiSelect(true);
        teamSelection.setHtmlContentAllowed(true);
        teamSelection.addStyleName("team-optiongroup");

        Set<String> topTeamsIds = MainScreenProperties.topteamsids;
        ContestInfo contestData = EventsLoader.getInstance().getContestData();
        int n = contestData.getStandings().length;
        for (int i = 0; i < n; i++) {
            TeamInfo team = contestData.getParticipant(i);
            teamSelection.addItem(team);
//            String teamHtml = topTeamsIds.contains(team.getAlias()) ?
//                    "<b>" + team.toString() + "</b>" : team.toString();
            String teamHtml = team.getRank() <= 12 && team.getSolvedProblemsNumber() > 0 ?
                    "<b>" + team.toString() + "</b>" : team.toString();
            teamSelection.setItemCaption(team, teamHtml);
        }
//        teamSelection.setValue(contestData[0]);
        teamSelection.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        teamSelection.setWidth("100%");

        teamSelection.addValueChangeListener(event -> {
            Set<TeamInfo> selection = (Set<TeamInfo>) teamSelection.getValue();
            mainScreenData.locatorData.setTeams(selection);
            String s = "";
            for (TeamInfo teamInfo : mainScreenData.locatorData.getTeams()) {
                if (s.length() > 0) s += ", ";
                s += teamInfo.getShortName();
            }
            if (s.length() == 0) s = "nothing selected";
            teamsSelected.setValue(s);
        });

        locatorSelection = new OptionGroup();
        locatorSelection.addItems(LocatorsData.locatorCameras);
        locatorSelection.select(LocatorsData.locatorCameras.get(0));
        locatorSelection.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        locatorSelection.setImmediate(true);
        locatorSelection.addValueChangeListener(event -> {
            mainScreenData.locatorData.setCameraID(((LocatorCamera) locatorSelection.getValue()).cameraID);
        });

        show = new Button("Show");
        show.setStyleName(ValoTheme.BUTTON_PRIMARY);
        show.addClickListener(e -> {
            String outcome = mainScreenData.locatorData.setVisible();
            if (outcome != null) {
                Notification.show(outcome, Notification.Type.WARNING_MESSAGE);
            }
        });
        hide = new Button("Hide");
        hide.addClickListener(e -> {
            mainScreenData.locatorData.hide();
        });
        clear = new Button("Clear");
        clear.addClickListener(e -> {
            teamSelection.clear();
        });

        teamsSelected = new Label("nothing selected", ContentMode.HTML);
        teamsSelected.setWidthUndefined();

        Component buttonGroup = createGroupLayout(show, hide, clear);
        HorizontalLayout teamsSelectedComponent = new HorizontalLayout(teamsSelected);
        teamsSelectedComponent.setMargin(true);
        teamsSelectedComponent.setComponentAlignment(teamsSelected, Alignment.MIDDLE_CENTER);
        teamsSelectedComponent.setSizeFull();
        teamsSelectedComponent.setWidth("100%");
        VerticalLayout result = new VerticalLayout(
                status,
                locatorSelection,
                buttonGroup,
                teamsSelectedComponent,
                teamSelection
        );
        result.setSizeFull();
        result.setHeight("100%");
        result.setComponentAlignment(buttonGroup, Alignment.MIDDLE_CENTER);
        result.setComponentAlignment(teamsSelectedComponent, Alignment.MIDDLE_CENTER);
        return result;
    }

    public MainScreenLocatorView() {
        mainScreenData = MainScreenData.getMainScreenData();

        Component main = getController();
        main.setSizeFull();
        main.setHeight("100%");

        setCompositionRoot(main);
    }

    public void refresh() {
        status.setValue(mainScreenData.locatorData.getStatus());
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
