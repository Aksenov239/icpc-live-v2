package ru.ifmo.acm.mainscreen;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import ru.ifmo.acm.datapassing.MainScreenProperties;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class MainScreenCameraView extends CustomComponent implements View {
    public static String NAME = "mainscreen-camera";

    Label cameraStatus;
    Button[] cameraButtons;

    private String getCameraStatus() {
        String status = mainScreenData.cameraData.cameraStatus();
        String[] z = status.split("\n");

        ;

        return "Now showing " + z[1] + " for " + (System.currentTimeMillis() - Long.parseLong(z[0])) / 1000 + " seconds";
    }

    public Component getControllerTeam() {
        cameraStatus = new Label("Camera " + getCameraStatus());

        cameraButtons = new Button[MainScreenProperties.cameraNumber];

        for (int i = 0; i < cameraButtons.length; i++) {
            cameraButtons[i] = new Button(MainScreenProperties.cameraNames[i]);
            final int id = i;
            cameraButtons[i].addClickListener(event -> {
                if (!mainScreenData.cameraData.setCameraNumber(id)) {
                    Notification.show("You need to wait 30 seconds first");
                }
            });
        }

        HorizontalLayout buttons = new HorizontalLayout(cameraButtons);
        VerticalLayout result = new VerticalLayout(cameraStatus, buttons);
        result.setSpacing(true);
        result.setSizeFull();
        result.setHeight("100%");

        return result;
    }

    /* Main screen */
    MainScreenData mainScreenData;

    public MainScreenCameraView() {
        mainScreenData = MainScreenData.getMainScreenData();

        Component teamInfoComponent = getControllerTeam();

        Component main = teamInfoComponent;
        main.setSizeFull();
        main.setHeight("100%");

        setCompositionRoot(main);
    }

    public void refresh() {
        cameraStatus.setValue(getCameraStatus());
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
