package ru.ifmo.acm;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import ru.ifmo.acm.creepingline.CreepingLineView;
import ru.ifmo.acm.datapassing.DataLoader;
import ru.ifmo.acm.datapassing.DataRequestHandler;
import ru.ifmo.acm.login.LoginView;
import ru.ifmo.acm.mainscreen.BreakingNews.MainScreenBreakingNews;
import ru.ifmo.acm.mainscreen.*;
import ru.ifmo.acm.mainscreen.Polls.MainScreenPollView;

import javax.servlet.annotation.WebServlet;

/**
 *
 */
@Theme("mytheme")
@Widgetset("ru.ifmo.acm.MyAppWidgetset")
public class    MyUI extends UI {
    View currentView;

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

        getNavigator().addView(CreepingLineView.NAME, CreepingLineView.class);

        getNavigator().addView(MainScreenView.NAME, MainScreenView.class);

        getNavigator().addView(MainScreenTeamView.NAME, MainScreenTeamView.class);

        getNavigator().addView(MainScreenStandingsView.NAME, MainScreenStandingsView.class);

        getNavigator().addView(MainScreenBreakingNews.NAME, MainScreenBreakingNews.class);

        getNavigator().addView(MainScreenPollView.NAME, MainScreenPollView.class);

        getNavigator().addView(MainScreenSplitScreenView.NAME, MainScreenSplitScreenView.class);

        menu.addItem("Captions", selectedItem -> {
            getNavigator().navigateTo(MainScreenView.NAME);
        });

        menu.addItem("Statistics", selectedItem -> {
            getNavigator().navigateTo(MainScreenStandingsView.NAME);
        });

        menu.addItem("Breaking news", selectedItem -> {
            getNavigator().navigateTo(MainScreenBreakingNews.NAME);
        });

        menu.addItem("Team views", selectedItem -> {
            getNavigator().navigateTo(MainScreenTeamView.NAME);
        });

        menu.addItem("Split screen", selectedItem -> {
            getNavigator().navigateTo(MainScreenSplitScreenView.NAME);
        });

        menu.addItem("Polls", selectedItem -> {
           getNavigator().navigateTo(MainScreenPollView.NAME);
        });

        menu.addItem("Creeping Line", selectedItem -> {
            getNavigator().navigateTo(CreepingLineView.NAME);
        });

        menu.addItem("Logout", selectedItem -> {
            getSession().setAttribute("user", null);
            getNavigator().navigateTo("");
        });

        setPollInterval(3000);

        addPollListener(event -> {
            if (currentView instanceof CreepingLineView) {
                ((CreepingLineView) currentView).refresh();
            }
            if (currentView instanceof MainScreenView) {
                ((MainScreenView) currentView).refresh();
            }
            if (currentView instanceof MainScreenStandingsView) {
                ((MainScreenStandingsView) currentView).refresh();
            }
            if (currentView instanceof MainScreenTeamView) {
                ((MainScreenTeamView) currentView).refresh();
            }
            if (currentView instanceof MainScreenPollView) {
                ((MainScreenPollView) currentView).refresh();
            }
            if (currentView instanceof MainScreenBreakingNews) {
                ((MainScreenBreakingNews) currentView).refresh();
            }
            if (currentView instanceof MainScreenSplitScreenView) {
                ((MainScreenSplitScreenView) currentView).refresh();
            }

            DataLoader.iterateFrontend();
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

                currentView = e.getNewView();

                return true;
            }

            public void afterViewChange(ViewChangeEvent e) {
                Notification.show("Navigated to " + e.getViewName());
            }
        });

        getNavigator().navigateTo(LoginView.NAME);

        VaadinSession.getCurrent().addRequestHandler(new DataRequestHandler());
        //new DataLoader().frontendInitialize();
    }

    //}

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
