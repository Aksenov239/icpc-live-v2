package org.icpclive.webadmin.login;

import com.vaadin.navigator.View;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.themes.Reindeer;
import org.icpclive.webadmin.MainView;
import com.vaadin.event.ShortcutAction;

/**
 * Created by Aksenov239 on 14.11.2015.
 */
public class LoginView extends CustomComponent implements View {
    public static final String NAME = "login";

    private final TextField user;
    private final PasswordField password;
    private final Button loginButton;

    private final LoginData data;

    public LoginView() {
        data = LoginData.getLoginData();

        setSizeFull();

        user = new TextField("User:");
        user.setWidth("300px");
        user.setRequired(true);

        password = new PasswordField("Password:");
        password.setWidth("300px");
        password.setRequired(true);
        password.setNullRepresentation("");

        loginButton = new Button("Login", new Button.ClickListener() {
            public void buttonClick(ClickEvent e) {
                String username = user.getValue();
                String pwd = password.getValue();
                if (data.check(username, pwd) ) {
                    getSession().setAttribute("user", username);
                    getUI().getNavigator().navigateTo(MainView.NAME);
                } else {
                    password.setValue(null);
                    password.focus();
                }
            }
        });

        loginButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        VerticalLayout fields = new VerticalLayout(user, password, loginButton);
        fields.setSizeUndefined();
        fields.setSpacing(true);
        fields.setMargin(true);

        VerticalLayout viewLayout = new VerticalLayout(fields);
        viewLayout.setSizeFull();
        viewLayout.setComponentAlignment(fields, Alignment.MIDDLE_CENTER);
        viewLayout.setStyleName(Reindeer.LAYOUT_BLUE);
        setCompositionRoot(viewLayout);
    }

    public void enter(ViewChangeEvent event) {
        user.focus();
    }
}
