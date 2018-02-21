package org.icpclive.backend.player.widgets.stylesheets;

import java.awt.*;

/**
 * Created by Aksenov239 on 12.06.2016.
 */
public class PlateStyle {
    public Color background;
    public Color text;
    public double opacity;
    public Alignment alignment;

    @Deprecated
    public RectangleType rectangleType;

    public PlateStyle(Color background, Color text, RectangleType rectangleType, Alignment alignment, double opacity) {
        this.background = background;
        this.text = text;
        this.rectangleType = rectangleType;
        this.alignment = alignment;
        this.opacity = opacity;
    }

    public PlateStyle brighter() {
//        if (background.equals(background.brighter())) {
//            return new PlateStyle(background.darker(), text, rectangleType, alignment, opacity);
//        }
        return new PlateStyle(brighter(background), text, rectangleType, alignment, opacity);
    }

    public PlateStyle darker() {
        return new PlateStyle(background.darker(), text, rectangleType, alignment, opacity);
    }

    public Color brighter(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int alpha = color.getAlpha();

        r = 255 - (255 - r) * 85 / 100;
        g = 255 - (255 - g) * 85 / 100;
        b = 255 - (255 - b) * 85 / 100;

//        if ( r > 0 && r < i ) r = i;
//        if ( g > 0 && g < i ) g = i;
//        if ( b > 0 && b < i ) b = i;

        return new Color(r, g, b, alpha);
    }


    public PlateStyle(String styleName) {
        background = Color.decode(Stylesheet.styles.getOrDefault(styleName + ".background", "#000000"));
        text = Color.decode(Stylesheet.styles.getOrDefault(styleName + ".text", "#FFFFFF"));
        opacity = Double.parseDouble(Stylesheet.styles.getOrDefault(styleName + ".opacity", "1"));
        rectangleType = RectangleType.SOLID;
//        rectangleType = Graphics.RectangleType.valueOf(Stylesheet.styles.getOrDefault(styleName + ".style", "SOLID_ROUNDED").toUpperCase());
        alignment = Alignment.valueOf(Stylesheet.styles.getOrDefault(styleName + ".align", "Left").toUpperCase());
    }


    public enum RectangleType {
        SOLID_ROUNDED,
        SOLID,
        ITALIC
    }

    public enum Alignment {
        LEFT,
        CENTER,
        RIGHT
    }
}
