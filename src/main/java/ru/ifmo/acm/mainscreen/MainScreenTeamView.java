package ru.ifmo.acm.mainscreen;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import ru.ifmo.acm.events.EventsLoader;
import ru.ifmo.acm.events.PCMS.PCMSEventsLoader;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class MainScreenTeamView extends CustomComponent implements View {
    public static String NAME = "mainscreen-team";

    Label teamStatus;
    Button teamShow;
    Button teamHide;
    final String[] types = {"screen", "camera", "info"};
    ComboBox type;
    ListSelect teamSelection;
    String[] teams;

    public Component getControllerTeam() {
        teamStatus = new Label("");

        EventsLoader loader = new PCMSEventsLoader();

        return null;
    }

    /* Main screen */
    MainScreenData mainScreenData;

    public MainScreenTeamView() {
        mainScreenData = MainScreenData.getMainScreenData();

        Component teamInfoComponent = getControllerTeam();
    }

    public void refresh() {

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
