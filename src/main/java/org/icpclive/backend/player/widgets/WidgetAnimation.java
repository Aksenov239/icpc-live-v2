package org.icpclive.backend.player.widgets;

public enum WidgetAnimation {
    NOT_ANIMATED(false, false),
    UNFOLD_ANIMATED(false, false),
    HORIZONTAL_ANIMATED(true, false),
    VERTICAL_ANIMATED(false, true),
    BOTH_ANIMATED(true, true);

    public final boolean isHorizontalAnimated;
    public final boolean isVerticalAnimated;

    WidgetAnimation(boolean isHorizontalAnimated, boolean isVerticalAnimated) {
        this.isHorizontalAnimated = isHorizontalAnimated;
        this.isVerticalAnimated = isVerticalAnimated;
    }
}
