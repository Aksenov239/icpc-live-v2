package org.icpclive.webadmin.mainscreen;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;
import org.icpclive.backend.player.urls.TeamUrls;
import org.icpclive.events.TeamInfo;

import java.util.Set;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class MainScreenTeamView extends CustomComponent implements View {
    public static String NAME = "mainscreen-team";

    public static final String STATISTICS_SHOW_TYPE = "stats";

    Label showStatus;

    Button teamShow;
    Button teamHide;

    final String AUTOMATIC_STOPPED_STATUS = "Not automated<br><br>";
    Button automatedShow;
    ComboBox automatedNumber;

    TextField sleepTime;

    final String[] types = TeamUrls.types;
    OptionGroup typeSelection;
    //    ListSelect teamSelection;
    OptionGroup teamSelection;

    public String getStatus() {
        if (mainScreenData.teamData.inAutomaticShow()) {
            return mainScreenData.teamData.automaticStatus();
        } else {
            String status = MainScreenData.getMainScreenData().teamData.infoStatus();
            return Utils.getTeamStatus(status, true);
        }
    }

    private void setSleepTime() {
        try {
            mainScreenData.teamData.setSleepTime(Integer.parseInt(sleepTime.getValue()));
        } catch (Exception e) {
            Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
        }
    }

    private boolean localLoad(String type) {
        return STATISTICS_SHOW_TYPE.equals(type) &&
                TeamUrls.localUrlType.contains(type);
    }

    public Component getControllerTeam() {
        showStatus = new Label(getStatus(), ContentMode.HTML);

        typeSelection = new OptionGroup();
        typeSelection.addItem(STATISTICS_SHOW_TYPE);
        for (String type : types) {
            if (!type.equals("")) {
                typeSelection.addItem(type);
            }
        }
        typeSelection.select(STATISTICS_SHOW_TYPE);
        typeSelection.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);

//                Notification.show("You need to wait " + MainScreenData.getProperties().sleepTime / 1000 + " seconds first", Type.WARNING_MESSAGE);

        automatedShow = new Button("Show top teams");
        automatedShow.addClickListener(event -> {
            setSleepTime();
            if (mainScreenData.teamData.inAutomaticShow()) {
                Notification.show("Automatic show is already on", Type.WARNING_MESSAGE);
                return;
            }
            if (mainScreenData.teamData.automaticStart(
                    (int) automatedNumber.getValue(),
                    (String) typeSelection.getValue(),
                    true)) {
                Notification.show(automatedNumber.getValue() + " first teams are in automatic show", Type.TRAY_NOTIFICATION);
            } else {
                Notification.show("You need to wait " + MainScreenData.getProperties().sleepTime / 1000 + " seconds first", Type.WARNING_MESSAGE);
            }
        });
        automatedNumber = new ComboBox();
        automatedNumber.addItems(3, 4, 5, 8, 10, 12, 15, 20);
        automatedNumber.setNullSelectionAllowed(false);
        automatedNumber.setValue(10);

        sleepTime = new TextField("Sleep time");
        sleepTime.setValue("" + MainScreenData.getProperties().sleepTime);

        setSleepTime();

        teamSelection = new OptionGroup();
        teamSelection.setHtmlContentAllowed(true);
        teamSelection.addStyleName("team-optiongroup");

        Set<String> topTeamsIds = MainScreenProperties.topteamsids;
        for (TeamInfo team : MainScreenData.getProperties().teamInfos) {
            teamSelection.addItem(team);
            String teamHtml = topTeamsIds.contains(team.getAlias()) ?
                    "<b>" + team.toString() + "</b>" : team.toString();
            teamSelection.setItemCaption(team, teamHtml);
//            log.debug(team.toString());
        }
        teamSelection.setValue(MainScreenData.getProperties().teamInfos[0]);
        teamSelection.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        teamSelection.setWidth("100%");

        teamSelection.addValueChangeListener(event -> {
            if (mainScreenData.teamData.isVisible()) {
                if (mainScreenData.teamData.inAutomaticShow()) {
                    Notification.show("You need to stop automatic show first", Type.WARNING_MESSAGE);
                    return;
                }

                if (localLoad((String) typeSelection.getValue())) {
                    mainScreenData.teamData.setSleepTime(0);
                } else {
                    setSleepTime();
                }
                String type = STATISTICS_SHOW_TYPE.equals(typeSelection.getValue()) ? "" :
                        (String) typeSelection.getValue();
                String result = mainScreenData.teamData.setInfoManual(
                        true, type,
                        (TeamInfo) teamSelection.getValue(),
                        false);
                if (result != null) {
                    teamSelection.setValue(mainScreenData.teamData.getTeam());
                    Notification.show(result, Type.WARNING_MESSAGE);
                    return;
                }
            }
        });

        teamShow = new Button("Show info");
        teamShow.addClickListener(event -> {
            if (mainScreenData.teamData.inAutomaticShow()) {
                Notification.show("You need to stop automatic show first", Type.WARNING_MESSAGE);
                return;
            }

            if (localLoad((String) typeSelection.getValue())) {
                // TODO: rewrite in the client
                mainScreenData.teamData.setSleepTime(0);
            } else {
                setSleepTime();
            }

            String type = STATISTICS_SHOW_TYPE.equals(typeSelection.getValue()) ?
                    "" : (String) typeSelection.getValue();
            String result = mainScreenData.teamData.setInfoManual(
                    true, type, (TeamInfo) teamSelection.getValue(), true);
            if (result != null) {
                Notification.show(result, Type.WARNING_MESSAGE);
            }
        });
        teamShow.setStyleName(ValoTheme.BUTTON_PRIMARY);

        teamHide = new Button("Stop");
        teamHide.addClickListener(event -> {
            if (mainScreenData.teamData.inAutomaticShow()) {
                mainScreenData.teamData.automaticStop();
            } else {
                mainScreenData.teamData.setInfoManual(false, null, null, false);
                mainScreenData.teamStatsData.setVisible(false, null);
            }
        });

        Component controlAutomaticGroup = createGroupLayout(automatedNumber, automatedShow);
        Component controlManualGroup = createGroupLayout(teamShow, teamHide);
        Component controlGroup = new HorizontalLayout(controlManualGroup, controlAutomaticGroup);
        VerticalLayout result = new VerticalLayout(
                showStatus,
                sleepTime,
                typeSelection,
                controlGroup,
                teamSelection
        );
//        result.setSpacing(true);
        result.setSizeFull();
        result.setHeight("100%");
        result.setComponentAlignment(sleepTime, Alignment.MIDDLE_CENTER);
        result.setComponentAlignment(typeSelection, Alignment.MIDDLE_CENTER);
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
        showStatus.setValue(getStatus());
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
