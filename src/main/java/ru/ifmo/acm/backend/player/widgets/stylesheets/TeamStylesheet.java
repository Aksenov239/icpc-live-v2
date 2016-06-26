package ru.ifmo.acm.backend.player.widgets.stylesheets;

import java.awt.*;

/**
 * Created by Aksenov239 on 12.06.2016.
 */
public class TeamStylesheet extends Stylesheet {
    public static PlateStyle acProblem;
    public static PlateStyle waProblem;
    public static PlateStyle udProblem;
    public static PlateStyle noProblem;
    public static PlateStyle replay;
    public static Color star;

    static {
        acProblem = new PlateStyle("team.ac");
        waProblem = new PlateStyle("team.wa");
        udProblem = new PlateStyle("team.ud");
        noProblem = new PlateStyle("team.no");
        replay = new PlateStyle("team.replay");
        star = Color.decode(Stylesheet.colors.getOrDefault("team.star", "#FFFFA0"));
    }
}
