package ru.ifmo.acm.mainscreen;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;

import static ru.ifmo.acm.mainscreen.Utils.createGroupLayout;
import static ru.ifmo.acm.mainscreen.Utils.setPanelDefaults;

public class MainScreenStandingsView extends CustomComponent implements View {
    public static String NAME = "mainscrean-standings";

    /* Clocks */
    final String[] clockStatuses = new String[]{"Clock is shown", "Clock isn't shown"};
    Label clockStatus;
    Button clockButtonOn;
    Button clockButtonOff;

    public Component getClockController() {
        clockStatus = new Label(getClockStatus());
        clockStatus.addStyleName("large");

        clockButtonOn = createClockButton("Show clock", true, 0);
        clockButtonOff = createClockButton("Hide clock", false, 1);

        CssLayout group = createGroupLayout(clockButtonOn, clockButtonOff);

        VerticalLayout panel = new VerticalLayout(clockStatus, group);
        setPanelDefaults(panel);
        return panel;
    }

    public String getClockStatus() {
        boolean status = mainScreenData.clockData.isClockVisible();
        return status ? clockStatuses[0] : clockStatuses[1];
    }

    private Button createClockButton(String name, boolean visibility, int status) {
        Button button = new Button(name);
        button.addClickListener(event -> {
            mainScreenData.clockData.setClockVisible(visibility);
            clockStatus.setValue(clockStatuses[status]);
        });

        return button;
    }


    /* Standings */
    Label standingsStatus;
    final String[] labelStatuses = new String[]{
            "Top 1 page is shown for %d seconds",
            "Top 2 pages are remaining for %d seconds",
            "All pages are remaining for %d seconds",
            "Standings aren't shown"
    };
    Button standingsShowTop1;
    Button standingsShowTop2;
    Button standingsShowAll;
    Button standingsHide;

    public Component getStandingsController() {
        standingsStatus = new Label(getStandingsStatus());
        standingsStatus.addStyleName("large");
        standingsShowTop1 = createStandingsControllerButton("Show first page", true, 0);
        standingsShowTop2 = createStandingsControllerButton("Show two pages", true, 1);
        standingsShowAll = createStandingsControllerButton("Show all pages", true, 2);
        standingsHide = createStandingsControllerButton("Hide", false, -1);

        CssLayout group = createGroupLayout(standingsShowTop1, standingsShowTop2, standingsShowAll, standingsHide);

        VerticalLayout panel = new VerticalLayout(
                standingsStatus,
                group
        );
        setPanelDefaults(panel);
        return panel;
    }

    private Button createStandingsControllerButton(String name, boolean visible, int type) {
        Button button = new Button(name);
        button.addClickListener(event -> {
            if (visible && mainScreenData.standingsData.isStandingsVisible()) {
                Notification.show("You should hide standings first", Notification.Type.WARNING_MESSAGE);
                return;
            }
            mainScreenData.standingsData.setStandingsVisible(visible, type);
            standingsStatus.setValue(getStandingsStatus());
        });

        return button;
    }

    public String getStandingsStatus() {
//        StandingsData status = mainScreenData.standingsStatus.standingsStatus();
//        if (status.isStandingsVisible) {
//            long time = status.standingsType == 0
//                    ? (System.currentTimeMillis() - status.standingsTimestamp) / 1000
//                    : (status.standingsTimestamp + mainScreenData.standingsStatus.getTotalTime(status.standingsType) - System.currentTimeMillis()) / 1000;
//            return String.format(labelStatuses[status.standingsType], time);
//        }
//        return labelStatuses[3];
        return mainScreenData.standingsData.toString();
    }

    /* mainscreen */
    MainScreenData mainScreenData;

    public void refresh() {
        clockStatus.setValue(getClockStatus());
        standingsStatus.setValue(getStandingsStatus());
        mainScreenData.update();
    }

    public MainScreenStandingsView() {
        mainScreenData = MainScreenData.getMainScreenData();

        Component clockController = getClockController();
        Component standingsController = getStandingsController();

        VerticalLayout mainPanel = new VerticalLayout(clockController, standingsController);
        mainPanel.setSizeFull();
        setCompositionRoot(mainPanel);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {

    }
}
