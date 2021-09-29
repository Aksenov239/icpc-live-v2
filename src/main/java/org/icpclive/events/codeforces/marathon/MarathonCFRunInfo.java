package org.icpclive.events.codeforces.marathon;

import org.icpclive.events.ScoreRunInfo;
import org.icpclive.events.codeforces.CFRunInfo;
import org.icpclive.events.codeforces.api.data.CFSubmission;

public class MarathonCFRunInfo extends CFRunInfo implements ScoreRunInfo {
    public MarathonCFRunInfo(CFSubmission submission) {
        super(submission);
    }

    @Override
    public double getScore() {
        return getPoints();
    }

    @Override
    public double getTotalScore() {
        return getPoints();
    }
}
