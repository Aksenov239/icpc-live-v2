package org.icpclive.backend.player.widgets.stylesheets;

import java.awt.*;

/**
 * Created by Aksenov239 on 09.04.2017.
 */
public class PollStylesheet {
    public static PlateStyle question;
    public static PlateStyle hashtag;
    public static PlateStyle option;
    public static PlateStyle votes;
    public static Color background;

    static {
        question = new PlateStyle("polls.question");
        hashtag = new PlateStyle("polls.hashtag");
        option = new PlateStyle("polls.option");
        votes = new PlateStyle("polls.votes");
        background = Color.decode(Stylesheet.styles.getOrDefault("polls.background", "#000000"));
    }
}
