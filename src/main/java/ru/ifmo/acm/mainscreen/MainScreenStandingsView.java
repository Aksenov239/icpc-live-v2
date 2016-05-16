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

    OptionGroup standingsOptimismLevel;

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

    /* Breaking news */
    CheckBox isLive;
    OptionGroup types;
    TextField team;
    Button show;
    Button hide;
    Label breakingNewsStatus;

//    public Component getBreakingNewsController() {
//        breakingNewsStatus = new Label(getBreakingNewsStatus());
//        breakingNewsStatus.addStyleName("large");
//
//        isLive = new CheckBox("Is live");
//        isLive.addValueChangeListener(event -> {
//            //types.setValue(isLive.getValue() ? null: TeamWidget.types[0]);
////            for (String type : TeamWidget.types) {
////                types.setItemEnabled(type, !isLive.getValue());
////            }
//            types.setEnabled(isLive.getValue());
//            breakingNewsStatus.setValue(getBreakingNewsStatus());
//        });
//        isLive.setValue(false);
//
//        types = new OptionGroup();
//        types.addItems(TeamWidget.types);
//        types.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
//        types.setValue(TeamWidget.types[0]);
//        types.setEnabled(false);
//
//        team = new TextField("Team: ");
//        team.setSizeFull();
//
//        show = new Button("Show");
//        show.addClickListener(event -> {
//            if (team.getValue().equals("")) {
//                Notification.show("Team field requires team id and problem id");
//            } else {
//                String[] zz = team.getValue().split(",");
//                int teamId = Integer.parseInt(zz[0]) - 1;
//                int problemId = zz[1].charAt(0) - 'A';
//                if (!mainScreenData.breakingNewsData.setNewsVisible(true,
//                        (String) types.getValue(),
//                        isLive.getValue(), "",
//                        teamId, problemId, -1)) {
//                    Notification.show(
//                            "You need to wait while current breaking news is shown",
//                            Notification.Type.WARNING_MESSAGE
//                    );
//                } else {
//                    team.clear();
//                }
//                breakingNewsStatus.setValue(getBreakingNewsStatus());
//            }
//        });
//
//        hide = new Button("Hide");
//        hide.addClickListener(event -> {
//            mainScreenData.breakingNewsData.setNewsVisible(false, null, isLive.getValue(), "", -1, -1, -1);
//            breakingNewsStatus.setValue(getBreakingNewsStatus());
//        });
//
//        CssLayout teamLayout = createGroupLayout(team, show, hide);
//        VerticalLayout result = new VerticalLayout(breakingNewsStatus, isLive, types, teamLayout);
//        result.setSpacing(true);
//
//        setPanelDefaults(result);
//        return result;
//    }

    /* Standings */
    Label standingsStatus;

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
        standingsOptimismLevel = new OptionGroup();
        for (StandingsData.OptimismLevel type : StandingsData.OptimismLevel.values()) {
            standingsOptimismLevel.addItem(type);
        }
        standingsOptimismLevel.setValue(StandingsData.OptimismLevel.NORMAL);

        standingsOptimismLevel.addValueChangeListener(e -> {
            String outcome = mainScreenData.standingsData.setStandingsVisible(
                    mainScreenData.standingsData.isVisible,
                    mainScreenData.standingsData.standingsType,
                    mainScreenData.standingsData.isBig,
                    StandingsData.OptimismLevel.valueOf(standingsOptimismLevel.getValue().toString().toUpperCase())
            );
            if (outcome != null) {
                Notification.show(outcome, Notification.Type.WARNING_MESSAGE);
                return;
            }
            standingsStatus.setValue(getStandingsStatus());
        });

        VerticalLayout panel = new VerticalLayout(
                standingsStatus,
                standingsOptimismLevel,
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
            String optimismLevel = standingsOptimismLevel.getValue().toString();

            String outcome = mainScreenData.standingsData.setStandingsVisible(visible, type, isBig, StandingsData.OptimismLevel.valueOf(optimismLevel.toUpperCase()));
            if (outcome != null) {
                Notification.show(outcome, Notification.Type.WARNING_MESSAGE);
                return;
            }
            standingsStatus.setValue(getStandingsStatus());
        });

        return button;
    }

    public String getStandingsStatus() {
        return mainScreenData.standingsData.toString();
    }

    public String getBreakingNewsStatus() {
        return mainScreenData.breakingNewsData.getStatus();
    }

    /* Queue */
    final String[] queueStatuses = new String[]{"Queue is shown", "Queue isn't shown"};
    Label queueStatus;
    Button queueShow;
    Button queueHide;

    public Component getQueueController() {
        queueStatus = new Label(getQueueStatus());
        queueStatus.addStyleName("large");

        queueShow = createQueueButton("Show queue", true, 0);
        queueHide = createQueueButton("Hide queue", false, 1);

        CssLayout group = createGroupLayout(queueShow, queueHide);

        VerticalLayout panel = new VerticalLayout(queueStatus, group);
        setPanelDefaults(panel);
        return panel;
    }

    public String getQueueStatus() {
        boolean status = mainScreenData.queueData.isQueueVisible();
        return status ? queueStatuses[0] : queueStatuses[1];
    }

    private Button createQueueButton(String name, boolean visibility, int status) {
        Button button = new Button(name);
        button.addClickListener(event -> {
            mainScreenData.queueData.setVisible(visibility);
            queueStatus.setValue(queueStatuses[status]);
        });

        return button;
    }


    /* Statistics */

    final String[] statisticsStatuses = new String[]{"Statistics is shown", "Statistics isn't shown"};
    Label statisticsStatus;
    Button statisticsShow;
    Button statisticsHide;

    public Component getStatisticsController() {
        statisticsStatus = new Label(getStatisticsStatus());
        statisticsStatus.addStyleName("large");

        statisticsShow = createStatisticsButton("Show statistics", true, 0);
        statisticsHide = createStatisticsButton("Hide statistics", false, 1);

        CssLayout group = createGroupLayout(statisticsShow, statisticsHide);

        VerticalLayout panel = new VerticalLayout(statisticsStatus, group);
        setPanelDefaults(panel);
        return panel;
    }

    public String getStatisticsStatus() {
        boolean status = mainScreenData.statisticsData.isVisible();
        return status ? statisticsStatuses[0] : statisticsStatuses[1];
    }

    private Button createStatisticsButton(String name, boolean visibility, int status) {
        Button button = new Button(name);
        button.addClickListener(event -> {
            String outcome = mainScreenData.statisticsData.setVisible(visibility);
            if (outcome != null) {
                Notification.show(outcome, Notification.Type.WARNING_MESSAGE);
                return;
            }
            
            statisticsStatus.setValue(statisticsStatuses[status]);
        });

        return button;
    }

    /* mainscreen */
    MainScreenData mainScreenData;

    public void refresh() {
        clockStatus.setValue(getClockStatus());
        standingsStatus.setValue(getStandingsStatus());
//            breakingNewsStatus.setValue(getBreakingNewsStatus());
        queueStatus.setValue(getQueueStatus());
        statisticsStatus.setValue(getStatisticsStatus());

        mainScreenData.update();
    }

    public MainScreenStandingsView() {
        mainScreenData = MainScreenData.getMainScreenData();

        Component clockController = getClockController();
        Component standingsController = getStandingsController();
        //Component breakingNewsController = getBreakingNewsController();
        Component queueStatus = getQueueController();
        Component statisticsController = getStatisticsController();

        VerticalLayout mainPanel = new VerticalLayout(
                clockController, standingsController, statisticsController, queueStatus);
        mainPanel.setSizeFull();
        setCompositionRoot(mainPanel);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {

    }
}
