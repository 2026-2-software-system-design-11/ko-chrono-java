package kochrono;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Function;

/**
 * 한국어 문장에서 날짜와 시각을 찾아 값으로 바꾼다.
 *
 * <pre>{@code
 * ParseResult r = KoChrono.parse("다음주 화요일 오후 3시");
 * ZonedDateTime when = r.date();
 * }</pre>
 */
public final class KoChrono {
    private KoChrono() {
    }

    /**
     * 문장에서 모든 날짜와 시간 표현을 찾는다. 등장 순서대로 돌려주며, 없으면 빈 목록.
     * "A부터 B까지"는 하나의 구간으로 합쳐진다.
     */
    public static List<ParseResult> parseAll(String text, ParseOptions options) {
        ParseOptions o = options != null ? options : ParseOptions.defaults();
        ZonedDateTime ref = o.referenceDate() != null ? o.referenceDate()
                : o.timezone() != null ? ZonedDateTime.now(o.timezone()) : ZonedDateTime.now();
        ZoneId zone = o.timezone() != null ? o.timezone() : ref.getZone();
        // timezone 옵션이 없으면 원본은 기준 Date를 그대로 쓰고, 있으면 그 시간대의 벽시계로 Date를 다시 만든다
        LocalDateTime wallRef = o.timezone() != null ? WallClock.toWall(ref, zone) : ref.toLocalDateTime();
        ResolveOptions opts = new ResolveOptions(wallRef,
                o.weekStart() != null ? o.weekStart() : WeekStart.MONDAY,
                o.defaultHour() != null ? o.defaultHour() : 0);
        // 그 시간대의 벽시계로 계산하고, 결과를 실제 시점으로 되돌린다
        Function<LocalDateTime, ZonedDateTime> at = o.timezone() != null
                ? wall -> WallClock.fromWall(wall, zone) : wall -> WallClock.local(wall, zone);
        return Pipeline.run(text, opts).stream().map(r -> toResult(r, at)).toList();
    }

    public static List<ParseResult> parseAll(String text) {
        return parseAll(text, null);
    }

    /** 문장에서 첫 번째 날짜와 시간 표현을 찾는다. 없으면 null */
    public static ParseResult parse(String text, ParseOptions options) {
        List<ParseResult> all = parseAll(text, options);
        return all.isEmpty() ? null : all.get(0);
    }

    public static ParseResult parse(String text) {
        return parse(text, null);
    }

    /** parse의 date만 돌려주는 단축형. 구간이면 시작 시각. 없으면 null */
    public static ZonedDateTime parseDate(String text, ParseOptions options) {
        ParseResult r = parse(text, options);
        return r != null ? r.date() : null;
    }

    public static ZonedDateTime parseDate(String text) {
        return parseDate(text, null);
    }

    /**
     * 할 일이나 일정 한 줄을 제목과 시점으로 나눈다. 날짜로 인식된 부분과 거기 붙은 조사를 뺀 나머지가 제목이다.
     * "다음주 금요일까지 보고서 제출"은 제목 "보고서 제출", 마감 금요일 23:59.
     */
    public static ParsedEvent parseEvent(String text, ParseOptions options) {
        return EventTitle.toEvent(text, parseAll(text, options));
    }

    public static ParsedEvent parseEvent(String text) {
        return parseEvent(text, null);
    }

    /**
     * parse 결과를 iCalendar(.ics) 문자열로 바꾼다. 시각이 있으면 DTSTART와 DTEND(끝이 없으면 durationMinutes 뒤),
     * 없으면 종일 일정(VALUE=DATE, DTEND는 마지막 날의 다음날). 반복이면 RRULE을 넣는다.
     */
    public static String toICS(ParseResult result, IcsOptions options) {
        return Ics.toICS(result.text(), result.date(), result.end(), result.hasTime(), result.recurrence(), options);
    }

    public static String toICS(ParseResult result) {
        return toICS(result, null);
    }

    /** parseEvent 결과를 iCalendar 문자열로 바꾼다. 날짜가 없으면 null */
    public static String toICS(ParsedEvent event, IcsOptions options) {
        return Ics.toICS(event.title(), event.date(), event.end(), event.hasTime(), event.recurrence(), options);
    }

    public static String toICS(ParsedEvent event) {
        return toICS(event, null);
    }

    private static ParseResult toResult(LocalResult r, Function<LocalDateTime, ZonedDateTime> at) {
        Recurrence recurrence = null;
        if (r.recur != null) {
            RecurSpec s = r.recur;
            // rrule의 UNTIL은 벽시계 그대로 두고, 값만 실제 시점으로
            recurrence = new Recurrence(s.freq(), s.interval(), s.byWeekday().stream().map(KoChrono::dayOfWeek).toList(),
                    s.byWeekdayNth(), s.byMonthDay(), s.byMonth(), s.until() != null ? at.apply(s.until()) : null,
                    s.count(), r.rrule);
        }
        Ambiguity ambiguity = r.ambiguity == null ? null
                : new Ambiguity(r.ambiguity, r.alternatives.stream().map(at).toList());
        return new ParseResult(at.apply(r.date), r.end != null ? at.apply(r.end) : null, r.text, r.index, r.hasTime,
                recurrence, ambiguity, r.past ? Tense.PAST : null);
    }

    /** 0=일 ... 6=토를 DayOfWeek로 */
    static DayOfWeek dayOfWeek(int jsDay) {
        return jsDay == 0 ? DayOfWeek.SUNDAY : DayOfWeek.of(jsDay);
    }
}
