package org.icpclive.backend.player.widgets.locator;

import java.io.File;

/**
 * Created by icpclive on 3/31/2019.
 */
public class LocatorCamera {
    public final String hostName;
    public final File inputFile;
    public final File coordinatesFile;
    public final int cameraID;

    public LocatorCamera(String hostName, File inputFile, File coordinatesFile, int cameraID) {
        this.hostName = hostName;
        this.inputFile = inputFile;
        this.coordinatesFile = coordinatesFile;
        this.cameraID = cameraID;
    }

    @Override
    public String toString() {
        return "Camera #" + cameraID;
    }
}
