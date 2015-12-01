package ru.ifmo.acm.mainscreen;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import ru.ifmo.acm.datapassing.StandingsData;

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
        //boolean status = mainScreenData.clockStatus.isClockVisible();
        clockStatus = new Label(getClockStatus());
        clockStatus.addStyleName("large");

        clockButtonOn = createClockButton("Show clock", true, 0);
        clockButtonOff = createClockButton("Hide clock", false, 1);

        CssLayout group = createGroupLayout(clockButtonOn, clockButtonOff);

        VerticalLayout panel = new VerticalLayout(clockStatus, group);
//        panel.setMargin(new MarginInfo(false, false, false, true));
//        panel.setSpacing(true);
        setPanelDefaults(panel);
        return panel;
    }

    public String getClockStatus() {
        boolean status = mainScreenData.clockStatus.isClockVisible();
        return status ? clockStatuses[0] : clockStatuses[1];
    }

    private Button createClockButton(String name, boolean visibility, int status) {
        Button button = new Button(name);
        button.addClickListener(event -> {
            mainScreenData.clockStatus.setClockVisible(visibility);
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
//        String status = mainScreenData.standingsStatus.standingsStatus();
//        String[] s = status.split("\n");
//        standingsStatus = new Label(Boolean.parseBoolean(s[1]) ?
//                (String.format(labelStatuses[Integer.parseInt(s[2])], (System.currentTimeMillis() - Long.parseLong(s[0])) / 1000)) :
//                labelStatuses[3]
//        );
        standingsStatus = new Label(getStandingsStatus());
        standingsStatus.addStyleName("large");
        standingsShowTop1 = createStandingsControllerButton("Show first page", 0, true, 0);
        standingsShowTop2 = createStandingsControllerButton("Show two pages", 1, true, 1);
        standingsShowAll = createStandingsControllerButton("Show all pages", 2, true, 2);
        standingsHide = createStandingsControllerButton("Hide", 3, false, -1);

        CssLayout group = createGroupLayout(standingsShowTop1, standingsShowTop2, standingsShowAll, standingsHide);

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
            if (visible && mainScreenData.standingsStatus.isStandingsVisible()) {
                Notification.show("You should hide standings first", Type.WARNING_MESSAGE);
                return;
            }
            standingsStatus.setValue(String.format(labelStatuses[index], 0));
            mainScreenData.standingsStatus.setStandingsVisible(visible, type);
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
    String[] addAdvertisementButtonStatus = {"Add new", "Edit"};

    public Component getAdvertisementController() {
        //String status = mainScreenData.advertisementStatus();
        //String[] s = status.split("\n");
        advertisementStatus = new Label(getAdvertisementStatus());
        advertisementStatus.addStyleName("large");

        advertisementText = new TextField("Advertisement text: ");

        createAddAdvertisementButton();
        createRemoveAdvertisementButton();
        createDiscardAdvertisementButton();

        CssLayout groupAdd = createGroupLayout(advertisementText, addAdvertisement, removeAdvertisement, discardAdvertisement);

        createAdvertisementTable();

        createShowAdvertisementButton();
        createHideAdvertisementButton();

        CssLayout groupControl = createGroupLayout(showAdvertisement, hideAdvertisement);

        VerticalLayout panel = new VerticalLayout(
                advertisementStatus,
                groupAdd,
                advertisements,
                groupControl
        );
//        panel.setMargin(new MarginInfo(false, false, false, true));
//        panel.setSpacing(true);
        setPanelDefaults(panel);
        return panel;
    }

    public String getAdvertisementStatus() {
        String status = mainScreenData.advertisementStatus.advertisementStatus();
        String[] s = status.split("\n");
        return s[1].equals("true") ? "Advertisement \"" + s[2] + "\"" : "No advertisement now";
    }

    private void setDefaultValues() {
        advertisements.setValue(null);
        addAdvertisement.setCaption(addAdvertisementButtonStatus[0]);
        removeAdvertisement.setVisible(false);
        discardAdvertisement.setVisible(false);
    }

    private void createAdvertisementTable() {
        advertisements = new Table();
        //advertisements.setContainerDataSource(mainScreenData.advertisements);
        advertisements.setContainerDataSource(mainScreenData.advertisementStatus.getContainer());
        advertisements.setSelectable(true);
        advertisements.setEditable(false);
        advertisements.setSizeFull();
        advertisements.addValueChangeListener(event -> {
            if (advertisements.getValue() == null) {
                setDefaultValues();
                return;
            }
            addAdvertisement.setCaption(addAdvertisementButtonStatus[1]);
            removeAdvertisement.setVisible(true);
            discardAdvertisement.setVisible(true);
            advertisementText.setValue(((Advertisement) advertisements.getValue()).getAdvertisement());
        });
    }

    private void createAddAdvertisementButton() {
        addAdvertisement = new Button(addAdvertisementButtonStatus[0]);
        addAdvertisement.addClickListener(event -> {
            if (addAdvertisement.getCaption().equals(addAdvertisementButtonStatus[0])) {
                mainScreenData.advertisementStatus.addAdvertisement(new Advertisement(advertisementText.getValue()));
            } else {
//                mainScreenData.advertisements.getItem(advertisements.getValue()).getItemProperty("advertisement").
//                        setValue(advertisementText.getValue());
                mainScreenData.advertisementStatus.setValue(advertisements.getValue(), advertisementText.getValue());
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
                mainScreenData.advertisementStatus.removeAdvertisement((Advertisement) advertisements.getValue());
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
                mainScreenData.advertisementStatus.setAdvertisementVisible(true, (Advertisement) advertisements.getValue());
                advertisementStatus.setValue(getAdvertisementStatus());
            } else {
                Notification.show("You should choose advertisement", Type.ERROR_MESSAGE);
            }
        });
    }

    private void createHideAdvertisementButton() {
        hideAdvertisement = new Button("Hide advertisement");
        hideAdvertisement.addClickListener(event -> {
            mainScreenData.advertisementStatus.setAdvertisementVisible(false, (Advertisement) advertisements.getValue());
            advertisementStatus.setValue(getAdvertisementStatus());
        });
    }


    /* Persons */
    Label[] personStatus = new Label[2];
    TextField name;
    TextField profession;
    Button addPersonButton;
    Person lastPerson;
    String[] addPersonButtonStatus = {"Add new", "Edit"};
    Button removePersonButton;
    Button discardPersonButton;

    Table[] persons = new Table[2];

    Button[] showPerson = new Button[2];
    Button[] hidePerson = new Button[2];
    String[] captionPersons = {"left", "right"};
    Button showBothPersons;
    Button hideBothPersons;

    public Component personController(int id) {
        createPersonsTable(id);
        createPersonButtons(id);

        Component showControl = createGroupLayout(showPerson[id], hidePerson[id]);
        return new VerticalLayout(
                personStatus[id],
                persons[id],
                showControl
        );
    }

    public Component getPersonsController() {
        createPersonTextFields();
        VerticalLayout person = new VerticalLayout(name, profession);
        person.setMargin(new MarginInfo(false, true, false, false));

        createAddPersonButton();
        createRemovePersonButton();
        createDiscardPersonButton();

        Component buttonPersonsControl = createGroupLayout(addPersonButton, removePersonButton, discardPersonButton);

        HorizontalLayout personsControl = new HorizontalLayout(
                person,
                buttonPersonsControl
        );
        personsControl.setComponentAlignment(buttonPersonsControl, Alignment.MIDDLE_LEFT);

        createPersonStatuses();
        Component controllerLeft = personController(0);
        Component controllerRight = personController(1);
        HorizontalLayout showControl = new HorizontalLayout(controllerLeft, controllerRight);
        showControl.setSizeFull();
        showControl.setExpandRatio(controllerLeft, 1);
        showControl.setExpandRatio(controllerRight, 1);

        createBothPersonButtons();
        Component bothControl = createGroupLayout(showBothPersons, hideBothPersons);

        VerticalLayout result = new VerticalLayout(personsControl, showControl, bothControl);

        setPersonFormDefault();

        result.setMargin(new MarginInfo(false, false, false, true));
        result.setComponentAlignment(bothControl, Alignment.MIDDLE_CENTER);
        result.setSizeFull();

        return result;
    }

    public void setPersonFormDefault() {
        removePersonButton.setVisible(false);
        discardPersonButton.setVisible(false);
        addPersonButton.setCaption(addPersonButtonStatus[0]);
        persons[0].setValue(null);
        persons[1].setValue(null);
        name.clear();
        profession.clear();
    }

    public String getPersonStatus(int id) {
        String[] z = mainScreenData.personStatus.labelStatus(id).split("\n");
        return z[1].equals("true") ? "Show " + z[2] : "Nothing is shown";
    }

    public void createPersonStatuses() {
        personStatus[0] = new Label("Left caption:");
        personStatus[1] = new Label("Right caption:");
    }

    public void createPersonTextFields() {
        name = new TextField("Name:");
        profession = new TextField("Caption:");
    }

    public void createAddPersonButton() {
        addPersonButton = new Button(addPersonButtonStatus[0]);
        addPersonButton.addClickListener(event -> {
            if (addPersonButton.getCaption().equals(addPersonButtonStatus[0])) {
                mainScreenData.personStatus.addPerson(new Person(name.getValue(), profession.getValue()));
                setPersonFormDefault();
            } else {
                if (lastPerson != null) {
                    mainScreenData.personStatus.setValue(lastPerson, "name", name.getValue());
                    mainScreenData.personStatus.setValue(lastPerson, "position", profession.getValue());
                }
                setPersonFormDefault();
            }
        });
    }

    public void createRemovePersonButton() {
        removePersonButton = new Button("Delete");
        removePersonButton.addClickListener(event -> {
            if (lastPerson != null) {
                mainScreenData.personStatus.removePerson(lastPerson);
            }
            setPersonFormDefault();
        });
    }

    public void createDiscardPersonButton() {
        discardPersonButton = new Button("Discard");
        discardPersonButton.addClickListener(event -> {
            setPersonFormDefault();
        });
    }

    public void createPersonsTable(int id) {
        persons[id] = new Table();
        Table table = persons[id];
        table.setContainerDataSource(mainScreenData.personStatus.getContainer());
        table.setSelectable(true);
        table.setEditable(false);
        table.setSizeFull();

        table.addValueChangeListener(event -> {
            if (table.getValue() == null) {
                return;
            }
            lastPerson = (Person) table.getValue();
            name.setValue(lastPerson.getName());
            profession.setValue(lastPerson.getPosition());
            addPersonButton.setCaption(addPersonButtonStatus[1]);
            removePersonButton.setVisible(true);
            discardPersonButton.setVisible(true);
        });
    }

    public void createPersonButtons(int id) {
        showPerson[id] = new Button("Show " + captionPersons[id] + " person");
        //showLeftPerson = new Button("Show " + caption + " person");
        showPerson[id].addClickListener(event -> {
            if (persons[id].getValue() != null) {
                mainScreenData.personStatus.setLabelVisible(true, (Person) persons[id].getValue(), id);
            } else {
                Notification.show("You need to choose " + captionPersons[id] + " person", Type.WARNING_MESSAGE);
            }
        });
        hidePerson[id] = new Button("Hide " + captionPersons[id] + " person");
        hidePerson[id].addClickListener(event -> {

            mainScreenData.personStatus.setLabelVisible(false, null, id);
        });
    }

    public void createBothPersonButtons() {
        showBothPersons = new Button("Show both persons");
        hideBothPersons = new Button("Hide both persons");
        showBothPersons.addClickListener(event -> {
            for (int i = 0; i < 2; i++) {
                if (persons[i].getValue() == null) {
                    Notification.show("You need to choose " + captionPersons[i] + " person", Type.WARNING_MESSAGE);
                    return;
                }
            }
            for (int i = 0; i < 2; i++) {
                mainScreenData.personStatus.setLabelVisible(true, (Person) persons[i].getValue(), i);
            }
        });

        hideBothPersons.addClickListener(event -> {
            for (int i = 0; i < 2; i++) {
                //S;
                mainScreenData.personStatus.setLabelVisible(false, null, i);
            }
        });
    }

    /* Main screen */
    MainScreenData mainScreenData;

    public MainScreenView() {
        mainScreenData = MainScreenData.getMainScreenData();

        Component clockController = getClockController();
        Component standingsController = getStandingsController();
        Component advertisementController = getAdvertisementController();

        VerticalLayout mainLeftPanel = new VerticalLayout(clockController, standingsController, advertisementController);
        mainLeftPanel.setSpacing(true);

        Component mainRightPanel = getPersonsController();

        HorizontalLayout mainPanel = new HorizontalLayout(mainLeftPanel, mainRightPanel);
        mainPanel.setExpandRatio(mainLeftPanel, 1);
        mainPanel.setExpandRatio(mainRightPanel, 1);
        mainPanel.setSizeFull();

        //mainPanel.setSpacing(true);

        setCompositionRoot(mainPanel);
    }

//    public String getAdvertisementStatus() {
//        String status = mainScreenData.advertisementStatus.advertisementStatus();
//        String[] s = status.split("\n");
//        return s[1].equals("true") ? "Advertisement \"" + s[2] + "\"" : "No advertisement now";
//    }


    public String getStandingsStatus() {
        StandingsData status = mainScreenData.standingsStatus.standingsStatus();
        if (status.isStandingsVisible) {
            long time = status.standingsType == 0
                    ? (System.currentTimeMillis() - status.standingsTimestamp) / 1000
                    : (status.standingsTimestamp + mainScreenData.standingsStatus.getTotalTime() - System.currentTimeMillis()) / 1000;
            return String.format(labelStatuses[status.standingsType], time);
        }
        return labelStatuses[3];
    }

    public void refresh() {
        clockStatus.setValue(getClockStatus());

//        String status = mainScreenData.standingsStatus.standingsStatus();
//        String[] s = status.split("\n");
//        standingsStatus.setValue(Boolean.parseBoolean(s[1]) ?
//                        (String.format(labelStatuses[Integer.parseInt(s[2])], (System.currentTimeMillis() - Long.parseLong(s[0])) / 1000)) :
//                        labelStatuses[3]
//        );
        standingsStatus.setValue(getStandingsStatus());
        mainScreenData.standingsStatus.update();

        advertisementStatus.setValue(getAdvertisementStatus());
        advertisements.refreshRowCache();
        mainScreenData.advertisementStatus.update();

        for (int i = 0; i < 2; i++) {
            personStatus[i].setValue(getPersonStatus(i));
            persons[i].refreshRowCache();
        }
        mainScreenData.personStatus.update();
    }

    public void enter(ViewChangeEvent event) {

    }


    /* Utils */
    private void setPanelDefaults(VerticalLayout panel) {
        panel.setMargin(new MarginInfo(false, false, false, true));
        panel.setSpacing(true);
    }

    private CssLayout createGroupLayout(Component... components) {
        CssLayout layout = new CssLayout();
        layout.addStyleName("v-component-group");
        layout.addComponents(components);

        return layout;
    }
}
