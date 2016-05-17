package net.egork.teaminfo.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.PrintWriter;

/**
 * @author egor@egork.net
 */
public class Record {
    public final int id;
    public final University university;
    public final Team team;
    public final Person coach;
    public final Person[] contestants = new Person[3];

    @JsonCreator
    public Record(@JsonProperty("id") int id) {
        this.id = id;
        university = new University();
        team = new Team();
        coach = new Person();
        for (int i = 0; i < 3; i++) {
            contestants[i] = new Person();
        }
    }

    public void print(PrintWriter out) {
        out.println("University: " + university.getFullName());
        out.println("Team: " + team.getName());
        if (team.getOpenCupPlace() != -1) {
            out.println(String.format("OpenCup: %d (%d)", team.getOpenCupPlace(), team.getOpenCupTimes()));
        }
        out.println("Regionals:");
        for (String s : team.getRegionals()) {
            out.println(s);
        }
        out.println();
        out.println("Coach:");
        printPerson(coach, out);
        out.println();
        for (int i = 0; i < 3; i++) {
            out.println("Contestant " + (i + 1) + ":");
            printPerson(contestants[i], out);
            out.println();
        }
        out.println();
        out.println();
        out.println();
        out.println();
        out.println();
    }

    public static void printPerson(Person person, PrintWriter out) {
        out.println("Name: " + person.getName());
        out.println("TC handle: " + (person.getTcHandle() == null ? "" : person.getTcHandle()));
        out.println("CF handle: " + (person.getCfHandle() == null ? "" : person.getCfHandle()));
        out.println("Achievements:");
        for (Achievement achievement : person.getAchievements()) {
            out.println(achievement.achievement);
        }
    }
}
