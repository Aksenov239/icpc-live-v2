package ru.ifmo.acm.mainscreen;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import ru.ifmo.acm.datapassing.StandingsData;

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
    Button standingsShowTop1Big;
    Button standingsShowTop2Big;
    Button standingsShowAllBig;

    public Component getStandingsController() {
        standingsStatus = new Label(getStandingsStatus());
        standingsStatus.addStyleName("large");
        standingsShowTop1 = createStandingsControllerButton("Show first page", true, StandingsData.StandingsType.ONE_PAGE, false);
        standingsShowTop2 = createStandingsControllerButton("Show two pages", true, StandingsData.StandingsType.TWO_PAGES, false);
        standingsShowAll = createStandingsControllerButton("Show all pages", true, StandingsData.StandingsType.ALL_PAGES, false);
        standingsHide = createStandingsControllerButton("Hide", false, StandingsData.StandingsType.HIDE, false);
        standingsShowTop1Big = createStandingsControllerButton("Show first page. Big standings", true, StandingsData.StandingsType.ONE_PAGE, true);
        standingsShowTop2Big = createStandingsControllerButton("Show two pages. Big standings", true, StandingsData.StandingsType.TWO_PAGES, true);
        standingsShowAllBig = createStandingsControllerButton("Show all pages. Big standings", true, StandingsData.StandingsType.ALL_PAGES, true);

        CssLayout group = createGroupLayout(standingsShowTop1, standingsShowTop2, standingsShowAll, standingsShowTop1Big, standingsShowTop2Big, standingsShowAllBig, standingsHide);

        VerticalLayout panel = new VerticalLayout(
                standingsStatus,
                group
        );
        setPanelDefaults(panel);
        return panel;
    }

    private Button createStandingsControllerButton(String name, boolean visible, StandingsData.StandingsType type, boolean isBig) {
        Button button = new Button(name);
        button.addClickListener(event -> {
            if (visible && mainScreenData.standingsData.isStandingsVisible()) {
                Notification.show("You should hide standings first", Notification.Type.WARNING_MESSAGE);
                return;
            }
            mainScreenData.standingsData.setStandingsVisible(visible, type, isBig);
            standingsStatus.setValue(getStandingsStatus());
        });

        return button;
    }

    public String getStandingsStatus() {
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
