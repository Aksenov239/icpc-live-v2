package org.icpclive.events.WF.json;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.Config;
import org.icpclive.backend.Preparation;
import org.icpclive.events.ContestInfo;
import org.icpclive.events.EventsLoader;
import org.icpclive.events.WF.WFRunInfo;
import org.icpclive.events.WF.json.WFTeamInfo;
import org.icpclive.events.WF.WFTestCaseInfo;
import org.icpclive.events.WF.WFAnalystMessage;

import java.awt.*;
import java.io.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by aksenov on 16.04.2015.
 */
public class WFEventsLoader extends EventsLoader {
    private static final Logger log = LogManager.getLogger(WFEventsLoader.class);

    private static WFContestInfo contestInfo;

    private String url;
    private String login;
    private String password;

    private boolean emulation;

    public WFEventsLoader() {
        try {
            Properties properties = Config.loadProperties("events");

            login = properties.getProperty("login");
            password = properties.getProperty("password");

            Preparation.prepareNetwork(login, password);

            // in format https://example.com/api/contests/wf14/
            url = properties.getProperty("url");
            emulationSpeed = Double.parseDouble(properties.getProperty("emulation.speed", "1"));
            emulationStartTime = Long.parseLong(properties.getProperty("emulation.startTime", "0"));

            if (!(url.startsWith("http") || url.startsWith("https"))) {
                emulation = true;
            } else {
                emulationSpeed = 1;
            }

            initialize();
        } catch (IOException e) {
            log.error("error", e);
        }
    }

    public ContestInfo getContestData() {
        return contestInfo;
    }

    public String readJsonArray(String url) throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Preparation.openAuthorizedStream(url, login, password)));
        String json = "";
        String line;
        while ((line = br.readLine()) != null) {
            json += line.trim();
        }
        return json;
    }

    private void readGroupsInfo(WFContestInfo contest) throws IOException {
        JsonArray jsonGroups = new Gson().fromJson(
                readJsonArray(url + "/groups"), JsonArray.class);
        contest.groupById = new HashMap<>();
        for (int i = 0; i < jsonGroups.size(); i++) {
            JsonObject je = jsonGroups.get(i).getAsJsonObject();
            String id = je.get("id").getAsString();
            String name = je.get("name").getAsString();
            contest.groupById.put(id, name);
            ContestInfo.GROUPS.add(name);
        }
    }

    private void readProblemInfos(WFContestInfo contest) throws IOException {
        JsonArray jsonProblems = new Gson().fromJson(
                readJsonArray(url + "/problems"), JsonArray.class);
        contest.problems = new ArrayList<>();
        contest.problemById = new HashMap<>();
        contest.problemById = new HashMap<>();
        WFProblemInfo[] problems = new WFProblemInfo[jsonProblems.size()];
        for (int i = 0; i < jsonProblems.size(); i++) {
            JsonObject je = jsonProblems.get(i).getAsJsonObject();

            WFProblemInfo problemInfo = new WFProblemInfo(contest.languages.length);
            String cdsId = je.get("id").getAsString();
            problemInfo.name = je.get("name").getAsString();
            problemInfo.id = je.get("ordinal").getAsInt();
            problemInfo.color = Color.decode(je.get("rgb").getAsString());
            if (je.get("test_data_count") == null) {
                // TODO
                problemInfo.testCount = 100;
            } else {
                problemInfo.testCount = je.get("test_data_count").getAsInt();
            }
            problemInfo.letter = je.get("label").getAsString();
            problems[i] = problemInfo;
            contest.problemById.put(cdsId, problemInfo);
        }

        Arrays.sort(problems, (WFProblemInfo a, WFProblemInfo b) -> a.id - b.id);
        for (int i = 0; i < problems.length; i++) {
            problems[i].id = i;
            contest.problems.add(problems[i]);
        }
    }

    private void readTeamInfos(WFContestInfo contest) throws IOException {
        JsonArray jsonOrganizations = new Gson().fromJson(
                readJsonArray(url + "/organizations"), JsonArray.class);
        HashMap<String, WFTeamInfo> organizations = new HashMap<>();
        contest.teamInfos = new org.icpclive.events.WF.WFTeamInfo[jsonOrganizations.size()];
        for (int i = 0; i < jsonOrganizations.size(); i++) {
            JsonObject je = jsonOrganizations.get(i).getAsJsonObject();
            WFTeamInfo teamInfo = new WFTeamInfo(contest.problems.size());
            // TODO
            teamInfo.name = je.get("formal_name").getAsString();
            teamInfo.shortName = je.get("name").getAsString();
            organizations.put(je.get("id").getAsString(), teamInfo);
            contest.teamInfos[i] = teamInfo;
        }

        Arrays.sort(contest.teamInfos, (a, b) -> a.name.compareTo(b.name));
        for (int i = 0; i < contest.teamInfos.length; i++) {
            contest.teamInfos[i].id = i;
        }

        JsonArray jsonTeams = new Gson().fromJson(
                readJsonArray(url + "/teams"), JsonArray.class);
        contest.teamById = new HashMap<>();
        for (int i = 0; i < jsonTeams.size(); i++){
            JsonObject je = jsonTeams.get(i).getAsJsonObject();
            WFTeamInfo teamInfo = organizations.get(je.get("organization_id").getAsString());

            JsonArray groups = je.get("group_ids").getAsJsonArray();
            for (int j = 0; j < groups.size(); j++) {
                String groupId = groups.get(j).getAsString();
                String group = contest.groupById.get(groupId);
                teamInfo.groups.add(group);
            }

            teamInfo.cdsId = je.get("id").getAsString();
            contest.teamById.put(teamInfo.cdsId, teamInfo);
        }
        Arrays.sort(contest.teamInfos, (a, b) -> a.id - b.id);
    }

    public void readLanguagesInfos(WFContestInfo contestInfo) throws IOException {
        JsonArray jsonLanguages = new Gson().fromJson(
                readJsonArray(url + "/languages"), JsonArray.class);
        contestInfo.languages = new WFLanguageInfo[jsonLanguages.size()];
        contestInfo.languageById = new HashMap<>();
        for (int i = 0; i < jsonLanguages.size(); i++) {
            JsonObject je = jsonLanguages.get(i).getAsJsonObject();
            WFLanguageInfo languageInfo = new WFLanguageInfo();
            String cdsId = je.get("id").getAsString();
            languageInfo.name = je.get("name").getAsString();
            contestInfo.languages[i] = languageInfo;
            contestInfo.languageById.put(cdsId, languageInfo);
        }
    }

    private void initialize() throws IOException {
        WFContestInfo contestInfo = new WFContestInfo();
        readGroupsInfo(contestInfo);
        readLanguagesInfos(contestInfo);
        readProblemInfos(contestInfo);
        readTeamInfos(contestInfo);
        contestInfo.initializationFinish();
        log.info("Problems " + contestInfo.problems.size() + ", teamInfos " + contestInfo.teamInfos.length);

        contestInfo.recalcStandings();
        this.contestInfo = contestInfo;
    }

    public long parseTime(String time) {
        ZonedDateTime zdt = ZonedDateTime.parse(time + ":00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return zdt.toInstant().toEpochMilli();
//        LocalDateTime ldt = LocalDateTime.parse(time + ":00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
//        return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public long parseRelativeTime(String time) {
        String[] z = time.split("\\.");
        String[] t = z[0].split(":");
        int h = Integer.parseInt(t[0]);
        int m = Integer.parseInt(t[1]);
        int s = Integer.parseInt(t[2]);
        int ms = z.length == 1 ? 0 : Integer.parseInt(z[1]);
        return ((h * 60 + m) * 60 + s) * 1000 + ms;
    }

    public void readContest(JsonObject je) {
        JsonElement startTimeElement = je.get("start_time");
        if (!startTimeElement.isJsonNull()) {
            contestInfo.setStartTime(parseTime(startTimeElement.getAsString()));
            contestInfo.setStatus(ContestInfo.Status.RUNNING);
        } else {
            contestInfo.setStatus(ContestInfo.Status.BEFORE);
        }
        if (emulation) {
            contestInfo.setStartTime(System.currentTimeMillis());
        }
    }

    public void readState(JsonObject je) {
        String startTime = je.get("started").getAsString();
        contestInfo.setStartTime(parseTime(startTime));
        if (emulation) {
            contestInfo.setStartTime(System.currentTimeMillis());
        }
        if (je.get("ended").isJsonNull()) {
            contestInfo.setStatus(ContestInfo.Status.RUNNING);
        } else {
            contestInfo.setStatus(ContestInfo.Status.OVER);
        }
    }

    public void waitForEmulation(long time) {
        if (emulation) {
            try {
                long dt = (long) ((time - contestInfo.getCurrentTime()) / emulationSpeed);
                if (dt > 0) Thread.sleep(dt);
            } catch (InterruptedException e) {
                log.error("error", e);
            }
        }
    }

    public void readSubmission(JsonObject je, boolean update) {
        waitForEmulation(parseRelativeTime(je.get("contest_time").getAsString()));
        if (update) {
            return;
        }
        WFRunInfo run = new WFRunInfo();

        String cdsId = je.get("id").getAsString();

        WFLanguageInfo languageInfo = contestInfo.languageById.get(je.get("language_id").getAsString());
        run.languageId = languageInfo.id;

        WFProblemInfo problemInfo = contestInfo.problemById.get(je.get("problem_id").getAsString());
        run.problemId = problemInfo.id;

        WFTeamInfo teamInfo = (WFTeamInfo) contestInfo.teamById.get(je.get("team_id").getAsString());
        run.teamId = teamInfo.id;
        run.team = teamInfo;

        run.time = parseRelativeTime(je.get("contest_time").getAsString());

        run.setLastUpdateTime(run.time);

        contestInfo.addRun(run);

        contestInfo.runBySubmissionId.put(cdsId, run);
    }

    public void readJudgement(JsonObject je) {
        String cdsId = je.get("id").getAsString();

        WFRunInfo runInfo = contestInfo.runBySubmissionId.get(je.get("submission_id").getAsString());

        contestInfo.runByJudgementId.put(cdsId, runInfo);

        JsonElement verdictElement = je.get("judgement_type_id");
        if (verdictElement.isJsonNull()) {
            waitForEmulation(parseRelativeTime(je.get("start_contest_time").getAsString()));
            return;
        }
        String verdict = verdictElement.getAsString();

        long time = parseRelativeTime(je.get("end_contest_time").getAsString());
        waitForEmulation(time);

        if (runInfo.time <= ContestInfo.FREEZE_TIME) {
            runInfo.setTeamInfoBefore(
                    contestInfo.getParticipant(runInfo.teamId).getSmallTeamInfo());

            runInfo.result = verdict;

            runInfo.judged = true;

            long start = System.currentTimeMillis();
            contestInfo.recalcStandings();
            log.info("Standing are recalculated in " + (System.currentTimeMillis() - start) + " ms");
        } else {
            runInfo.judged = false;
        }

        runInfo.setLastUpdateTime(time);
    }

    public void readRun(JsonObject je, boolean update) {
        WFRunInfo runInfo = contestInfo.runByJudgementId.get(je.get("judgement_id").getAsString());

        long time = parseRelativeTime(je.get("contest_time").getAsString());

        waitForEmulation(time);

        if (runInfo == null || runInfo.time > ContestInfo.FREEZE_TIME || update) {
            return;
        }

        WFTestCaseInfo testCaseInfo = new WFTestCaseInfo();
        testCaseInfo.id = je.get("ordinal").getAsInt();
        testCaseInfo.result = je.get("judgement_type_id").getAsString();
        testCaseInfo.time = time;
        testCaseInfo.timestamp = parseTime(je.get("time").getAsString());
        testCaseInfo.runId = runInfo.id;
        testCaseInfo.total = contestInfo.getProblemById(runInfo.problemId).testCount;

//        System.err.println(runInfo);
        contestInfo.addTest(testCaseInfo);
    }

    public WFAnalystMessage readAnalystMessage(JsonObject je) {
        WFAnalystMessage message = new WFAnalystMessage();
        message.setPriority(je.get("priority").getAsInt());
        message.setMessage(je.get("text").getAsString());
        message.setTime(parseRelativeTime(je.get("contest_time").getAsString()) + contestInfo.getStartTime());
        return message;
    }

    public void run() {
        String lastSavedEvent = null;
        while (true) {
            try {
                String url = this.url + "/event-feed"
                        + (lastSavedEvent == null ? "" : "?since_id=" + lastSavedEvent);
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(Preparation.openAuthorizedStream(url, login, password),
                                "utf-8"));

                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }

                    JsonObject je = new Gson().fromJson(line, JsonObject.class);
                    if (je == null) {
                        log.info("Non-json line");
                        System.err.println("Non-json line: " + Arrays.toString(line.toCharArray()));
                        continue;
                    }
                    lastSavedEvent = je.get("id") == null ? lastSavedEvent : je.get("id").getAsString();
                    boolean update = !je.get("op").getAsString().equals("create");
                    String type = je.get("type").getAsString();
                    JsonObject json = je.get("data").getAsJsonObject();

                    switch (type) {
                        case "contests":
                            readContest(json);
                            break;
                        case "state":
                            readState(json);
                            break;
                        case "submissions":
                            readSubmission(json, update);
                            break;
                        case "judgements":
                            readJudgement(json);
                            break;
                        case "runs":
                            readRun(json, update);
                        default:
                    }
                }
            } catch(IOException e){
                log.error("error", e);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    log.error("error", e1);
                }
                log.info("Restart event feed");
                System.err.println("Restarting feed");
            }
        }
    }

        // public static ArrayBlockingQueue<RunInfo> getAllRuns() {

    static Map<String, String> shortNames = new HashMap<>();

    static {
        try {
            Properties properties = new Properties();
            properties.load(WFEventsLoader.class.getClassLoader().getResourceAsStream("events.properties"));

            File override = new File(properties.getProperty("teamInfos.shortnames.override", "override.txt"));
            if (override.exists()) {
                BufferedReader in = new BufferedReader(new FileReader("override.txt"));
                String line;
                while ((line = in.readLine()) != null) {
                    String[] ss = line.split("\t");
                    shortNames.put(ss[0], ss[1]);
                }
            }
        } catch (Exception e) {
            log.error("error", e);
        }
    }

    static String shortName(String name) {
        assert shortNames.get(name) == null;
        if (shortNames.containsKey(name)) {
            return shortNames.get(name);
        } else if (name.length() > 15) {
            return name.substring(0, 12) + "...";
        } else {
            return name;
        }
    }
}
