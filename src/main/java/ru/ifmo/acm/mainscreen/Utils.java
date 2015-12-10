package ru.ifmo.acm.mainscreen;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.VerticalLayout;

public class Utils {
    /* Utils */
    public static void setPanelDefaults(VerticalLayout panel) {
        panel.setMargin(new MarginInfo(false, false, false, true));
        panel.setSpacing(true);
    }

    public static CssLayout createGroupLayout(Component... components) {
        CssLayout layout = new CssLayout();
        layout.addStyleName("v-component-group");
        layout.addComponents(components);

        return layout;
    }

    public static abstract class StoppedRunnable implements Runnable {
        public volatile boolean stop;
    }

    public static class StoppedThread extends Thread {
        private StoppedRunnable runnable;

        public StoppedThread(StoppedRunnable runnable) {
            super(runnable);
            this.runnable = runnable;
        }

        public void interrupt() {
            runnable.stop = true;
            super.interrupt();
        }
    }
}
