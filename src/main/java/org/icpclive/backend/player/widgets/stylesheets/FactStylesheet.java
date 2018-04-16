package org.icpclive.backend.player.widgets.stylesheets;

/**
 * Created by vaksenov on 13.04.2018.
 */
public class FactStylesheet {
    public static PlateStyle title;
    public static PlateStyle text;

    static {
        title = new PlateStyle("fact.title");
        text = new PlateStyle("fact.text");
    }
}
