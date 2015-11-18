package ru.ifmo.acm.mainscreen;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;

/**
 * Created by Aksenov239 on 15.11.2015.
 */
public class MainScreenView extends CustomComponent implements View {
    public static String NAME = "mainscreen";
    final String[] clockStatuses = new String[]{"Clock is shown", "Clock isn't shown"};
    Label clockStatus;
    Button clockButtonOn;
    Button clockButtonOff;

    public String getClockStatus() {
        boolean status = mainScreenData.isClockVisible();
        return status ? clockStatuses[0] : clockStatuses[1];
    }

    public Component getClockController() {
        boolean status = mainScreenData.isClockVisible;
        clockStatus = new Label(getClockStatus());
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
        String[] s = status.split("\n");
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
        panel.setMargin(new MarginInfo(false, false, false, true));
        panel.setSpacing(true);
        return panel;
    }

    Label advertisementStatus;
    TextField advertisementText;
    Button addAdvertisement;
    Button removeAdvertisement;
    Button discardAdvertisement;
    Button showAdvertisement;
    Button hideAdvertisement;
    Table advertisements;

    public String getAdvertisementStatus() {
        String status = mainScreenData.advertisementStatus();
        String[] s = status.split("\n");
        return s[1].equals("true") ? "Advertisement \"" + s[2] + "\"" : "No advertisement now";
    }

    public Component getAdvertisementController() {
        //String status = mainScreenData.advertisementStatus();
        //String[] s = status.split("\n");
        advertisementStatus = new Label(getAdvertisementStatus());
        advertisementStatus.addStyleName("large");

        advertisementText = new TextField("Advertisement text: ");

        addAdvertisement = new Button("Add new");
        addAdvertisement.addClickListener(event -> {
            if (addAdvertisement.getCaption().equals("Add new")) {
                mainScreenData.addAdvertisement(new Advertisement(advertisementText.getValue()));
            } else {
                mainScreenData.advertisements.getItem(advertisements.getValue()).getItemProperty("advertisement").
                        setValue(advertisementText.getValue());
                advertisements.setValue(null);
                addAdvertisement.setCaption("Add new");
                removeAdvertisement.setVisible(false);
                discardAdvertisement.setVisible(false);
            }
            advertisementText.clear();

            advertisements.refreshRowCache();
        });

        removeAdvertisement = new Button("Remove selected");
        removeAdvertisement.addClickListener(event -> {
            if (advertisements.getValue() != null) {
                mainScreenData.removeAdvertisement((Advertisement) advertisements.getValue());
                advertisements.refreshRowCache();
            } else {
                Notification.show("You should choose advertisement", Type.ERROR_MESSAGE);
            }
            advertisementText.setValue("");
            advertisements.setValue(null);
            addAdvertisement.setCaption("Add new");
            removeAdvertisement.setVisible(false);
            discardAdvertisement.setVisible(false);
        });
        removeAdvertisement.setVisible(false);

        discardAdvertisement = new Button("Discard");
        discardAdvertisement.addClickListener(event -> {
            advertisementText.setValue("");
            advertisements.setValue(null);
            addAdvertisement.setCaption("Add new");
            removeAdvertisement.setVisible(false);
            discardAdvertisement.setVisible(false);
        });
        discardAdvertisement.setVisible(false);
        CssLayout groupAdd = new CssLayout();
        groupAdd.addStyleName("v-component-group");
        groupAdd.addComponents(advertisementText, addAdvertisement, removeAdvertisement, discardAdvertisement);

        advertisements = new Table();
        advertisements.setContainerDataSource(mainScreenData.advertisements);
        advertisements.setSelectable(true);
        advertisements.setEditable(false);
        advertisements.setSizeFull();
        advertisements.addValueChangeListener(event -> {
            if (advertisements.getValue() == null) {
                addAdvertisement.setCaption("Add new");
                removeAdvertisement.setVisible(false);
                discardAdvertisement.setVisible(false);
                advertisementText.setValue("");
                return;
            }
            addAdvertisement.setCaption("Edit");
            removeAdvertisement.setVisible(true);
            discardAdvertisement.setVisible(true);
            advertisementText.setValue(((Advertisement) advertisements.getValue()).getAdvertisement());
        });

        showAdvertisement = new Button("Show advertisement");
        showAdvertisement.addClickListener(event -> {
            if (advertisements.getValue() != null) {
                mainScreenData.setAdvertisementVisible(true, (Advertisement) advertisements.getValue());
                advertisementStatus.setValue(getAdvertisementStatus());
            } else {
                Notification.show("You should choose advertisement", Type.ERROR_MESSAGE);
            }
        });

        hideAdvertisement = new Button("Hide advertisement");
        hideAdvertisement.addClickListener(event -> {
            mainScreenData.setAdvertisementVisible(false, (Advertisement) advertisements.getValue());
            advertisementStatus.setValue(getAdvertisementStatus());
        });

        CssLayout groupControl = new CssLayout();
        groupControl.addStyleName("v-component-group");
        groupControl.addComponents(showAdvertisement, hideAdvertisement);

        VerticalLayout panel = new VerticalLayout(
                advertisementStatus,
                groupAdd,
                advertisements,
                new HorizontalLayout(
                        showAdvertisement,
                        hideAdvertisement
                )
        );
        panel.setMargin(new MarginInfo(false, false, false, true));
        panel.setSpacing(true);
        return panel;
    }

    TextField name;
    TextField profession;
    Button addPerson;

    Table personsLeft;
    Table personsRight;

    public Component getPersonsWidget() {
        return null;
    }

    MainScreenData mainScreenData;

    public MainScreenView() {
        mainScreenData = MainScreenData.getMainScreenData();

        Component clockController = getClockController();
        Component standingsController = getStandingsController();
        Component advertisementController = getAdvertisementController();

        VerticalLayout mainPanel = new VerticalLayout(clockController, standingsController, advertisementController);
        mainPanel.setSpacing(true);
        setCompositionRoot(mainPanel);
    }

    public void refresh() {
        clockStatus.setValue(getClockStatus());

        String status = mainScreenData.standingsStatus();
        String[] s = status.split("\n");
        standingsStatus.setValue(Boolean.parseBoolean(s[1]) ?
                        (String.format(labelStatuses[Integer.parseInt(s[2])], (System.currentTimeMillis() - Long.parseLong(s[0])) / 1000)) :
                        labelStatuses[3]
        );

        advertisementStatus.setValue(getAdvertisementStatus());
        advertisements.refreshRowCache();

        //personsLeft.refreshRowCache();
        //personsRight.refreshRowCache();
    }

    public void enter(ViewChangeEvent event) {

    }
}
