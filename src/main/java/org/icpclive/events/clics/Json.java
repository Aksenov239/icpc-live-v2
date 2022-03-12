package org.icpclive.events.clics;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// https://ccs-specs.icpc.io/2021-11/contest_api#json-attribute-types
public class Json {
    private static final Pattern TIME_PATTERN = Pattern.compile("^(([0-9]{1,4})-([0-9]{1,2})-([0-9]{1,2}))T(([0-9]{1,2}):([0-9]{1,2}):([0-9]{1,2}([.][0-9]{1,})?))(([-+])([0-9]{2}):?([0-9]{2})?)?$");
    public static long TIME(CharSequence csTime) {
        Matcher matcher = TIME_PATTERN.matcher(csTime);
        if (matcher.matches()) {
            int year=Integer.parseInt(matcher.group(2));
            int month=Integer.parseInt(matcher.group(3));
            int day=Integer.parseInt(matcher.group(4));
            String isoDate=String.format("%04d-%02d-%02d",year,month,day);

            int hour=Integer.parseInt(matcher.group(6));
            int minute=Integer.parseInt(matcher.group(7));
            double second=Double.parseDouble(matcher.group(8));
            int iSecond = (int) second;
            int nanos = (int) Math.rint(1e9*(second-iSecond));
            String isoTime = String.format("%02d:%02d:%02d.%09d",hour,minute,iSecond,nanos);

            int offsetSign = 0;
            int offsetHour = 0;
            int offsetMinute = 0;
            if (matcher.group(10) != null) {
                offsetSign = matcher.group(11).equals("+") ? 1 : -1;
                offsetHour = Integer.parseInt(matcher.group(12));
                if (matcher.group(13) != null) {
                    offsetMinute = Integer.parseInt(matcher.group(13));
                }
            }
            String isoOffset = offsetSign == 0 ? "Z" : String.format("%c%02d:%02d",offsetSign == 1 ? '+' : '-',offsetHour,offsetMinute);

            String isoDateTime = isoDate + "T" + isoTime + isoOffset;
            ZonedDateTime zdt = ZonedDateTime.parse(isoDateTime , DateTimeFormatter.ISO_DATE_TIME);
            return zdt.toInstant().toEpochMilli();
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static final Pattern RELTIME_PATTERN = Pattern.compile("^([-+])?(([0-9]{1,2}):([0-9]{1,2}):([0-9]{1,2}([.][0-9]{1,})?))$");
    public static long RELTIME(CharSequence csTime) {
        Matcher matcher = RELTIME_PATTERN.matcher(csTime);
        if (matcher.matches()) {
            int sign = 1;
            if (matcher.group(1) != null) {
                sign = matcher.group(1).equals("+") ? 1 : -1;
            }

            int hour = Integer.parseInt(matcher.group(3));
            int minute = Integer.parseInt(matcher.group(4));
            double second = Double.parseDouble(matcher.group(5));

            long millis = sign * Math.round(1000.0 * (60.0 * (60.0 * hour + minute) + second));
            return millis;

        } else {
            throw new IllegalArgumentException();
        }
    }
}
