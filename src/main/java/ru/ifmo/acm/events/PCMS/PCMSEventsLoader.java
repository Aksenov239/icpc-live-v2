package ru.ifmo.acm.events.PCMS;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import ru.ifmo.acm.events.EventsLoader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class PCMSEventsLoader extends EventsLoader {

    private static PCMSEventsLoader instance;

    public static EventsLoader getInstance() {
        if (instance == null) {
            instance = new PCMSEventsLoader();
            properties = new Properties();
            try {
                properties.load(PCMSEventsLoader.class.getClassLoader().getResourceAsStream("events.properties"));
                PCMSContestInfo initial = parseInitialContestInfo();
                contestInfo.set(initial);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private static PCMSContestInfo parseInitialContestInfo() throws IOException {
        int problemsNumber = Integer.parseInt(properties.getProperty("problemsNumber"));
        PCMSContestInfo initial = new PCMSContestInfo(problemsNumber);
        String fn = properties.getProperty("participants");
        String xml = new String(Files.readAllBytes(Paths.get(fn)));
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element participants = doc.child(0);
        int id = 0;
        for (Element participant : participants.children()) {
            id++;
            String participantName = participant.attr("name");
            String shortName = participant.attr("id");

            PCMSTeamInfo team = new PCMSTeamInfo(id, participantName, shortName, initial.getProblemsNumber());
            initial.addTeamStandings(team);
        }
        return initial;
    }

    private void updateStatements() throws IOException {
        String url = properties.getProperty("url");
//        String html = new BufferedReader(new FileReader(url))
//                .lines()
//                .collect(Collectors.joining());
//        Document doc = Jsoup.parse(html, url);
//        parseAndUpdateStandings(doc.body());
        String xml = new BufferedReader(new FileReader(url))
                .lines()
                .collect(Collectors.joining());
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        parseAndUpdateStandings(doc);
    }

    @Override
    public void run() {
        //System.err.println(check.getName() + " " + check.getShortName());
        while (true) {
            try {
                while (true) {
                    updateStatements();
                    sleep(1000);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();

            }
        }
    }

    private void parseAndUpdateStandings(Element element) {
        if ("contest".equals(element.tagName())) {
            PCMSContestInfo updatedContestInfo = parseContestInfo(element);
            contestInfo.set(updatedContestInfo);
        } else {
            element.children().forEach(this::parseAndUpdateStandings);
        }
    }

    private PCMSContestInfo parseContestInfo(Element element) {
        int problemsNumber = Integer.parseInt(properties.getProperty("problemsNumber"));
        PCMSContestInfo updatedContestInfo = new PCMSContestInfo(problemsNumber);
        updatedContestInfo.setCurrentTime(Long.parseLong(element.attr("time")));
        updatedContestInfo.frozen = "yes".equals(element.attr("frozen"));

        element.children().forEach(session -> {
            if ("session".equals(session.tagName())) {
                PCMSTeamInfo teamInfo = parseTeamInfo(session);
                updatedContestInfo.addTeamStandings(teamInfo);
            }
        });

        updatedContestInfo.fillTimeFirstSolved();
        updatedContestInfo.calculateRanks();

        return updatedContestInfo;
    }

    private PCMSTeamInfo parseTeamInfo(Element element) {
        String name = element.attr("party");
        PCMSTeamInfo oldTeamInfo = new PCMSTeamInfo(contestInfo.get().getParticipant(name));
        PCMSTeamInfo teamInfo = new PCMSTeamInfo(oldTeamInfo.id, name, oldTeamInfo.getShortName(), contestInfo.get().getProblemsNumber());

        teamInfo.solved = Integer.parseInt(element.attr("solved"));
        teamInfo.penalty = Integer.parseInt(element.attr("penalty"));

        for (int i = 0; i < element.children().size(); i++) {
            ArrayList<PCMSRunInfo> problemRuns = parseProblemRuns(element.child(i), i);
            teamInfo.addRuns(problemRuns, i);
        }

        return teamInfo;
    }

    private ArrayList<PCMSRunInfo> parseProblemRuns(Element element, int problemId) {
        ArrayList<PCMSRunInfo> runs = new ArrayList<>();
        element.children().forEach(run -> {
            PCMSRunInfo runInfo = parseRunInfo(run, problemId);
            runs.add(runInfo);
        });

        return runs;
    }

    private PCMSRunInfo parseRunInfo(Element element, int problemId) {
        long time = Long.parseLong(element.attr("time"));
        boolean isFrozen = time >= contestInfo.get().getTotalTime();
        String result = isFrozen ? "Frozen" : ("yes".equals(element.attr("accepted")) ? "AC" : "REJ");

        return new PCMSRunInfo(!isFrozen, result, problemId, time);
    }

    public PCMSContestInfo getContestData() {
        return contestInfo.get();
    }

    static AtomicReference<PCMSContestInfo> contestInfo = new AtomicReference<>();
    private static Properties properties;
}
