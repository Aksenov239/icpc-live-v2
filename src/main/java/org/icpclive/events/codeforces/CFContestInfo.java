package org.icpclive.events.codeforces;

import org.icpclive.datapassing.StandingsData;
import org.icpclive.events.AnalystMessage;
import org.icpclive.events.ContestInfo;
import org.icpclive.events.RunInfo;
import org.icpclive.events.TeamInfo;
import org.icpclive.events.codeforces.api.data.*;
import org.icpclive.events.codeforces.api.results.CFStandings;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.util.Arrays.sort;

/**
 * @author egor@egork.net
 */
public class CFContestInfo extends ContestInfo {
    private BlockingQueue<AnalystMessage> messageQueue = new ArrayBlockingQueue<AnalystMessage>(1);
    private CFStandings standings;
    private final Map<Integer, CFRunInfo> runsById = new HashMap<>();
    private Map<Integer, List<CFRunInfo>[]> runsByTeam = new HashMap<>();
    private Map<String, CFProblemInfo> problemsMap = new HashMap<>();
    private Map<String, CFTeamInfo> participantsByName = new HashMap<>();
    private Map<Integer, CFTeamInfo> participantsById = new HashMap<>();
    private CFRunInfo[] firstSolved;
    private int nextParticipantId = 1;

    @Override
    public CFTeamInfo getParticipant(String name) {
        return participantsByName.get(name);
    }

    @Override
    public CFTeamInfo getParticipant(int id) {
        return participantsById.get(id);
    }

    @Override
    public CFTeamInfo getParticipantByHashTag(String hashTag) {
        return null;
    }

    @Override
    public CFTeamInfo[] getStandings() {
        if (this.standings == null) {
            return new CFTeamInfo[0];
        }
        CFTeamInfo[] standings = new CFTeamInfo[this.standings.rows.size()];
        int i = 0;
        for (CFRanklistRow row : this.standings.rows) {
            standings[i++] = participantsByName.get(getName(row.party));
        }
        return standings;
    }

    public static String getName(CFParty party) {
        return party.teamName == null ? party.members.get(0).handle : party.teamName;
    }

    @Override
    public TeamInfo[] getStandings(StandingsData.OptimismLevel optimismLevel) {
        return getStandings();
    }

    @Override
    public long[] firstTimeSolved() {
        long[] result = new long[problemsMap.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = firstSolved[i] == null ? 0 : firstSolved[i].getTime();
        }
        return result;
    }

    @Override
    public CFRunInfo[] firstSolvedRun() {
        return firstSolved;
    }

    @Override
    public CFRunInfo[] getRuns() {
        synchronized (runsById) {
            CFRunInfo[] cfRunInfos = runsById.values().toArray(new CFRunInfo[0]);
            sort(cfRunInfos, (a, b) -> Long.compare(a.getTime(), b.getTime()));
            return cfRunInfos;
        }
    }

    @Override
    public RunInfo getRun(int id) {
        synchronized (runsById) {
            return runsById.get(id);
        }
    }

    @Override
    public BlockingQueue<AnalystMessage> getAnalystMessages() {
        return messageQueue;
    }

    @Override
    public int getLastRunId() {
        return runsById.size() - 1;
    }

    public void update(CFStandings standings, List<CFSubmission> submissions) {
        if (problemsMap.isEmpty() && !standings.problems.isEmpty()) {
            int id = 0;
            for (CFProblem problem : standings.problems) {
                CFProblemInfo problemInfo = new CFProblemInfo(problem, id++);
                problemsMap.put(problem.index, problemInfo);
                problems.add(problemInfo);
            }
            firstSolved = new CFRunInfo[id];
            problemNumber = id;
        }
        this.standings = standings;
        lastTime = standings.contest.relativeTimeSeconds;
        CFContest.CFContestPhase phase = standings.contest.phase;
        status = phase == CFContest.CFContestPhase.BEFORE ? Status.BEFORE : phase == CFContest.CFContestPhase.CODING ? Status.RUNNING : Status.OVER;
        for (CFRanklistRow row : standings.rows) {
            CFTeamInfo teamInfo = new CFTeamInfo(row);
            if (participantsByName.containsKey(teamInfo.getName())) {
                teamInfo.setId(participantsByName.get(teamInfo.getName()).getId());
            } else {
                runsByTeam.put(nextParticipantId, createEmptyRunsArray());
                teamInfo.setId(nextParticipantId++);
            }
            participantsByName.put(teamInfo.getName(), teamInfo);
            participantsById.put(teamInfo.getId(), teamInfo);
        }
        teamNumber = standings.rows.size();
        synchronized (runsById) {
            for (CFSubmission submission : submissions) {
                if (submission.author.participantType != CFParty.CFPartyParticipantType.CONTESTANT || !participantsByName.containsKey(getName(submission.author))) {
                    continue;
                }
                CFRunInfo runInfo;
                boolean isNew;
                if (runsById.containsKey((int) submission.id)) {
                    runInfo = runsById.get((int) submission.id);
                    runInfo.updateFrom(submission, standings.contest.relativeTimeSeconds);
                    isNew = false;
                } else {
                    runInfo = new CFRunInfo(submission);
                    runsById.put(runInfo.getId(), runInfo);
                    isNew = true;
                }
                if (isNew) {
                    addRun(runInfo, runInfo.getProblemId());
                }
                if (runInfo.isAccepted()) {
                    int pid = runInfo.getProblemId();
                    if (firstSolved[pid] == null || firstSolved[pid].getTime() > runInfo.getTime()) {
                        firstSolved[pid] = runInfo;
                    }
                }
            }
        }
    }

    private List<CFRunInfo>[] createEmptyRunsArray() {
        List<CFRunInfo>[] array = new List[problemsMap.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = new ArrayList<>();
        }
        return array;
    }

    public CFProblemInfo getProblem(CFProblem problem) {
        return problemsMap.get(problem.index);
    }

    public void addRun(CFRunInfo run, int problem) {
        List<CFRunInfo> runs = getRuns(run.getSubmission().author)[problem];
        synchronized (runs) {
            runs.add(run);
            run.getProblem().update(run);
        }
    }

    public List<CFRunInfo>[] getRuns(CFParty party) {
        return runsByTeam.get(participantsByName.get(getName(party)).getId());
    }
}
