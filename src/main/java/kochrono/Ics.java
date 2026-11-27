package kochrono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** 결과를 iCalendar(.ics) 문자열로 바꾼다 */
final class Ics {
    private Ics() {
    }

    private static String ymd(LocalDateTime d) {
        return d.getYear() + Rrule.pad(d.getMonthValue()) + Rrule.pad(d.getDayOfMonth());
    }

    private static String hms(LocalDateTime d) {
        return Rrule.pad(d.getHour()) + Rrule.pad(d.getMinute()) + Rrule.pad(d.getSecond());
    }

    private static String utc(Instant t) {
        LocalDateTime d = LocalDateTime.ofInstant(t, ZoneOffset.UTC);
        return ymd(d) + "T" + hms(d) + "Z";
    }

    /** 날짜가 없으면 null */
    static String toICS(String defaultTitle, ZonedDateTime date, ZonedDateTime end, boolean hasTime,
                        Recurrence r, IcsOptions options) {
        if (date == null) {
            return null;
        }
        IcsOptions o = options != null ? options : IcsOptions.defaults();
        String title = o.title() != null ? o.title() : defaultTitle;
        String uid = o.uid() != null ? o.uid() : Long.toString(System.currentTimeMillis(), 36) + "-"
                + Long.toString(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE), 36) + "@ko-chrono";
        List<String> lines = new ArrayList<>(List.of("BEGIN:VCALENDAR", "VERSION:2.0", "PRODID:-//ko-chrono//KO",
                "CALSCALE:GREGORIAN", "BEGIN:VEVENT", "UID:" + uid,
                "DTSTAMP:" + utc(o.now() != null ? o.now() : Instant.now()), "SUMMARY:" + title));
        if (hasTime) {
            ZonedDateTime last = end != null ? end
                    : date.plusMinutes(o.durationMinutes() != null ? o.durationMinutes() : 60);
            lines.add(at(o, "DTSTART", date));
            lines.add(at(o, "DTEND", last));
        }
        lines.add("END:VEVENT");
        lines.add("END:VCALENDAR");
        lines.add("");
        return String.join("\r\n", lines);
    }

    private static String at(IcsOptions o, String name, ZonedDateTime d) {
        return name + ":" + utc(d.toInstant());
    }
}
