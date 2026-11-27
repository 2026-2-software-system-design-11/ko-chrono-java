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
        if (!hasTime) {
            LocalDateTime first = wall(o, date);
            LocalDateTime last = end != null ? wall(o, end) : first;
            LocalDateTime after = last.plusDays(1);
            lines.add("DTSTART;VALUE=DATE:" + ymd(first));
            lines.add("DTEND;VALUE=DATE:" + ymd(after));
        }
        if (r != null) {
            // recurrence.rrule의 UNTIL은 벽시계(floating) 형식이라, 시각이 있는 일정이면 UTC로 바꿔 쓴다
            String rrule = r.until() != null && hasTime
                    ? Rrule.of(spec(r), false) + ";UNTIL=" + utc(r.until().toInstant()) : r.rrule();
            lines.add("RRULE:" + rrule);
        }
        lines.add("END:VEVENT");
        lines.add("END:VCALENDAR");
        lines.add("");
        return String.join("\r\n", lines);
    }

    /** 시간대 옵션이 있으면 그 시간대의 벽시계, 없으면 값이 가진 시간대의 벽시계 */
    private static LocalDateTime wall(IcsOptions o, ZonedDateTime d) {
        return o.timezone() != null ? WallClock.toWall(d, o.timezone()) : d.toLocalDateTime();
    }

    private static String at(IcsOptions o, String name, ZonedDateTime d) {
        if (o.timezone() == null) {
            return name + ":" + utc(d.toInstant());
        }
        LocalDateTime w = wall(o, d);
        return name + ";TZID=" + o.timezone().getId() + ":" + ymd(w) + "T" + hms(w);
    }

    /** UNTIL을 뺀 반복 규칙 */
    private static RecurSpec spec(Recurrence r) {
        List<Integer> days = r.byWeekday().stream().map(d -> d.getValue() % 7).toList();
        return new RecurSpec(r.freq(), r.interval(), days, r.byWeekdayNth(), r.byMonthDay(), r.byMonth(), null,
                r.count());
    }
}
