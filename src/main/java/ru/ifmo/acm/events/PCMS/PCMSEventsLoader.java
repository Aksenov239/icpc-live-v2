package ru.ifmo.acm.events.PCMS;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import ru.ifmo.acm.events.EventsLoader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
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
                //properties.load(new FileInputStream("src/main/resources/events.properties"));
                PCMSContestInfo initial = parseInitialContestInfo();
                contestInfo = new AtomicReference<>(initial);
                instance.start();
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
        participants.children().forEach(participant -> {
            String id = participant.attr("id");
            String participantName = participant.attr("name");
            String shortName = id; //participant.attr("short");

            PCMSTeamInfo team = new PCMSTeamInfo(id, participantName, shortName, initial.getProblemsNumber());
            initial.addTeamStandings(team);
        });
        return initial;
    }

    private void updateStatements() throws IOException {
        String url = properties.getProperty("url");
        String html = new BufferedReader(new InputStreamReader(new URL(url).openStream()))
                .lines()
                .collect(Collectors.joining());
        Document doc = Jsoup.parse(html, url);
        parseAndUpdateStandings(doc.body());
    }

    @Override
    public void run() {
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
        if ("center".equals(element.tagName())) {
            PCMSContestInfo updatedContestInfo = parseContestInfo(element);
            contestInfo.set(updatedContestInfo);
        } else {
            element.children().forEach(this::parseAndUpdateStandings);
        }
    }


    private long parseTime(String s) {
        String[] times = s.trim().split(":");
        long time = 60 * Integer.parseInt(times[0]) + Integer.parseInt(times[1]);
        if (times.length > 2) {
            time = time * 60 + Integer.parseInt(times[2]);
        }

        return time;
    }

    private int numberPreviousAttempts(String standing) {
        switch (standing.charAt(0)) {
            case '.':
                return 0;
            case '+':
                return standing.length() > 1 ? Integer.parseInt(standing.substring(1)) : 1;
            default: // - or ?
                return Integer.parseInt(standing.substring(1));
        }
    }

    private PCMSRunInfo parseRun(Node element, PCMSTeamInfo previousStandings, int problemId) {
        String text = (element instanceof TextNode) ? ((TextNode) element).getWholeText() : ((Element) element).ownText();
        int attemptsNumber = numberPreviousAttempts(text);
        boolean isNewRun = (previousStandings.getRunsNumber(problemId) != attemptsNumber);

        if (isNewRun) {
            boolean isAccepted = text.startsWith("+");
            boolean isJudged = !text.startsWith("?"); // ? for frozen results
            String result = (isJudged) ? (isAccepted ? "AC" : "REJ") : "FROZEN";
            boolean firstToSolve = "first-to-solve".equals(element.attr("class"));

            long time = (isAccepted && element instanceof Element)
                    ? parseTime(((Element) element).child(0).ownText())
                    : currentTime;

            return new PCMSRunInfo(isJudged, result, problemId, time, firstToSolve);
        }

        return null;
    }

    private PCMSTeamInfo parseTeamStandings(Element element) {
        int problemsNumber = contestInfo.get().getProblemsNumber();
        String name = element.child(1).html();
        PCMSTeamInfo parsedTeam = new PCMSTeamInfo(contestInfo.get().getParticipant(name));
        parsedTeam.rank = Integer.parseInt(element.child(0).html());

        for (int i = 0; i < problemsNumber; i++) {
            PCMSRunInfo run = parseRun(element.child(2 + i).childNode(0), parsedTeam, i);
            parsedTeam.addRun(run, i);
        }

        int lastIndex = element.childNodeSize() - 1;
        parsedTeam.solved = Integer.parseInt(element.child(lastIndex - 1).html());
        parsedTeam.penalty = Integer.parseInt(element.child(lastIndex).html());

        return parsedTeam;
    }

    private PCMSContestInfo parseContestInfo(Element element) {
        int problemsNumber = Integer.parseInt(properties.getProperty("problemsNumber"));
        PCMSContestInfo updateContestInfo = new PCMSContestInfo(problemsNumber);
        long contestTime = parseTime(element.child(1).ownText().split("of")[0]);
        updateContestInfo.setCurrentTime(contestTime);
        element = element.child(4).child(0);
        for (int i = 1; i < element.childNodeSize(); i++) {
            if ("rankl".equals(element.child(i).child(0).attr("class"))) {
                PCMSTeamInfo team = parseTeamStandings(element.child(i));
                updateContestInfo.standings.add(team);
            }
        }
        return updateContestInfo;
    }

    public PCMSContestInfo getContestData() {
        return contestInfo.get();
    }

    static AtomicReference<PCMSContestInfo> contestInfo;
    long currentTime;
    private static Properties properties;
}
