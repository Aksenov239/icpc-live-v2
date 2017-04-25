package ru.ifmo.acm.backend.player;

import ru.ifmo.acm.backend.player.generator.ScreenGenerator;

/**
 * Created by icpclive on 4/25/2017.
 */
public class Player {
    public static double scale;
    ScreenGenerator generator;

    public Player(ScreenGenerator generator) {
        this.generator = generator;
    }
}
