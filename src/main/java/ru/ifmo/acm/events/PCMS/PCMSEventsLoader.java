package ru.ifmo.acm.events.PCMS;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import ru.ifmo.acm.events.EventsLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class PCMSEventsLoader extends EventsLoader {
    private static final Logger log = LogManager.getLogger(PCMSEventsLoader.class);

    public PCMSEventsLoader() throws IOException {
        properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("events.properties"));

        int problemsNumber = Integer.parseInt(properties.getProperty("problemsNumber"));
        PCMSContestInfo initial = new PCMSContestInfo(problemsNumber);
        String fn = properties.getProperty("participants");
        String xml = new String(Files.readAllBytes(Paths.get(fn)), StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element participants = doc.child(0);
        int id = 0;
        for (Element participant : participants.children()) {
            id++;
            String participantName = participant.attr("name");
            String alias = participant.attr("id");
            String shortName = participant.attr("shortname");

            PCMSTeamInfo team = new PCMSTeamInfo(id, alias, participantName, shortName, initial.getProblemsNumber());
            initial.addTeamStandings(team);
        }
        contestInfo.set(initial);
    }

    private void updateStatements() throws IOException {
        try {
            String url = properties.getProperty("url");
//        String html = new BufferedReader(new FileReader(url))
//                .lines()
//                .collect(Collectors.joining());
//        Document doc = Jsoup.parse(html, url);
//        parseAndUpdateStandings(doc.body());

//            String xml = new BufferedReader(new FileReader(url))
            String xml = new BufferedReader(new InputStreamReader((new URL(url)).openStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining());
            Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
            parseAndUpdateStandings(doc);
        } catch (IOException e) {
            log.error("error", e);
        }
    }

    @Override
    public void run() {
        //log.debug(check.getName() + " " + check.getShortName());
        while (true) {
            try {
                while (true) {
                    updateStatements();
                    sleep(5000);
                }
            } catch (IOException | InterruptedException e) {
                log.error("error", e);
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

        long previousStartTime = contestInfo.get().getStartTime();
        long currentTime = Long.parseLong(element.attr("time"));
        //log.debug("Time now " + currentTime);
        if (previousStartTime == 0 && !"before".equals(element.attr("status"))) {
            // if (previousStartTime < System.currentTimeMillis() - currentTime)
            updatedContestInfo.setStartTime(System.currentTimeMillis() - currentTime);
        } else {
            updatedContestInfo.setStartTime(previousStartTime);
        }
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
        String alias = element.attr("alias");
        PCMSTeamInfo oldTeamInfo = new PCMSTeamInfo(contestInfo.get().getParticipant(alias));
        PCMSTeamInfo teamInfo = new PCMSTeamInfo(oldTeamInfo.id, oldTeamInfo.alias, oldTeamInfo.name, oldTeamInfo.getShortName(), contestInfo.get().getProblemsNumber());

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
        long time = Long.parseLong(element.attr("time")) / 1000;
        boolean isFrozen = time >= 4 * 60 * 60;
        String result = isFrozen ? "" : ("yes".equals(element.attr("accepted")) ? "AC" : "WA");

        return new PCMSRunInfo(!isFrozen, result, problemId, time);
    }

    public PCMSContestInfo getContestData() {
        return contestInfo.get();
    }

    AtomicReference<PCMSContestInfo> contestInfo = new AtomicReference<PCMSContestInfo>();
    private Properties properties;
}
