package ru.ifmo.acm.events.WF;

import ru.ifmo.acm.backend.Preparation;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.EventsLoader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.*;
import java.io.*;

/**
 * Created by aksenov on 16.04.2015.
 */
public class WFEventsLoader extends EventsLoader {

    private static WFContestInfo contestInfo;

    private String url;
    private String teamsInfoURL;
    private String problemsInfoURL;
    private String login;
    private String password;

    public WFEventsLoader() {
        try {
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("events.properties"));

            login = properties.getProperty("login");
            password = properties.getProperty("password");

            Preparation.prepareNetwork(login, password);

            url = properties.getProperty("url");

            problemsInfoURL = properties.getProperty("problems.url");
            teamsInfoURL = properties.getProperty("teams.url");

            initialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ContestInfo getContestData() {
        return contestInfo;
    }

    private int problemsInfoRead() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(Preparation.openAuthorizedStream(problemsInfoURL, login, password)));
        int problems = 0;
        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains("letter")) {
                problems++;
            }
        }
        return problems;
    }

    private WFTeamInfo[] teamsInfoRead(int problemsNumber) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(Preparation.openAuthorizedStream(teamsInfoURL, login, password)));
        ArrayList<WFTeamInfo> infos = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            String[] z = line.split("\\t");
            if (z.length < 6) {
                continue;
            }
            WFTeamInfo team = new WFTeamInfo(problemsNumber);
            team.id = Integer.parseInt(z[0]) - 1;
            team.name = z[4];
            team.shortName = z[5];
            infos.add(team);
        }
        return infos.toArray(new WFTeamInfo[0]);
    }

    private void initialize() throws IOException {
        int problemsNumber = problemsInfoRead();
        WFTeamInfo[] teams = teamsInfoRead(problemsNumber);
        System.err.println(problemsNumber + " " + teams.length);
        contestInfo = new WFContestInfo(problemsNumber, teams.length);
        for (WFTeamInfo team : teams) {
            contestInfo.addTeam(team);
        }
        contestInfo.recalcStandings();
        //();
    }

    public WFTestCaseInfo readTest(XMLEventReader xmlEventReader) throws XMLStreamException {
        WFTestCaseInfo test = new WFTestCaseInfo();
        System.out.println("Reading testcase");
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
        run.setLastUpdateTimestamp(System.currentTimeMillis());
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
                        run.team = Integer.parseInt(xmlEvent.asCharacters().getData()) - 1;
                        break;
                    case "time":
                        double time = Double.parseDouble(xmlEvent.asCharacters().getData());
                        if (run.time == 0) {
                            run.time = time;
                        }
                        break;
                    case "timestamp":
                        run.timestamp = Double.parseDouble(xmlEvent.asCharacters().getData());
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
                        // team.name = xmlEvent.asCharacters().getData();
                        // team.name =
                        // xmlEvent.toString();//asCharacters().getData();
                        team.name = xmlEventReader.getElementText();
                        team.shortName = shortName(team.name);
                        break;
                    case "region":
                        team.region = xmlEventReader.getElementText();
                        break;
                }
            }
            if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                if (endElement.getName().getLocalPart().equals("team")) {
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
        while (true) {
            try {
                initialize();

                XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

                XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(
                        Preparation.openAuthorizedStream(url, login, password),
                        "windows-1251"
                );

                // XMLInputFactory xmlInputFactory =
                // XMLInputFactory.newInstance();
                // XMLEventReader xmlEventReader =
                // xmlInputFactory.createXMLEventReader(
                // new FileInputStream(new File(properties.getProperty("url"))),
                // "windows-1251");

                while (xmlEventReader.hasNext()) {
                    XMLEvent xmlEvent = null;
                    try {
                        xmlEvent = xmlEventReader.nextEvent();
                    } catch (XMLStreamException e) {
                        e.printStackTrace();
                        break;
                    }
                    if (xmlEvent.isStartElement()) {
                        StartElement startElement = xmlEvent.asStartElement();
                        switch (startElement.getName().getLocalPart()) {
                            case "run":
                                WFRunInfo run = readRun(xmlEventReader);
                                System.err.println("new run: " + (int) (run.getTime() / 60) + " " + run.getTeam() + " "
                                        + (char) ('A' + run.getProblemNumber()) + " " + run.getResult());
                                if (run.getTime() <= 4 * 60 * 60 * 1000 || run.getResult().length() == 0) {
                                    contestInfo.addRun(run);
                                    if (run.getTime() > contestInfo.getCurrentTime() / 1000 - 600) {
                                        contestInfo.recalcStandings();
                                    }
                                }
                                break;
                            case "testcase":
                                WFTestCaseInfo test = readTest(xmlEventReader);
                                contestInfo.addTest(test);
                                break;
                            case "language":
                                readLanguage(xmlEventReader);
                                break;
                            /*case "team":
                                WFTeamInfo team = readTeam(xmlEventReader);
                                if (team != null) {
                                    contestInfo.addTeam(team);
                                    contestInfo.teamNumber++;
                                }
                                contestInfo.recalcStandings();
                                break;
                            case "problem":
                                contestInfo.problemNumber++;
                                break;*/
                            case "starttime":
                                contestInfo.setStartTime(
                                        (long) (Double.parseDouble(xmlEventReader.getElementText().replace(",", "."))
                                                * 1000));
                                break;
                        }
                    }
                }
                contestInfo.recalcStandings();
                break;
            } catch (IOException | XMLStreamException e) {
                e.printStackTrace();
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
            e.printStackTrace();
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
