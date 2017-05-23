package ru.ifmo.acm.events.WF;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.Marker;
import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.EventsLoader;
import ru.ifmo.acm.events.ProblemInfo;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Created by aksenov on 16.04.2015.
 */
public class WFEventsLoader extends EventsLoader {
    private static final Logger log = LogManager.getLogger(WFEventsLoader.class);

    private static WFContestInfo contestInfo;

    private String url;
    private String teamsInfoURL;
    private String problemsInfoURL;
    private String login;
    private String password;

    private HashMap<String, String> regionsMapping;

    private boolean emulation;

    public WFEventsLoader() {
        try {
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("events.properties"));

            login = properties.getProperty("login");
            password = properties.getProperty("password");

            Preparation.prepareNetwork(login, password);

            url = properties.getProperty("url");

            if (!(url.startsWith("http") || url.startsWith("https"))) {
                emulation = true;
            } else {
                EMULATION_SPEED = 1;
            }

            problemsInfoURL = properties.getProperty("problems.url");
            teamsInfoURL = properties.getProperty("teams.url");

            String mapping = properties.getProperty("regions.mapping");

            JsonObject jsonObject = new Gson().fromJson(mapping, JsonObject.class);
            regionsMapping = new HashMap<>();
            for (Map.Entry<String, JsonElement> elementEntry : jsonObject.entrySet()) {
                regionsMapping.put(elementEntry.getKey(), elementEntry.getValue().getAsString());
            }

            initialize();
        } catch (IOException e) {
            log.error("error", e);
        }
    }

    public ContestInfo getContestData() {
        return contestInfo;
    }

    private List<ProblemInfo> problemsInfoRead() throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(Preparation.openAuthorizedStream(problemsInfoURL, login, password)));
        String line;
        List<ProblemInfo> problems = new ArrayList<>();
        ProblemInfo problem = null;
        while ((line = br.readLine()) != null) {
            line = line.trim();

            problem = new ProblemInfo();
            problems.add(problem);

            JsonObject jsonObject = new Gson().fromJson(line, JsonObject.class);
            JsonObject problemObject = jsonObject.get("problem").getAsJsonObject();
            problem.letter = problemObject.get("label").getAsString();
            problem.name = problemObject.get("name") == null ? "temporary" :
                problemObject.get("name").getAsString();
            problem.color = Color.decode(problemObject.get("rgb").getAsString());
        }
        return problems;
    }

    private WFTeamInfo[] teamsInfoRead(int problemsNumber) throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(Preparation.openAuthorizedStream(teamsInfoURL, login, password), "utf8"));
        ArrayList<WFTeamInfo> infos = new ArrayList<WFTeamInfo>();
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();

            JsonObject jsonObject = new Gson().fromJson(line, JsonObject.class);
            JsonObject teamObject = jsonObject.get("team").getAsJsonObject();

            WFTeamInfo team = new WFTeamInfo(problemsNumber);
            team.id = teamObject.get("id").getAsInt() - 1;
            team.name = teamObject.get("affiliation").getAsString();
            team.shortName = teamObject.get("affiliation-short-name").getAsString();
            team.region = regionsMapping.get(teamObject.get("group-id").getAsString());
            WFContestInfo.REGIONS.add(team.region);
            team.hashTag = teamObject.get("hashtag") == null ? "#None" :
                    teamObject.get("hashtag").getAsString();
            team.hashTag = team.hashTag.toLowerCase();
            infos.add(team);
        }

        return infos.toArray(new WFTeamInfo[0]);
    }

    private void initialize() throws IOException {
        List<ProblemInfo> problems = problemsInfoRead();
        int problemsNumber = problems.size();
        WFTeamInfo[] teams = teamsInfoRead(problemsNumber);
        log.info("Problems " + problemsNumber + ", teams " + teams.length);
        contestInfo = new WFContestInfo(problemsNumber, teams.length);
        contestInfo.problems = problems;
        for (WFTeamInfo team : teams) {
            contestInfo.addTeam(team);
        }
        contestInfo.recalcStandings();
        //();
    }

    public WFTestCaseInfo readTest(XMLEventReader xmlEventReader) throws XMLStreamException {
        WFTestCaseInfo test = new WFTestCaseInfo();
//        System.out.println("Reading testcase");
        while (true) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                StartElement startElement = xmlEvent.asStartElement();
                String name = startElement.getName().getLocalPart();
                xmlEvent = xmlEventReader.nextEvent();
                switch (name) {
                    case "i":
                        test.id = Integer.parseInt(xmlEvent.asCharacters().getData());
                        break;
                    case "judged":
                        test.judged = Boolean.parseBoolean(xmlEvent.asCharacters().getData());
                        break;
                    case "judgement_id":
                        test.judgementId = Integer.parseInt(xmlEvent.asCharacters().getData());
                        break;
                    case "n":
                        test.total = Integer.parseInt(xmlEvent.asCharacters().getData());
                        break;
                    case "result":
                        test.result = xmlEvent.asCharacters().getData();
                        break;
                    case "run-id":
                        test.run = Integer.parseInt(xmlEvent.asCharacters().getData());
                        break;
                    case "solved":
                        test.solved = Boolean.parseBoolean(xmlEvent.asCharacters().getData());
                        break;
                    case "time":
                        test.time = (long) (Double.parseDouble(xmlEvent.asCharacters().getData()) * 1000);
                        break;
                    case "timestamp":
                        test.timestamp = Double.parseDouble(xmlEvent.asCharacters().getData());
                        break;
                }
            }
            if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                if (endElement.getName().getLocalPart().equals("testcase")) {
                    break;
                }
            }
        }
        return test;
    }

    public WFRunInfo readRun(XMLEventReader xmlEventReader) throws XMLStreamException {
        WFRunInfo run = new WFRunInfo();
        run.setLastUpdateTimestamp(0);
        while (true) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                StartElement startElement = xmlEvent.asStartElement();
                String name = startElement.getName().getLocalPart();
                xmlEvent = xmlEventReader.nextEvent();
                switch (name) {
                    case "id":
                        run.id = Integer.parseInt(xmlEvent.asCharacters().getData());
                        if (contestInfo.runExists(run.id)) {
                            WFRunInfo currentRun = run;
                            run = contestInfo.getRun(run.id);
                            if (currentRun.getResult().length() > 0) {
                                run.result = currentRun.getResult();
                            }
                        }
                        break;
                    case "judged":
                        run.judged = Boolean.parseBoolean(xmlEvent.asCharacters().getData());
                        break;
                    case "language":
                        run.language = xmlEvent.asCharacters().getData();
                        break;
                    case "problem":
                        run.problem = Integer.parseInt(xmlEvent.asCharacters().getData()) - 1;
                        break;
                    case "result":
                        run.result = xmlEvent.asCharacters().getData();
                        break;
                    case "team":
                        run.teamId = Integer.parseInt(xmlEvent.asCharacters().getData()) - 1;
                        run.team = contestInfo.getParticipant(run.teamId);
                        break;
                    case "time":
                        double time = Double.parseDouble(xmlEvent.asCharacters().getData());
                        if (run.time == 0) {
                            run.time = time;
                        }
                        break;
                    case "timestamp":
//                        run.timestamp = Double.parseDouble(xmlEvent.asCharacters().getData());
                        run.setLastUpdateTimestamp((long) Math.max(run.getLastUpdateTimestamp(), run.timestamp * 1000));
                        run.timestamp = System.currentTimeMillis() / 1000;
                        // Double.parseDouble(xmlEvent.asCharacters().getData());
                        break;
                }
            }
            if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                if (endElement.getName().getLocalPart().equals("run")) {
                    break;
                }
            }
        }
        return run;
    }

    public WFAnalystMessage readMessage(XMLEventReader xmlEventReader) throws XMLStreamException {
        WFAnalystMessage message = new WFAnalystMessage();
        while (true) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                StartElement startElement = xmlEvent.asStartElement();
                String name = startElement.getName().getLocalPart();
                xmlEvent = xmlEventReader.nextEvent();
                switch (name) {
                    case "id":
                        message.setId(Integer.parseInt(xmlEvent.asCharacters().getData()));
                        break;
                    case "problem":
                        message.setProblem(Integer.parseInt(xmlEvent.asCharacters().getData()) - 1);
                        break;
                    case "team":
                        message.setTeam(Integer.parseInt(xmlEvent.asCharacters().getData()) - 1);
                        break;
                    case "time":
                        int time = Integer.parseInt(xmlEvent.asCharacters().getData());
                        message.setTime(time);
                        break;
                    case "priority":
                        message.setPriority(Integer.parseInt(xmlEvent.asCharacters().getData()));
                        break;
                    case "run_id":
                        break;
                    case "category":
                        message.setCategory(WFAnalystMessage.WFAnalystMessageCategory.getCategory(xmlEvent.asCharacters().getData()));
                        break;
                    case "message":
                        message.setMessage(xmlEvent.asCharacters().getData());
                        break;
                    case "submission":
                        //yes, really
                        message.setRunId(Integer.parseInt(xmlEvent.asCharacters().getData()));
                        break;
                    default:
                        log.warn("Unknown tag: " + name);
                }
            }
            if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                if (endElement.getName().getLocalPart().equals("analystmsg")) {
                    break;
                }
            }
        }
        return message;
    }

    public WFTeamInfo readTeam(XMLEventReader xmlEventReader) throws XMLStreamException {
        WFTeamInfo team = new WFTeamInfo(contestInfo.getProblemsNumber());
        while (true) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                StartElement startElement = xmlEvent.asStartElement();
                String name = startElement.getName().getLocalPart();
                switch (name) {
                    case "id":
                        team.id = Integer.parseInt(xmlEventReader.getElementText()) - 1;
                        break;
                    case "university":
                        // teamId.name = xmlEvent.asCharacters().getData();
                        // teamId.name =
                        // xmlEvent.toString();//asCharacters().getData();
                        team.name = xmlEventReader.getElementText();
                        team.shortName = shortName(team.name);
                        break;
                    case "region":
                        team.region = xmlEventReader.getElementText();
                        if (team.region != null) {
                            WFContestInfo.REGIONS.add(team.region);
                        }
                        break;
                }
            }
            if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                if (endElement.getName().getLocalPart().equals("teamId")) {
                    break;
                }
            }
        }
        if (team.id == -1)
            return null;
        return team;
    }

    public void readLanguage(XMLEventReader xmlEventReader) throws XMLStreamException {
        int id = 0;
        String language = "";
        while (true) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                StartElement startElement = xmlEvent.asStartElement();
                String name = startElement.getName().getLocalPart();
                xmlEvent = xmlEventReader.nextEvent();
                switch (name) {
                    case "id":
                        id = Integer.parseInt(xmlEvent.asCharacters().getData());
                        break;
                    case "name":
                        language = xmlEvent.asCharacters().getData();
                        break;
                }
            }
            if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                if (endElement.getName().getLocalPart().equals("language")) {
                    break;
                }
            }
        }
        contestInfo.languages[id] = language;
    }

    public void run() {
        XMLEventReader xmlEventReader;
        while (true) {
            try {
                XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

                try {
                    xmlEventReader = xmlInputFactory.createXMLEventReader(
                            Preparation.openAuthorizedStream(url, login, password),
                            "windows-1251"
                    );
                } catch (XMLStreamException e) {
                    log.error("error", e);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        log.error("error", e1);
                    }
                    continue;
                }

                initialize();

                // XMLInputFactory xmlInputFactory =
                // XMLInputFactory.newInstance();
                // XMLEventReader xmlEventReader =
                // xmlInputFactory.createXMLEventReader(
                // new FileInputStream(new File(properties.getProperty("url"))),
                // "windows-1251");

                //emulation = false;

                int total = 0;
                while (xmlEventReader.hasNext()) {
                    XMLEvent xmlEvent =
                            xmlEventReader.nextEvent();
                    if (xmlEvent.isStartElement()) {
                        StartElement startElement = xmlEvent.asStartElement();
                        switch (startElement.getName().getLocalPart()) {
                            case "run":

                                WFRunInfo run = readRun(xmlEventReader);
                                if (emulation) {
                                    try {
                                        long dt = (long) ((run.getTime() - contestInfo.getCurrentTime()) / EMULATION_SPEED);
                                        if (dt > 0)
                                            Thread.sleep(dt);
                                        total++;
                                    } catch (InterruptedException e) {
                                        log.error("error", e);
                                    }
                                }
                                log.info("New run: " + run);
                                if (run.getTime() <= ContestInfo.FREEZE_TIME || run.getResult().length() == 0) {
                                    if (contestInfo.runExists(run.getId())) {
                                        run.setTeamInfoBefore(contestInfo.getParticipant(run.getTeamId()).getSmallTeamInfo());
                                    }
                                    contestInfo.addRun(run);
//                                    if (run.getTime() > contestInfo.getCurrentTime() / 1000 - 600) {
                                    long start = System.currentTimeMillis();
                                    contestInfo.recalcStandings();
                                    log.info("Standings calculated in " + (System.currentTimeMillis() - start) + " ms");
//                                    }
                                } else {
                                    run.result = "";
                                    run.judged = false;
                                }
                                break;
                            case "testcase":
                                WFTestCaseInfo test = readTest(xmlEventReader);
                                if (emulation) {
                                    try {
                                        long dt = (long) ((test.time - contestInfo.getCurrentTime()) / EMULATION_SPEED);
                                        if (dt > 0) Thread.sleep(dt);
                                    } catch (InterruptedException e) {
                                        log.error("error", e);
                                    }
                                }
                                if (test.time <= ContestInfo.FREEZE_TIME) { // Update the tests results only if not frozen
                                    contestInfo.addTest(test);
                                }
                                break;
                            case "language":
                                readLanguage(xmlEventReader);
                                break;
                            /*case "teamId":
                                WFTeamInfo teamId = readTeam(xmlEventReader);
                                if (teamId != null) {
                                    contestInfo.addTeam(teamId);
                                    contestInfo.teamNumber++;
                                }
                                contestInfo.recalcStandings();
                                break;
                            case "problem":
                                contestInfo.problemNumber++;
                                break;*/
                            case "starttime":
                                String starttime = xmlEventReader.getElementText();
                                if ("undefined".equals(starttime)) {
                                    contestInfo.setStatus(ContestInfo.Status.PAUSED);
//                                    Now we wait while the starttime is not set
//                                    throw new Exception("The start time is undefined");
                                    contestInfo.setStartTime(System.currentTimeMillis());
                                } else {
                                    contestInfo.setStatus(ContestInfo.Status.RUNNING);
                                    contestInfo.setStartTime(
                                            (long) (Double.parseDouble(starttime.replace(",", "."))
                                                    * 1000));
                                }
                                if (emulation) {
                                    contestInfo.setStartTime(System.currentTimeMillis());
                                }
                                break;
                            case "length": {
                                String s = xmlEventReader.getElementText();
                                String[] time = s.split(":");
                                int hh = Integer.parseInt(time[0]);
                                int mm = Integer.parseInt(time[1]);
                                int ss = Integer.parseInt(time[2]);
                                ContestInfo.CONTEST_LENGTH = ((hh * 60 + mm) * 60 + ss) * 1000;
                                break;
                            }
                            case "scoreboard-freeze-length": {
                                String s = xmlEventReader.getElementText();
                                String[] time = s.split(":");
                                int hh = Integer.parseInt(time[0]);
                                int mm = Integer.parseInt(time[1]);
                                int ss = Integer.parseInt(time[2]);
                                ContestInfo.FREEZE_TIME = ContestInfo.CONTEST_LENGTH - ((hh * 60 + mm) * 60 + ss) * 1000;
                                break;
                            }
                            case "analystmsg": {
                                WFAnalystMessage message = readMessage(xmlEventReader);
                                if (emulation) {
                                    try {
                                        long dt = (long) ((message.getTime() - contestInfo.getCurrentTime()) / EMULATION_SPEED);
                                        if (dt > 0)
                                            Thread.sleep(dt);
                                        total++;
                                    } catch (InterruptedException e) {
                                        log.error("error", e);
                                    }
                                }
                                log.info("New message: " + message);
                                contestInfo.addMessage(message);
                                break;
                            }
                            default: {
                                log.warn("Unknow tag: " + startElement.getName().getLocalPart());
                            }
                        }
                    }
                }
                contestInfo.recalcStandings();
                break;
            } catch (Exception e) {
                log.error("error", e);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    log.error("error", e1);
                }
                log.info("Restart event read");
                System.err.println("Restart event read");
            }
        }
    }

    // public static ArrayBlockingQueue<RunInfo> getAllRuns() {

    static Map<String, String> shortNames = new HashMap<>();

    static {
        try {
            Properties properties = new Properties();
            properties.load(WFEventsLoader.class.getClassLoader().getResourceAsStream("events.properties"));

            File override = new File(properties.getProperty("teams.shortnames.override", "override.txt"));
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
