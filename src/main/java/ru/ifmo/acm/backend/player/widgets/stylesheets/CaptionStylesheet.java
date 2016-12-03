package ru.ifmo.acm.backend.player.widgets.stylesheets;

import java.awt.*;

/**
 * Created by Aksenov239 on 12.06.2016.
 */
public class CaptionStylesheet extends Stylesheet {
    public static PlateStyle main;
    public static PlateStyle description;

    static {
        main = new PlateStyle("caption.main");
        description = new PlateStyle("caption.description");
    }
}
