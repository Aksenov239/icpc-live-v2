package org.icpclive.webadmin.mainscreen;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.VerticalLayout;
import org.icpclive.backend.player.urls.TeamUrls;

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

    public static String getTeamStatus(String status, boolean html) {
        String[] z = status.split("\0");
        String[] current = z[0].split("\n");
        String[] last = z[1].split("\n");

        if (current[1].equals("true")) {
            boolean hasThisType = MainScreenTeamView.STATISTICS_SHOW_TYPE.equals(current[2]);
            for (String type1 : TeamUrls.types) {
                hasThisType |= type1.equals(current[2]);
            }
            if (!hasThisType) {
                return "Type " + current[2] + " does not exist";
            }
            long currentTime = System.currentTimeMillis() - Long.parseLong(current[0]);
            if (currentTime > MainScreenData.getProperties().sleepTime) {
                return "Now showing " + current[2] + " of team " + current[3] + " for " +
                        (currentTime - MainScreenData.getProperties().sleepTime) / 1000 + " seconds" +
                        (html ? "<br><br>" : "");
            } else {
                String result = "buffering " + current[2] + " of team " + current[3] + " for " + currentTime / 1000 + " seconds";                 if (last[1].equals("false")) {
                    return "Now " + result + (html ? "<br><br>" : "");
                }
                long lastTime = System.currentTimeMillis() - Long.parseLong(last[0]);
                return "Now showing " + last[2] + " of team " + last[3] + " for " +
                        (lastTime - MainScreenData.getProperties().sleepTime) / 1000 + " seconds" +
                        (html ? "<br>" : "\n") + "while " + result + (html ? "<br>" : " ");
            }
        } else {
            if (MainScreenData.getMainScreenData().teamStatsData.isVisible) {
                return "Not showing team view of team " + MainScreenData.getMainScreenData().teamStatsData.getTeamString() + ", but probably team stats is shown.";
            }
            return "No team view is shown" + (html ? "<br><br>" : "");
        }
    }
}
