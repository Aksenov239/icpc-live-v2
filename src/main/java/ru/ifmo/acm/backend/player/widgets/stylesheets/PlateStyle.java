package ru.ifmo.acm.backend.player.widgets.stylesheets;

import java.awt.*;

/**
 * Created by Aksenov239 on 12.06.2016.
 */
public class PlateStyle {
    public Color background;
    public Color text;

    public PlateStyle(Color background, Color text) {
        this.background = background;
        this.text = text;
    }

    public PlateStyle brighter() {
        if (background.equals(background.brighter())) {
            return new PlateStyle(background.darker(), text);
        }
        return new PlateStyle(background.brighter(), text);
    }

    public PlateStyle darker() {
        if (background.equals(background.darker())) {
            return new PlateStyle(background.brighter(), text);
        }
        return new PlateStyle(background.darker(), text);
    }

    public PlateStyle(String styleName) {
        background = Color.decode(Stylesheet.colors.getOrDefault(styleName + ".background", "#000000"));
        text = Color.decode(Stylesheet.colors.getOrDefault(styleName + ".text", "#FFFFFF"));
    }


}
