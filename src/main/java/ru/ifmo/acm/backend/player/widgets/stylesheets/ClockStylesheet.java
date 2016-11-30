package ru.ifmo.acm.backend.player.widgets.stylesheets;

/**
 * Created by Aksenov239 on 12.06.2016.
 */
public class ClockStylesheet extends Stylesheet {
    public static PlateStyle main;
    public static PlateStyle freeze;

    static {
        main = new PlateStyle("clock");
        freeze = new PlateStyle("clock.freeze");
    }
}
