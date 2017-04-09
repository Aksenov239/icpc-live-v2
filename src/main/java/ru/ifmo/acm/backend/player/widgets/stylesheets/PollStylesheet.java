package ru.ifmo.acm.backend.player.widgets.stylesheets;

/**
 * Created by Aksenov239 on 09.04.2017.
 */
public class PollStylesheet {
    public static PlateStyle question;
    public static PlateStyle hashtag;
    public static PlateStyle option;
    public static PlateStyle votes;

    static {
        question = new PlateStyle("polls.question");
        hashtag = new PlateStyle("polls.hashtag");
        option = new PlateStyle("polls.option");
        votes = new PlateStyle("polls.votes");
    }
}
