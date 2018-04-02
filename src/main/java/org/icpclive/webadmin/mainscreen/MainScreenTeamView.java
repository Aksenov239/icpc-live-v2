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

    Label teamStatus;
    Button teamShow;
    Button teamHide;
    final String AUTOMATIC_STOPPED_STATUS = "Not automated<br><br>";
    Label automaticStatus;
    Button automatedShow;
    Button automatedStop;
    ComboBox automatedNumber;

    TextField sleepTime;

    final String[] types = TeamUrls.types;
    OptionGroup typeSelection;
    //    ListSelect teamSelection;
    OptionGroup teamSelection;
    CheckBox stats;

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
        teamStatus = new Label(getTeamStatus());

        typeSelection = new OptionGroup();
        typeSelection.addItem(STATISTICS_SHOW_TYPE);
        for (String type : types) {
            if (!type.equals("")) {
                typeSelection.addItem(type);
            }
        }
        typeSelection.select(STATISTICS_SHOW_TYPE);
        typeSelection.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);

//        typeSelection.addValueChangeListener(event -> {
//           if (typeSelection.getValue().equals("")) {
//               stats.setValue(true);
//           }
//        });
//        type.addValueChangeListener(event -> {
//            setSleepTime();
//            if (teamSelection.getValue() == null)
//                return;
//            if (mainScreenData.teamData.isVisible() &&
//                    !mainScreenData.teamData.setInfoManual(true, (String) type.getValue(), (TeamInfo) teamSelection.getValue())) {
//                type.setValue(mainScreenData.teamData.infoType);
//                Notification.show("You need to wait " + MainScreenData.getProperties().sleepTime / 1000 + " seconds first", Type.WARNING_MESSAGE);
//            } else {
//                mainScreenData.teamStatsData.setVisible(stats.getValue(), (TeamInfo) teamSelection.getValue());
//            }
//        });

        //teamSelection.addItems(mainScreenData.teamStatus.teamNames);

        automaticStatus = new Label(AUTOMATIC_STOPPED_STATUS, ContentMode.HTML);
        automatedShow = new Button("Show top teams");
        automatedShow.addClickListener(event -> {
            setSleepTime();
            if (mainScreenData.teamData.inAutomaticShow()) {
                Notification.show("Automatic show is already on", Type.WARNING_MESSAGE);
                return;
            }
            if (mainScreenData.teamData.automaticStart((int) automatedNumber.getValue())) {
                Notification.show(automatedNumber.getValue() + " first teams are in automatic show", Type.TRAY_NOTIFICATION);
            } else {
                Notification.show("You need to wait " + MainScreenData.getProperties().sleepTime / 1000 + " seconds first", Type.WARNING_MESSAGE);
            }
        });
        automatedStop = new Button("Stop automatic");
        automatedStop.addClickListener(event -> {
            if (!mainScreenData.teamData.inAutomaticShow()) {
                return;
            }
            mainScreenData.teamData.automaticStop();
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
                if (STATISTICS_SHOW_TYPE.equals(typeSelection.getValue())) {
                    mainScreenData.teamData.setInfoManual(false, null, null);
                    mainScreenData.teamStatsData.setVisible(stats.getValue(),
                            (TeamInfo) teamSelection.getValue());
                } else {
                    String result = mainScreenData.teamData.setInfoManual(
                            true, (String) typeSelection.getValue(), (TeamInfo) teamSelection.getValue());
                    if (result == null) {
                        teamSelection.setValue(mainScreenData.teamData.getTeamString());
                        Notification.show("You need to wait " +
                                MainScreenData.getProperties().sleepTime / 1000 + " seconds first", Type.WARNING_MESSAGE);
                    } else {
                        mainScreenData.teamStatsData.setVisible(stats.getValue(),
                            (TeamInfo) teamSelection.getValue());
                    };

                }
            }
        });

        stats = new CheckBox("Statistics");

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

            if (stats.getValue() ||
                    STATISTICS_SHOW_TYPE.equals(typeSelection.getValue())) {
                mainScreenData.teamStatsData.setVisible(stats.getValue(), (TeamInfo) teamSelection.getValue());
            }

            if (STATISTICS_SHOW_TYPE.equals(typeSelection.getValue())) {
                mainScreenData.teamData.setInfoManual(false, null, null);
            } else {
                String result = mainScreenData.teamData.setInfoManual(
                        true, (String) typeSelection.getValue(), (TeamInfo) teamSelection.getValue());
                if (result != null) {
                    Notification.show(result);
                }
            }
        });

        teamHide = new Button("Hide info");
        teamHide.addClickListener(event -> {
            if (mainScreenData.teamData.inAutomaticShow()) {
                Notification.show("You need to stop automatic show first");
                return;
            }
            mainScreenData.teamData.setInfoManual(false, null, null);
            mainScreenData.teamStatsData.setVisible(false, null);
        });

        Component controlAutomaticGroup = createGroupLayout(automatedShow, automatedStop, automatedNumber);
        Component controlGroup = new HorizontalLayout(
                createGroupLayout(typeSelection, stats), teamShow, teamHide);
        VerticalLayout result = new VerticalLayout(
                automaticStatus,
                controlAutomaticGroup,
                sleepTime,
                teamStatus,
                controlGroup,
                teamSelection
        );
//        result.setSpacing(true);
        result.setSizeFull();
        result.setHeight("100%");
        result.setComponentAlignment(controlAutomaticGroup, Alignment.MIDDLE_CENTER);
        result.setComponentAlignment(sleepTime, Alignment.MIDDLE_CENTER);
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
        String automatic = mainScreenData.teamData.automaticStatus();
        automaticStatus.setValue(automatic.length() == 0 ? AUTOMATIC_STOPPED_STATUS : automatic);
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
