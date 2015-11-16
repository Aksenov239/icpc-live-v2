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
    public PCMSEventsLoader() throws IOException {
        properties.load(new FileInputStream("events.properties"));
        PCMSContestInfo initial = parseInitialContestInfo();
        contestInfo = new AtomicReference<>(initial);
    }

    private PCMSContestInfo parseInitialContestInfo() throws IOException {
        int problemsNumber = Integer.parseInt(properties.getProperty("problemsNumber"));
        PCMSContestInfo initial = new PCMSContestInfo(problemsNumber);
        String fn = properties.getProperty("participants");
        String xml = new String(Files.readAllBytes(Paths.get(fn)));
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Element participants = doc.child(0);
        participants.children().forEach(participant -> {
            String participantName = participant.attr("name");
            initial.addTeamStandings(new PCMSTeamInfo(participantName, initial.getProblemsNumber()));
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
        try {
            while (true) {
                updateStatements();
                sleep(1000);
            }
        } catch (IOException| InterruptedException e) {
            e.printStackTrace();
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
            case '-':
                return Integer.parseInt(standing.substring(1));
        }

        return 0;
    }

    private PCMSRunInfo parseRun(Node element, PCMSTeamInfo previousStandings, int problemId) {
        String text = (element instanceof TextNode) ? ((TextNode) element).getWholeText() : ((Element) element).ownText();
        int attemptsNumber = numberPreviousAttempts(text);
        boolean isNewRun = (previousStandings.getRunsNumber(problemId) != attemptsNumber);

        if (isNewRun) {
            PCMSRunInfo parsedRun = new PCMSRunInfo();
            boolean isAccepted = text.startsWith("+");
            parsedRun.result = isAccepted ? "AC" : "REJ";
            parsedRun.firstToSolve = "first-to-solve".equals(element.attr("class"));

            parsedRun.time = (isAccepted && element instanceof Element)
                    ? parseTime(((Element) element).child(0).ownText())
                    : currentTime;

            return parsedRun;
        }

        return null;
    }

    private PCMSTeamInfo parseTeamStandings(Element element) {
        int problemsNumber = contestInfo.get().getProblemsNumber();
        String name = element.child(1).html();
        PCMSTeamInfo parsedTeam = contestInfo.get().getParticipant(name);
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

    public static PCMSContestInfo getContestData() {
        return contestInfo.get();
    }

    static AtomicReference<PCMSContestInfo> contestInfo;
    long currentTime;
    private Properties properties;
}
