package org.icpclive.backend.player.widgets.stylesheets;

/**
 * Created by Aksenov239 on 12.06.2016.
 */
public class StatisticsStylesheet extends Stylesheet {            public static PlateStyle problemAlias;
    public static PlateStyle acProblem;
    public static PlateStyle waProblem;
    public static PlateStyle udProblem;
    public static PlateStyle header;

    static {
        problemAlias = new PlateStyle("statistics.problem.alias");
        acProblem = new PlateStyle("statistics.ac");
        waProblem = new PlateStyle("statistics.wa");
        udProblem = new PlateStyle("statistics.ud");
        header = new PlateStyle("statistics.header");
    }
}
