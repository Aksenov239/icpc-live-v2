package ru.ifmo.acm.mainscreen;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.shared.ui.MarginInfo;

/**
 * Created by Aksenov239 on 15.11.2015.
 */
public class MainScreenView extends CustomComponent implements View {
    public static String NAME = "mainscreen";
    final String[] clockStatuses = new String[]{"Clock is shown", "Clock isn't shown"};
    Label clockStatus;
    Button clockButtonOn;
    Button clockButtonOff;

    public Component getClockController() {
        boolean status = mainScreenData.isClockVisible;
        clockStatus = new Label(status ? clockStatuses[0] : clockStatuses[1]);
        clockStatus.addStyleName("large");

        clockButtonOn = new Button("Show clock");
        clockButtonOn.addClickListener(event -> {
            mainScreenData.setClockVisible(true);
            clockStatus.setValue(clockStatuses[0]);
        });

        clockButtonOff = new Button("Hide clock");
        clockButtonOff.addClickListener(event -> {
            mainScreenData.setClockVisible(false);
            clockStatus.setValue(clockStatuses[1]);
        });

        CssLayout group = new CssLayout();
        group.addStyleName("v-component-group");
        group.addComponents(clockButtonOn, clockButtonOff);

        VerticalLayout panel = new VerticalLayout(clockStatus, group);
        panel.setMargin(new MarginInfo(false, false, false, true));
        panel.setSpacing(true);
        return panel;
    }

    Label standingsStatus;
    final String[] labelStatuses = new String[]{
            "Top 1 page is shown for %d seconds",
            "Top 2 pages are shown for %d seconds",
            "All pages are shown for %d seconds",
            "Standings aren't shown"
    };
    Button standingsShowTop1;
    Button standingsShowTop2;
    Button standingsShowAll;
    Button standingsHide;

    public Component getStandingsController() {
        String status = mainScreenData.standingsStatus();
        String[] s = status.split(" ");
        standingsStatus = new Label(Boolean.parseBoolean(s[1]) ?
                (String.format(labelStatuses[Integer.parseInt(s[2])], (System.currentTimeMillis() - Long.parseLong(s[0])) / 1000)) :
                labelStatuses[3]
        );
        standingsStatus.addStyleName("large");
        standingsShowTop1 = new Button("Show first page");
        standingsShowTop1.addClickListener(event -> {
            standingsStatus.setValue(String.format(labelStatuses[0], 0));
            mainScreenData.setStandingsVisible(true, 0);
        });

        standingsShowTop2 = new Button("Show two pages");
        standingsShowTop2.addClickListener(event -> {
            standingsStatus.setValue(String.format(labelStatuses[1], 0));
            mainScreenData.setStandingsVisible(true, 1);
        });

        standingsShowAll = new Button("Show all pages");
        standingsShowAll.addClickListener(event -> {
            standingsStatus.setValue(String.format(labelStatuses[2], 0));
            mainScreenData.setStandingsVisible(true, 2);
        });

        standingsHide = new Button("Hide");
        standingsHide.addClickListener(event -> {
            standingsStatus.setValue(String.format(labelStatuses[3], 0));
            mainScreenData.setStandingsVisible(false, -1);
        });

        CssLayout group = new CssLayout();
        group.addStyleName("v-component-group");
        group.addComponents(standingsShowTop1, standingsShowTop2, standingsShowAll, standingsHide);

        VerticalLayout panel = new VerticalLayout(
                standingsStatus,
                group
        );
        panel.setMargin(new MarginInfo(false, false, false, true) );
        panel.setSpacing(true);
        return panel;
    }

    TextField name;
    TextField profession;
    Button addPerson;

    Table personsLeft;
    Table personsRight;

    MainScreenData mainScreenData;

    public MainScreenView() {
        mainScreenData = MainScreenData.getMainScreenData();

        Component clockController = getClockController();
        Component standingsController = getStandingsController();

        VerticalLayout mainPanel = new VerticalLayout(clockController, standingsController);
        mainPanel.setSpacing(true);
        setCompositionRoot(mainPanel);
    }

    public void refresh() {
        clockStatus.setValue(mainScreenData.isClockVisible() ? clockStatuses[0] : clockStatuses[1]);

        String status = mainScreenData.standingsStatus();
        String[] s = status.split(" ");
        standingsStatus.setValue(Boolean.parseBoolean(s[1]) ?
                        (String.format(labelStatuses[Integer.parseInt(s[2])], (System.currentTimeMillis() - Long.parseLong(s[0])) / 1000)) :
                        labelStatuses[3]
        );

        //personsLeft.refreshRowCache();
        //personsRight.refreshRowCache();
    }

    public void enter(ViewChangeEvent event) {

    }
}
