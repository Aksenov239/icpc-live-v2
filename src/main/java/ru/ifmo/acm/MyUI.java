package ru.ifmo.acm;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ChameleonTheme;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.themes.ValoTheme;
import ru.ifmo.acm.login.LoginView;

import javax.servlet.annotation.WebServlet;

/**
 *
 */
@Theme("mytheme")
@Widgetset("ru.ifmo.acm.MyAppWidgetset")
public class MyUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        VerticalLayout rootLayout = new VerticalLayout();
        final MenuBar menu = new MenuBar();
        menu.setStyleName(ValoTheme.MENUBAR_BORDERLESS);
        menu.setVisible(false);

        final VerticalLayout content = new VerticalLayout();
        content.setMargin(false);
        content.setSpacing(true);
        content.setSizeFull();

        rootLayout.addComponents(menu, content);
        rootLayout.setComponentAlignment(menu, Alignment.MIDDLE_CENTER);

        setContent(rootLayout);

        new Navigator(this, content);

        getNavigator().addView(LoginView.NAME, LoginView.class);

        getNavigator().addView(MainView.NAME, MainView.class);

        menu.addItem("Logout", new MenuBar.Command() {
            public void menuSelected(final MenuBar.MenuItem selectedItem) {
                getSession().setAttribute("user", null);
                getNavigator().navigateTo("");
            }
        });

        getNavigator().addViewChangeListener(new ViewChangeListener() {
            public boolean beforeViewChange(ViewChangeEvent e) {
                boolean isLoggedIn = getSession().getAttribute("user") != null;
                boolean isLoginView = e.getNewView() instanceof LoginView;
                if (isLoggedIn && isLoginView) {
                    return false;
                } else if (!isLoggedIn && !isLoginView) {
                    getNavigator().navigateTo(LoginView.NAME);
                    return false;
                }
                if (!isLoginView) {
                    menu.setVisible(true);
                } else {
                    menu.setVisible(false);
                }

                return true;
            }

            public void afterViewChange(ViewChangeEvent e) {
                Notification.show("Navigated to " + e.getViewName());
            }
        });

        getNavigator().navigateTo(LoginView.NAME);
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
