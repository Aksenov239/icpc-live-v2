package org.icpclive.backend.player.widgets.locator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by icpclive on 3/31/2019.
 */
public class LocatorCamera {
    public final String hostName;
    public final File inputFile;
    public final File coordinatesFile;
    public final int cameraID;
    public LocatorPoint[] coordinates;
    private long lastChanged;

    public LocatorCamera(String hostName, File inputFile, File coordinatesFile, int cameraID) throws FileNotFoundException {
        this.hostName = hostName;
        this.inputFile = inputFile;
        this.coordinatesFile = coordinatesFile;
        this.cameraID = cameraID;
        lastChanged = -1;
        update();
    }

    public void update() throws FileNotFoundException {
        long curLastChanged = coordinatesFile.lastModified();
        if (lastChanged == curLastChanged) {
            return;
        }
        lastChanged = curLastChanged;
        Scanner in = new Scanner(coordinatesFile);
        int n = in.nextInt();
        coordinates = new LocatorPoint[n];
        for (int i = 0; i < n; i++) {
            coordinates[i] = new LocatorPoint(in.nextDouble(), in.nextDouble(), in.nextDouble());
        }
    }

    @Override
    public String toString() {
        return "Camera #" + cameraID;
    }
}
