package io.icpc.clics;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class JsonTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    Object [][] years = new Object[][] {{"2001",2001},{"2345",2345}};
    Object [][] months = new Object[][]{{"1",1},{"01",1},{"1",1},{"01",1},{"12",12}};
    Object [][] days = new Object[][] {{"7",7},{"22",22}};

    Object [][] hours = new Object[][] {{"00",0},{"0",0},{"1",1},{"8",8},{"08",8},{"23",23}};
    Object [][] minutes = new Object[][] {{"00",0},{"0",0},{"8",8},{"08",8},{"59",59}};
    Object [][] seconds = new Object[][] {{"00",0.0},{"0",0.0},{"8",8.0},{"08",8.0},{"59",59.0},
            {"1.2",1.2},{"1.23",1.23},{"1.234",1.234},{"1.23456",1.234}};
    Object [][] zones = new Object[][] {{"","+00:00"},{"Z","+00:00"},{"z","+00:00"},{"+12","+12:00"},{"+1","+01:00"},{"-730","-07:30"},{"+1234","+12:34"},{"-12:34","-12:34"}};

    Object [][] signs = new Object[][] {{"",1},{"+",1},{"-",-1}};

    @Test
    public void TIME() {
        for (Object [] YY : years) for (Object [] MM : months) for (Object [] DD :days)
            for (Object[] hh : hours) for (Object[] mm : minutes) for (Object [] ss : seconds) for (Object[] zz : zones) {
                String testTime = String.format("%s-%s-%sT%s:%s:%s%s",
                        YY[0], MM[0], DD[0], hh[0], mm[0], ss[0], zz[0]);
                double s = (double) ss[1];

                int is = (int) s;
                int ns = (int) Math.rint(1e9*(s-is));
                String isoDateTime = String.format("%04d-%02d-%02dT%02d:%02d:%02d.%09d%s",
                        YY[1], MM[1], DD[1], hh[1], mm[1], is, ns, zz[1]);

                ZonedDateTime zdt = ZonedDateTime.parse(isoDateTime, DateTimeFormatter.ISO_DATE_TIME);
                long expect = zdt.toInstant().toEpochMilli();
                long result = Json.TIME(testTime);
                assertEquals(testTime + " vs " + isoDateTime,expect, result);
            }
    }

    Object [][] relHours = new Object[][] {{null,0},{"00",0},{"0",0},{"1",1},{"8",8},{"08",8},{"23",23}};
    Object [][] relMinutes = new Object[][] {{null,0},{"00",0},{"0",0},{"8",8},{"08",8},{"59",59}};
    Object [][] relSeconds = new Object[][] {{"00",0.0},{"0",0.0},{"8",8.0},{"08",8.0},{"59",59.0},
            {"1.2",1.2},{"1.23",1.23},{"1.234",1.234},{"1.23456",1.235}};
    Object [][] relSigns = new Object[][] {{"",1},{"+",1},{"-",-1}};


    @Test
    public void RELTIME() {
        for (Object [] pp : relSigns) for (Object[] hh : relHours) for (Object[] mm : relMinutes) for (Object [] ss : relSeconds) {
            if (mm[0] == null && hh[0] != null) continue;
            String testRelTime = String.format("%s%s%s%s",
                    pp[0], hh[0] != null ? "" + hh[0] + ":" : "", mm[0] != null ? "" + mm[0] + ":" : "", ss[0]);
            double t = ((int) pp[1]) * (60.0 * 60.0 * ((int) hh[1]) + 60.0 *((int) mm[1]) + (double) ss[1]);
            long expect = Math.round(t * 1000.0);

            double s = (double) ss[1];
            int is = (int) s;
            int ns = (int) Math.round(1e9 * (s - is));
            String isoInstant = String.format("%s%02d:%02d:%02d.%09d",
                    ((int) pp[1]) == 1 ? "+" : "-", hh[1], mm[1], is, ns);

            long result = Json.RELTIME(testRelTime);

            assertEquals(testRelTime + " vs " + isoInstant, expect, result);
        }
    }

}