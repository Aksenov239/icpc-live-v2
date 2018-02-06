package org.icpclive.mainscreen;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import org.icpclive.creepingline.MessageData;

/**
 * Created by Aksenov239 on 15.11.2015.
 */
public class MainScreenView extends CustomComponent implements View {
    public static String NAME = "mainscreen";

    /* Advertisements */
    Label advertisementStatus;
    TextField advertisementText;
    Button addAdvertisement;
    Button removeAdvertisement;
    Button discardAdvertisement;
    Button showAdvertisement;
    Button hideAdvertisement;
    Table advertisements;
    String[] addButtonStatuses = {"Add new", "Edit"};

    public Component getAdvertisementController() {
        advertisementStatus = new Label(getAdvertisementStatus());
        advertisementStatus.addStyleName("large");

        advertisementText = new TextField("Advertisement text: ");

        createAddAdvertisementButton();
        createRemoveAdvertisementButton();
        createDiscardAdvertisementButton();

        CssLayout groupAdd = Utils.createGroupLayout(advertisementText, addAdvertisement, removeAdvertisement, discardAdvertisement);

        advertisements = createAdvertisementTable(mainScreenData.advertisementData.getContainer());
        advertisements.addValueChangeListener(event -> {
            if (advertisements.getValue() == null) {
                setDefaultValues();
                return;
            }
            addAdvertisement.setCaption(addButtonStatuses[1]);
            removeAdvertisement.setVisible(true);
            discardAdvertisement.setVisible(true);
            advertisementText.setValue(((Advertisement) advertisements.getValue()).getAdvertisement());
        });


        createShowAdvertisementButton();
        createHideAdvertisementButton();

        CssLayout groupControl = Utils.createGroupLayout(showAdvertisement, hideAdvertisement);

        VerticalLayout panel = new VerticalLayout(
                advertisementStatus,
                groupAdd,
                advertisements,
                groupControl
        );
        Utils.setPanelDefaults(panel);
        return panel;
    }

    /*Small Creeping line */
    Label creepingLineLogoStatus;
    TextField creepingLineLogoText;

    // final static BeanItemContainer<Advertisement> logosContainer = new SynchronizedBeanItemContainer<>(Advertisement.class);
    final static BeanItemContainer<Advertisement> logosContainer = MessageData.getMessageData().logosList.getContainer();
    Table creepingLineLogos;

    Button addCreepingLineLogoButton;
    Button removeCreepingLineLogoButton;
    Button discardCreepingLineLogoButton;

    public Component getCreepingLineLogoController() {
        creepingLineLogoStatus = new Label("Creeping line logos");
        creepingLineLogoStatus.addStyleName("large");
        creepingLineLogoText = new TextField("Logo: ");

        creepingLineLogos = createAdvertisementTable(logosContainer);
        creepingLineLogos.addValueChangeListener(event -> {
            if (creepingLineLogos.getValue() == null) {
                setCreepingLineLogoDefaultValues();
                return;
            }
            addCreepingLineLogoButton.setCaption(addButtonStatuses[1]);
            removeCreepingLineLogoButton.setVisible(true);
            discardCreepingLineLogoButton.setVisible(true);
            creepingLineLogoText.setValue(((Advertisement) creepingLineLogos.getValue()).getAdvertisement());
        });

        createAddCreepingLineLogoButton();
        createRemoveCreepingLineLogoButton();
        createDiscardCreepingLineLogoButton();

        CssLayout groupAdd = Utils.createGroupLayout(creepingLineLogoText,
                addCreepingLineLogoButton, removeCreepingLineLogoButton, discardCreepingLineLogoButton);

        VerticalLayout panel = new VerticalLayout(
                creepingLineLogoStatus,
                groupAdd,
                creepingLineLogos
        );
        Utils.setPanelDefaults(panel);
        return panel;
    }

    private void createAddCreepingLineLogoButton() {
        addCreepingLineLogoButton = new Button(addButtonStatuses[0]);
        addCreepingLineLogoButton.addClickListener(event -> {
            if (addCreepingLineLogoButton.getCaption().equals(addButtonStatuses[0])) {
                MessageData.getMessageData().addLogo(new Advertisement(creepingLineLogoText.getValue()));
            } else {
                MessageData.getMessageData().setLogoValue(creepingLineLogos.getValue(), creepingLineLogoText.getValue());
                setCreepingLineLogoDefaultValues();
            }
            creepingLineLogoText.clear();
            creepingLineLogos.refreshRowCache();
        });
    }

    private void createRemoveCreepingLineLogoButton() {
        removeCreepingLineLogoButton = new Button("Remove selected");
        removeCreepingLineLogoButton.addClickListener(event -> {
            if (creepingLineLogos.getValue() != null) {
                MessageData.getMessageData().removeLogo((Advertisement) creepingLineLogos.getValue());
                creepingLineLogos.refreshRowCache();
            } else {
                Notification.show("You should choose logo", Type.ERROR_MESSAGE);
            }
            creepingLineLogoText.setValue("");
            setCreepingLineLogoDefaultValues();
        });
        removeCreepingLineLogoButton.setVisible(false);
    }

    private void createDiscardCreepingLineLogoButton() {
        discardCreepingLineLogoButton = new Button("Discard selected");
        discardCreepingLineLogoButton.addClickListener(event -> {
            creepingLineLogos.setValue(null);
            creepingLineLogoText.setValue("");
            setCreepingLineLogoDefaultValues();
        });
        discardCreepingLineLogoButton.setVisible(false);
    }

    private void setCreepingLineLogoDefaultValues() {
        creepingLineLogos.setValue(null);
        addCreepingLineLogoButton.setCaption(addButtonStatuses[0]);
        removeCreepingLineLogoButton.setVisible(false);
        discardCreepingLineLogoButton.setVisible(false);
    }

    public String getAdvertisementStatus() {
        return mainScreenData.advertisementData.toString();
//        AdvertisementData adv = mainScreenData.advertisementStatus.advertisementStatus();
//        return adv.isVisible ? "Advertisement \"" + adv.advertisement.getAdvertisement() + "\"" : "No advertisement now";
        //return s[1].equals("true") ? "Advertisement \"" + s[2] + "\"" : "No advertisement now";
    }

    private void setDefaultValues() {
        advertisements.setValue(null);
        addAdvertisement.setCaption(addButtonStatuses[0]);
        removeAdvertisement.setVisible(false);
        discardAdvertisement.setVisible(false);
    }

    private Table createAdvertisementTable(BeanItemContainer<Advertisement> container) {
        Table table = new Table();
        table.setContainerDataSource(container);
        table.setSelectable(true);
        table.setEditable(false);
        table.setSizeFull();
        table.setPageLength(0);

        return table;
    }

    private void createAddAdvertisementButton() {
        addAdvertisement = new Button(addButtonStatuses[0]);
        addAdvertisement.addClickListener(event -> {
            if (addAdvertisement.getCaption().equals(addButtonStatuses[0])) {
                mainScreenData.advertisementData.addAdvertisement(new Advertisement(advertisementText.getValue()));
            } else {
                mainScreenData.advertisementData.setValue(advertisements.getValue(), advertisementText.getValue());
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
                mainScreenData.advertisementData.removeAdvertisement((Advertisement) advertisements.getValue());
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
                String outcome = mainScreenData.advertisementData.setAdvertisementVisible(true, (Advertisement) advertisements.getValue());
                if (outcome != null) {
                    Notification.show(outcome, Notification.Type.WARNING_MESSAGE);
                    return;
                }
                advertisementStatus.setValue(getAdvertisementStatus());
            } else {
                Notification.show("You should choose advertisement", Type.ERROR_MESSAGE);
            }
        });
    }

    private void createHideAdvertisementButton() {
        hideAdvertisement = new Button("Hide advertisement");
        hideAdvertisement.addClickListener(event -> {
            mainScreenData.advertisementData.setAdvertisementVisible(false, (Advertisement) advertisements.getValue());
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

        Component showControl = Utils.createGroupLayout(showPerson[id], hidePerson[id]);
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

        Component buttonPersonsControl = Utils.createGroupLayout(addPersonButton, removePersonButton, discardPersonButton);

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
        Component bothControl = Utils.createGroupLayout(showBothPersons, hideBothPersons);

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
        String[] z = mainScreenData.personData.labelStatus(id).split("\n");
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
                mainScreenData.personData.addPerson(new Person(name.getValue(), profession.getValue()));
                setPersonFormDefault();
            } else {
                if (lastPerson != null) {
                    mainScreenData.personData.setValue(lastPerson, "name", name.getValue());
                    mainScreenData.personData.setValue(lastPerson, "position", profession.getValue());
                }
                setPersonFormDefault();
            }
        });
    }

    public void createRemovePersonButton() {
        removePersonButton = new Button("Delete");
        removePersonButton.addClickListener(event -> {
            if (lastPerson != null) {
                mainScreenData.personData.removePerson(lastPerson);
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
        table.setContainerDataSource(mainScreenData.personData.getContainer());
        table.setSelectable(true);
        table.setEditable(false);
        table.setSizeFull();
        table.setPageLength(0);

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
                String outcome = mainScreenData.personData.setLabelVisible(true, (Person) persons[id].getValue(), id);
                if (outcome != null) {
                    Notification.show(outcome, Notification.Type.WARNING_MESSAGE);
                    return;
                }
            } else {
                Notification.show("You need to choose " + captionPersons[id] + " person", Type.WARNING_MESSAGE);
            }
        });
        hidePerson[id] = new Button("Hide " + captionPersons[id] + " person");
        hidePerson[id].addClickListener(event -> mainScreenData.personData.setLabelVisible(false, null, id));
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
                String outcome = mainScreenData.personData.setLabelVisible(true, (Person) persons[i].getValue(), i);
                if (outcome != null) {
                    Notification.show(outcome, Notification.Type.WARNING_MESSAGE);
                    return;
                }
            }
        });

        hideBothPersons.addClickListener(event -> {
            for (int i = 0; i < 2; i++) {
                //S;
                mainScreenData.personData.setLabelVisible(false, null, i);
            }
        });
    }

    /* Main screen */
    MainScreenData mainScreenData;

    public MainScreenView() {
        mainScreenData = MainScreenData.getMainScreenData();

        Component advertisementController = getAdvertisementController();
        Component personController = getPersonsController();
        Component creepingLineController = getCreepingLineLogoController();
        HorizontalLayout horizontalPanel = new HorizontalLayout(advertisementController, personController);
        horizontalPanel.setSizeFull();
        VerticalLayout mainPanel = new VerticalLayout(horizontalPanel, creepingLineController);
        mainPanel.setSizeFull();
        setCompositionRoot(mainPanel);
    }

    public void refresh() {
        advertisementStatus.setValue(getAdvertisementStatus());
        advertisements.refreshRowCache();
        //mainScreenData.advertisementStatus.update();

        for (int i = 0; i < 2; i++) {
            personStatus[i].setValue(getPersonStatus(i));
            persons[i].refreshRowCache();
        }
        //mainScreenData.personStatus.update();

//        mainScreenData.update();
    }

    public void enter(ViewChangeEvent event) {

    }
}
