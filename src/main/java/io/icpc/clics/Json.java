package io.icpc.clics;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Json {
    // https://ccs-specs.icpc.io/2021-11/contest_api#json-attribute-types
    private static final String DATE_STR = "([0-9]{1,4})-([0-9]{1,2})-([0-9]{1,2})";
    private static final String TIME_STR = "([0-9]{1,2}):([0-9]{1,2}):([0-9]{1,2}([.][0-9]{1,})?)";
    private static final String OPT_ZONE_STR = "(([-+])([0-9]{1,2}):?([0-9]{2})?)?[zZ]?";
    private static final Pattern TIME_PATTERN = Pattern.compile("^(" + DATE_STR + ")T(" + TIME_STR + ")(" + OPT_ZONE_STR + ")$");

    public static long TIME(CharSequence csTime) {
        Matcher matcher = TIME_PATTERN.matcher(csTime);
        if (matcher.matches()) {
            String yearStr = matcher.group(2);
            String monthStr = matcher.group(3);
            String dayStr = matcher.group(4);
            int year=Integer.parseInt(yearStr);
            if (yearStr.length() <= 2) {
                // https://www.ibm.com/docs/en/i/7.2?topic=mcdtdi-conversion-2-digit-years-4-digit-years-centuries
                year = (year >= 40) ? 1900 + year : 2000 + year;
            }
            int month=Integer.parseInt(monthStr);
            int day=Integer.parseInt(dayStr);
            String isoDate=String.format("%04d-%02d-%02d",year,month,day);

            String hourStr = matcher.group(6);
            String minuteStr = matcher.group(7);
            String secondStr = matcher.group(8);

            int hour=Integer.parseInt(hourStr);
            int minute=Integer.parseInt(minuteStr);
            double second=Double.parseDouble(secondStr);
            int iSecond = (int) second;
            int nanoSecond = (int) Math.rint(1e9*(second-iSecond));
            String isoTime = String.format("%02d:%02d:%02d.%09d",hour,minute,iSecond,nanoSecond);

            String offsetSignStr = matcher.group(12);
            String offsetHourStr = matcher.group(13);
            String offsetMinuteStr = matcher.group(14);
            int offsetSign = offsetSignStr != null && offsetSignStr.equals("-") ? -1 : 1;
            int offsetHour = offsetHourStr != null ? Integer.parseInt(offsetHourStr) : 0;
            int offsetMinute = offsetMinuteStr != null ? Integer.parseInt(offsetMinuteStr) : 0;

            String isoOffset = String.format("%c%02d:%02d",offsetSign == 1 ? '+' : '-',offsetHour,offsetMinute);

            String isoDateTime = isoDate + "T" + isoTime + isoOffset;
            ZonedDateTime zdt = ZonedDateTime.parse(isoDateTime , DateTimeFormatter.ISO_DATE_TIME);
            return zdt.toInstant().toEpochMilli();
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static final Pattern RELTIME_PATTERN = Pattern.compile("^([-+])?(([0-9]{1,}):)?(([0-9]{1,}):)?([0-9]{1,}([.][0-9]{1,})?)$");
    public static long RELTIME(CharSequence csTime) {
        Matcher matcher = RELTIME_PATTERN.matcher(csTime);
        if (matcher.matches()) {
            String signStr = matcher.group(1);
            String hourStr = matcher.group(3);
            String minuteStr = matcher.group(5);
            if (minuteStr == null && hourStr != null) {
                minuteStr = hourStr;
                hourStr = null;
            }
            String secondStr = matcher.group(6);
            int sign = signStr != null && signStr.equals("-") ? -1 : 1;
            int hour = hourStr != null ? Integer.parseInt(hourStr) : 0;
            int minute = minuteStr != null ? Integer.parseInt(minuteStr) : 0;
            double second = Double.parseDouble(secondStr);
            long millis = sign * Math.round(1000.0 * (60.0 * 60.0*hour + 60.0*minute + second));
            return millis;
        } else {
            throw new IllegalArgumentException();
        }
    }
}
