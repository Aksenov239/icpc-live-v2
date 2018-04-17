package org.icpclive.events.WF.json;

import org.icpclive.events.WF.WFRunInfo;
import org.icpclive.events.WF.WFTeamInfo;

import java.util.HashMap;

/**
 * Created by Aksenov239 on 3/5/2018.
 */
public class WFContestInfo extends org.icpclive.events.WF.WFContestInfo {
    public WFContestInfo() {
        problemById = new HashMap<>();
        teamById = new HashMap<>();
        languageById = new HashMap<>();
        runBySubmissionId = new HashMap<>();
        runByJudgementId = new HashMap<>();
    }

    public void initializationFinish() {
        problemNumber = problems.size();
        teamNumber = teamInfos.length;
        timeFirstSolved = new long[problemNumber];
        runs = new WFRunInfo[1000000];
        firstSolvedRun = new WFRunInfo[problemNumber];
    }

    // Groups
    HashMap<String, String> groupById;

    // Problems
    HashMap<String, WFProblemInfo> problemById;

    // Teams
    HashMap<String, WFTeamInfo> teamById;

    // Languages
    WFLanguageInfo[] languages;
    HashMap<String, WFLanguageInfo> languageById;

    // Submissions
    HashMap<String, WFRunInfo> runBySubmissionId;
    HashMap<String, WFRunInfo> runByJudgementId;

    public void addRun(WFRunInfo runInfo) {
        runInfo.id = maxRunId + 1;
        runs[runInfo.id] = runInfo;
        teamInfos[runInfo.teamId].addRun(runInfo, runInfo.problemId);
        getProblemById(runInfo.problemId).submissions[runInfo.languageId]++;
        maxRunId++;
    }

    public WFTeamInfo getTeamByCDSId(String cdsId) {
        return teamById.get(cdsId);
    }
}
