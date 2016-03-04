package ru.ifmo.acm.events.WF;

import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.EventsLoader;
import ru.ifmo.acm.events.RunInfo;
import ru.ifmo.acm.events.TeamInfo;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by aksenov on 16.04.2015.
 */
public class WFEventsLoader extends EventsLoader {

    private String url;

    private static WFContestInfo contestInfo;
    private static ContestInfo returnContestInfo;

    public WFEventsLoader(String url) {
        this.url = url;
    }

    public ContestInfo getContestData() {
        return returnContestInfo;
    }

    public WFRunInfo readRun(XMLEventReader xmlEventReader) throws XMLStreamException {
        WFRunInfo run = new WFRunInfo();
        while (true) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                StartElement startElement = xmlEvent.asStartElement();
                String name = startElement.getName().getLocalPart();
                xmlEvent = xmlEventReader.nextEvent();
                switch (name) {
                    case "id":
                        run.id = Integer.parseInt(xmlEvent.asCharacters().getData());
                        break;
                    case "judged":
                        run.judged = Boolean.parseBoolean(xmlEvent.asCharacters().getData());
                        break;
                    case "language":
                        run.language = xmlEvent.asCharacters().getData();
                        break;
                    case "problem":
                        run.problem = Integer.parseInt(xmlEvent.asCharacters().getData());
                        break;
                    case "result":
                        run.result = xmlEvent.asCharacters().getData();
                        break;
                    case "team":
                        run.team = Integer.parseInt(xmlEvent.asCharacters().getData());
                        break;
                    case "time":
                        run.time = Double.parseDouble(xmlEvent.asCharacters().getData());
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
        WFTeamInfo team = new WFTeamInfo(contestInfo.problemNumber);
        while (true) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                StartElement startElement = xmlEvent.asStartElement();
                String name = startElement.getName().getLocalPart();
                switch (name) {
                    case "id":
                        team.id = Integer.parseInt(xmlEventReader.getElementText());
                        break;
                    case "university":
//                        team.name = xmlEvent.asCharacters().getData();
//                        team.name = xmlEvent.toString();//asCharacters().getData();
                        team.name = xmlEventReader.getElementText();//"Team " + team.id;
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
        if (team.id == 0) return null;
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
                URL url = new URL(this.url);

                contestInfo = new WFContestInfo();
                if (returnContestInfo == null) {
                    returnContestInfo = contestInfo;
                }
                contestInfo.runs = new ArrayBlockingQueue<>(1000000);

                Properties properties = new Properties();
                properties.load(getClass().getClassLoader().getResourceAsStream("generator.properties"));

                String login = properties.getProperty("login");
                String password = properties.getProperty("password");

                XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
                CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((login + ":" + password).getBytes()));
                con.connect();
                System.err.println(con.getHeaderFields());
                XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(con.getInputStream(), "windows-1251");

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
                                RunInfo run = readRun(xmlEventReader);
                                System.err.println("new run: " + (int) (run.getTime() / 60) + " " + run.getTeam() + " " + (char) ('A' + run.getProblemNumber() - 1) + " " + run.getResult());
                                if (run.getTime() <= 240 * 60 || run.getResult().length() == 0) {
                                    contestInfo.runs.add(run);
                                    contestInfo.teamInfos[run.getTeam()].addRun(run, run.getProblemNumber() - 1);
                                    if (run.getTime() > (System.currentTimeMillis() - contestInfo.startTime) / 1000 - 600) {
                                        if (returnContestInfo != contestInfo) {
                                            returnContestInfo = contestInfo;
                                        }
                                        contestInfo.recalcStandings();
                                    }
                                }
                                break;
                            case "language":
                                readLanguage(xmlEventReader);
                                break;
                            case "team":
                                WFTeamInfo team = readTeam(xmlEventReader);
                                if (team != null) {
                                    contestInfo.teamInfos[team.getId()] = team;
                                    contestInfo.teamNumber++;
                                }
                                contestInfo.recalcStandings();
                                break;
                            case "problem":
                                contestInfo.problemNumber++;
                                break;
                            case "starttime":
                                contestInfo.startTime = (long) (Double.parseDouble(xmlEventReader.getElementText().replace(",", ".")) * 1000);
                                break;
                        }
                    }
                }
                if (returnContestInfo != contestInfo) {
                    contestInfo.recalcStandings();
                    returnContestInfo = contestInfo;
                }
                break;
            } catch (IOException | XMLStreamException e) {
                e.printStackTrace();
            }
        }
    }

//    public static ArrayBlockingQueue<RunInfo> getAllRuns() {

    static Map<String, String> shortNames = new HashMap<>();

    static {
        try {
            Properties properties = new Properties();
            properties.load(new FileReader("resources/generator.properties"));
            URL url = new URL(properties.getProperty("teams"));

            String login = properties.getProperty("login");
            String password = properties.getProperty("password");


            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((login + ":" + password).getBytes()));
            con.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            in.readLine();
            String line;
            while ((line = in.readLine()) != null) {
                String[] ss = line.split("\t");
                shortNames.put(ss[4], ss[5]);
            }

            in = new BufferedReader(new FileReader("override.txt"));
            while ((line = in.readLine()) != null) {
                String[] ss = line.split("\t");
                shortNames.put(ss[0], ss[1]);
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
