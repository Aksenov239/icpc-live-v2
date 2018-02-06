package org.icpclive.backend.player;

import org.icpclive.backend.player.generator.ScreenGenerator;

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
