package ru.ifmo.acm.backend.player.widgets.stylesheets;

/**
 * Created by Aksenov239 on 12.06.2016.
 */
public class TeamPaneStylesheet extends Stylesheet {
    public static PlateStyle gold;
    public static PlateStyle silver;
    public static PlateStyle bronze;
    public static PlateStyle none;
    public static PlateStyle zero;
    public static int goldPlaces;
    public static int silverPlaces;
    public static int bronzePlaces;
    public static PlateStyle name;
    public static PlateStyle problems;
    public static PlateStyle penalty;

    static {
        gold = new PlateStyle("team.pane.rank.gold");
        silver = new PlateStyle("team.pane.rank.silver");
        bronze = new PlateStyle("team.pane.rank.bronze");
        none = new PlateStyle("team.pane.rank");zero = new PlateStyle("team.pane.rank.zero");
        goldPlaces = Integer.parseInt(properties.getProperty("team.pane.rank.gold.places", "4"));
        silverPlaces = Integer.parseInt(properties.getProperty("team.pane.rank.silver.places", "4"));
        bronzePlaces = Integer.parseInt(properties.getProperty("team.pane.rank.bronze.places", "4"));
    }
}
