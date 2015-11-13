package ru.ifmo.acm;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;


/**
 * Created by Aksenov239 on 14.11.2015.
 */
public class MainView extends CustomComponent implements View {
    public static final String NAME = "";

    public MainView() {
        Button logout = new Button("Logout", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                getSession().setAttribute("user", null);

                getUI().getNavigator().navigateTo(NAME);
            }
        });

        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(logout);
        setCompositionRoot(layout);
    }

    public void enter(ViewChangeListener.ViewChangeEvent event) {
        String username = String.valueOf(getSession().getAttribute("user"));
        Notification.show("Hello " + username, Type.HUMANIZED_MESSAGE);
    }
}
