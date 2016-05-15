package ru.ifmo.acm.datapassing;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public interface CachedData {
    CachedData initialize();

    default String checkOverlays() {
        return null;
    }

    default void switchOverlaysOff() {
    }

    default void hide() {
    }

    default String getOverlayError() {
        return "";
    }
}
