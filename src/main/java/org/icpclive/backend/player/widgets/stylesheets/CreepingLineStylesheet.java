package org.icpclive.backend.player.widgets.stylesheets;

/**
 * Created by Aksenov239 on 12.06.2016.
 */
public class CreepingLineStylesheet extends Stylesheet {
    public static PlateStyle main;
    public static PlateStyle logo;
    public static PlateStyle logoBefore;

    static {
        main = new PlateStyle("creeping.line.main");
        logo = new PlateStyle("creeping.line.logo");
        logoBefore = new PlateStyle("creeping.line.logo.before");
    }
}
