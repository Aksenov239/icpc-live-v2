package org.icpclive.backend.player.widgets.stylesheets;

import java.awt.Color;

/**
 * Created by Aksenov239 on 12.06.2016.
 */
public class BigStandingsStylesheet extends Stylesheet {
    public static PlateStyle name;
    public static PlateStyle acProblem;
    public static PlateStyle waProblem;
    public static PlateStyle udProblem;
    public static PlateStyle noProblem;
    public static PlateStyle problems;
    public static PlateStyle penalty;
    public static PlateStyle heading;
    public static PlateStyle optimisticHeading;
    public static PlateStyle frozenHeading;
    public static PlateStyle topUniversityTeam;
    public static int finalists;
    public static PlateStyle topRegionTeam;
    public static Color star;

    static {
        name = new PlateStyle("big.standings.name");
        acProblem = new PlateStyle("big.standings.ac");
        waProblem = new PlateStyle("big.standings.wa");
        udProblem = new PlateStyle("big.standings.ud");
        noProblem = new PlateStyle("big.standings.no");
        problems = new PlateStyle("big.standings.problems");
        penalty = new PlateStyle("big.standings.penalty");
        heading = new PlateStyle("big.standings.heading");
        optimisticHeading = new PlateStyle("big.standings.optimistic.heading");
        frozenHeading = new PlateStyle("big.standings.frozen.heading");
        topUniversityTeam = new PlateStyle("big.standings.top.university");
        finalists = Integer.parseInt(properties.getProperty("big.standings.finalists", "0"));
        topRegionTeam = new PlateStyle("big.standings.top.region");
        star = Color.decode(styles.getOrDefault("big.standings.star", "#FFFFA0"));
    }
}
