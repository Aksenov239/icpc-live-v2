package org.icpclive.backend.player.widgets.stylesheets;

import java.awt.*;

/**
 * Created by Aksenov239 on 12.06.2016.
 */
public class TeamStatsStylesheet extends Stylesheet {
    public static Color background;

    static {
        background = Color.decode(styles.getOrDefault("stats.background", "#000000"));
    }
}
