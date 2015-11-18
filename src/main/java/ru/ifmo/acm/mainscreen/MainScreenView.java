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

/* Clocks */
    final String[] clockStatuses = new String[]{"Clock is shown", "Clock isn't shown"};
    Label clockStatus;
    Button clockButtonOn;
    Button clockButtonOff;

    public Component getClockController() {
        boolean status = mainScreenData.isClockVisible;
        clockStatus = new Label(getClockStatus());
        clockStatus.addStyleName("large");

        clockButtonOn = createClockButton("Show clock", true, 0);
        clockButtonOff = createClockButton("Hide clock", false, 1);

        CssLayout group = createLayout(clockButtonOn, clockButtonOff);

        VerticalLayout panel = new VerticalLayout(clockStatus, group);
//        panel.setMargin(new MarginInfo(false, false, false, true));
//        panel.setSpacing(true);
        setPanelDefaults(panel);
        return panel;
    }

    public String getClockStatus() {
        boolean status = mainScreenData.isClockVisible();
        return status ? clockStatuses[0] : clockStatuses[1];
    }

    private Button createClockButton(String name, boolean visibility, int status) {
        Button button = new Button(name);
        button.addClickListener(event -> {
            mainScreenData.setClockVisible(visibility);
            clockStatus.setValue(clockStatuses[status]);
        });

        return button;
    }


/* Standings */
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
        standingsShowTop1 = createStandingsControllerButton("Show first page", 0, true, 0);
        standingsShowTop2 = createStandingsControllerButton("Show two pages", 1, true, 1);
        standingsShowAll = createStandingsControllerButton("Show all pages", 2, true, 2);
        standingsHide = createStandingsControllerButton("Hide", 3, false, -1);

        CssLayout group = createLayout(standingsShowTop1, standingsShowTop2, standingsShowAll, standingsHide);

        VerticalLayout panel = new VerticalLayout(
                standingsStatus,
                group
        );
//        panel.setMargin(new MarginInfo(false, false, false, true));
//        panel.setSpacing(true);
        setPanelDefaults(panel);
        return panel;
    }

    private Button createStandingsControllerButton(String name, int index, boolean visible, int type) {
        Button button = new Button(name);
        button.addClickListener(event -> {
            standingsStatus.setValue(String.format(labelStatuses[index], 0));
            mainScreenData.setStandingsVisible(visible, type);
        });

        return button;
    }


/* Advertisements */
    Label advertisementStatus;
    TextField advertisementText;
    Button addAdvertisement;
    Button removeAdvertisement;
    Button discardAdvertisement;
    Button showAdvertisement;
    Button hideAdvertisement;
    Table advertisements;

    public Component getAdvertisementController() {
        //String status = mainScreenData.advertisementStatus();
        //String[] s = status.split("\n");
        advertisementStatus = new Label(getAdvertisementStatus());
        advertisementStatus.addStyleName("large");

        advertisementText = new TextField("Advertisement text: ");

        createAddAdvertisementButton();
        createRemoveAdvertisementButton();
        createDiscardAdvertisementButton();

        CssLayout groupAdd = createLayout(advertisementText, addAdvertisement, removeAdvertisement, discardAdvertisement);

        createAdvertisementTable();

        createShowAdvertisementButton();
        createHideAdvertisementButton();

        CssLayout groupControl = createLayout(showAdvertisement, hideAdvertisement);

        VerticalLayout panel = new VerticalLayout(
                advertisementStatus,
                groupAdd,
                advertisements,
                new HorizontalLayout(
                        showAdvertisement,
                        hideAdvertisement
                )
        );
//        panel.setMargin(new MarginInfo(false, false, false, true));
//        panel.setSpacing(true);
        setPanelDefaults(panel);
        return panel;
    }

    public String getAdvertisementStatus() {
        String status = mainScreenData.advertisementStatus();
        String[] s = status.split("\n");
        return s[1].equals("true") ? "Advertisement \"" + s[2] + "\"" : "No advertisement now";
    }

    private void setDefaultValues() {
        advertisements.setValue(null);
        addAdvertisement.setCaption("Add new");
        removeAdvertisement.setVisible(false);
        discardAdvertisement.setVisible(false);
    }

    private void createAdvertisementTable() {
        advertisements = new Table();
        //advertisements.setContainerDataSource(mainScreenData.advertisements);
        advertisements.setContainerDataSource(mainScreenData.advertisements.getContainer());
        advertisements.setSelectable(true);
        advertisements.setEditable(false);
        advertisements.setSizeFull();
        advertisements.addValueChangeListener(event -> {
            if (advertisements.getValue() == null) {
                setDefaultValues();
                return;
            }
            addAdvertisement.setCaption("Edit");
            removeAdvertisement.setVisible(true);
            discardAdvertisement.setVisible(true);
            advertisementText.setValue(((Advertisement) advertisements.getValue()).getAdvertisement());
        });
    }

    private void createAddAdvertisementButton() {
        addAdvertisement = new Button("Add new");
        addAdvertisement.addClickListener(event -> {
            if (addAdvertisement.getCaption().equals("Add new")) {
                mainScreenData.addAdvertisement(new Advertisement(advertisementText.getValue()));
            } else {
                mainScreenData.advertisements.getItem(advertisements.getValue()).getItemProperty("advertisement").
                        setValue(advertisementText.getValue());
                setDefaultValues();
            }
            advertisementText.clear();

            advertisements.refreshRowCache();
        });
    }

    private void createRemoveAdvertisementButton() {
        removeAdvertisement = new Button("Remove selected");
        removeAdvertisement.addClickListener(event -> {
            if (advertisements.getValue() != null) {
                mainScreenData.removeAdvertisement((Advertisement) advertisements.getValue());
                advertisements.refreshRowCache();
            } else {
                Notification.show("You should choose advertisement", Type.ERROR_MESSAGE);
            }
            advertisementText.setValue("");
            setDefaultValues();
        });
        removeAdvertisement.setVisible(false);
    }

    private void createDiscardAdvertisementButton() {
        discardAdvertisement = new Button("Discard");
        discardAdvertisement.addClickListener(event -> {
            advertisementText.setValue("");
            setDefaultValues();
        });
        discardAdvertisement.setVisible(false);
    }

    private void createShowAdvertisementButton() {
        showAdvertisement = new Button("Show advertisement");
        showAdvertisement.addClickListener(event -> {
            if (advertisements.getValue() != null) {
                mainScreenData.setAdvertisementVisible(true, (Advertisement) advertisements.getValue());
                advertisementStatus.setValue(getAdvertisementStatus());
            } else {
                Notification.show("You should choose advertisement", Type.ERROR_MESSAGE);
            }
        });
    }

    private void createHideAdvertisementButton() {
        hideAdvertisement = new Button("Hide advertisement");
        hideAdvertisement.addClickListener(event -> {
            mainScreenData.setAdvertisementVisible(false, (Advertisement) advertisements.getValue());
            advertisementStatus.setValue(getAdvertisementStatus());
        });
    }


/* Persons */
    TextField name;
    TextField profession;
    Button addPerson;

    Table personsLeft;
    Table personsRight;

    public Component getPersonsWidget() {
        return null;
    }


/* Main screen */
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


/* Utils */
    private void setPanelDefaults(VerticalLayout panel) {
        panel.setMargin(new MarginInfo(false, false, false, true));
        panel.setSpacing(true);
    }

    private CssLayout createLayout(Component... components) {
        CssLayout layout = new CssLayout();
        layout.addStyleName("v-component-group");
        layout.addComponents(components);

        return layout;
    }
}
