package ru.ifmo.acm.backend.player.widgets.stylesheets;

import java.awt.*;

/**
 * Created by Aksenov239 on 12.06.2016.
 */
public class QueueStylesheet extends Stylesheet {
    public static PlateStyle acProblem;
    public static PlateStyle waProblem;
    public static PlateStyle udProblem;public static Color udTests;
    public static PlateStyle frozenProblem;
    public static PlateStyle name;

    static {
        acProblem = new PlateStyle("queue.ac");
        waProblem = new PlateStyle("queue.wa");
        udProblem = new PlateStyle("queue.ud");udTests = Color.decode(styles.get("queue.ud.tests"));
        frozenProblem = new PlateStyle("queue.frozen");
        name = new PlateStyle("queue.name");
    }
}
