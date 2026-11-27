package kochrono;

import static kochrono.Fixtures.utc;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 원본 tests/ics.test.ts. 실행 환경 시간대와 상관없이 같은 결과가 나오도록 서울 시간대로 고정한다 */
@DisplayName("toICS")
class IcsTest {
    private static final ZonedDateTime REF = utc(2026, 10, 30, 5); // 2026-10-30 (금) 14:00 서울
    private static final ParseOptions OPTS = ParseOptions.defaults().withReferenceDate(REF).withTimezone("Asia/Seoul");
    private static final IcsOptions FIXED = IcsOptions.defaults().withUid("test@ko-chrono").withNow(REF.toInstant())
            .withTimezone("Asia/Seoul");

    private static List<String> lines(String ics) {
        return Arrays.asList(ics.split("\r\n", -1));
    }

    private static List<String> event(String ics) {
        List<String> ls = lines(ics);
        return ls.subList(ls.indexOf("BEGIN:VEVENT") + 1, ls.indexOf("END:VEVENT"));
    }

    private static ParsedEvent ev(String s) {
        return KoChrono.parseEvent(s, OPTS);
    }

    private static ParseResult p(String s) {
        return KoChrono.parse(s, OPTS);
    }

    @Test @DisplayName("다음달까지 매주 월요일 9시 팀 회의") void c1() {
        String ics = KoChrono.toICS(ev("다음달까지 매주 월요일 9시 팀 회의"), FIXED);
        assertEquals(String.join("\r\n",
                "BEGIN:VCALENDAR",
                "VERSION:2.0",
                "PRODID:-//ko-chrono//KO",
                "CALSCALE:GREGORIAN",
                "BEGIN:VEVENT",
                "UID:test@ko-chrono",
                "DTSTAMP:20261030T050000Z",
                "SUMMARY:팀 회의",
                "DTSTART;TZID=Asia/Seoul:20261102T090000",
                "DTEND;TZID=Asia/Seoul:20261102T100000",
                // TZID가 있는 DTSTART와 함께 쓰는 UNTIL은 UTC여야 한다 (RFC 5545 3.3.10)
                "RRULE:FREQ=WEEKLY;BYDAY=MO;UNTIL=20261130T145900Z",
                "END:VEVENT",
                "END:VCALENDAR",
                ""), ics);
    }

    @Test @DisplayName("내일 오후 3시부터 5시까지 회의 (끝이 있으면 DTEND)") void c2() {
        assertTrue(event(KoChrono.toICS(ev("내일 오후 3시부터 5시까지 회의"), FIXED)).containsAll(List.of(
                "DTSTART;TZID=Asia/Seoul:20261031T150000", "DTEND;TZID=Asia/Seoul:20261031T170000")));
    }

    @Test @DisplayName("timezone이 없으면 UTC로") void c3() {
        List<String> ev = event(KoChrono.toICS(ev("내일 오후 3시 치과"),
                IcsOptions.defaults().withUid("u").withNow(REF.toInstant())));
        assertTrue(ev.contains("DTSTART:20261031T060000Z"));
        assertTrue(ev.contains("DTEND:20261031T070000Z"));
    }

    @Test @DisplayName("durationMinutes") void c4() {
        assertTrue(event(KoChrono.toICS(ev("내일 오후 3시 치과"), FIXED.withDurationMinutes(30)))
                .contains("DTEND;TZID=Asia/Seoul:20261031T153000"));
    }

    @Test @DisplayName("크리스마스에 파티 (시각이 없으면 종일)") void c5() {
        List<String> ev = event(KoChrono.toICS(ev("크리스마스에 파티"), FIXED));
        assertTrue(ev.contains("SUMMARY:파티"));
        assertTrue(ev.contains("DTSTART;VALUE=DATE:20261225"));
        assertTrue(ev.contains("DTEND;VALUE=DATE:20261226"));
    }

    @Test @DisplayName("11월 16일부터 20일까지 출장 (종일 구간은 끝 다음날이 DTEND)") void c6() {
        List<String> ev = event(KoChrono.toICS(ev("11월 16일부터 20일까지 출장"), FIXED));
        assertTrue(ev.contains("DTSTART;VALUE=DATE:20261116"));
        assertTrue(ev.contains("DTEND;VALUE=DATE:20261121"));
    }

    @Test @DisplayName("11월 1일부터 12월 31일까지 매주 토요일 (종일 반복은 UNTIL도 날짜)") void c7() {
        assertTrue(event(KoChrono.toICS(ev("11월 1일부터 12월 31일까지 매주 토요일 봉사"), FIXED)).containsAll(List.of(
                "DTSTART;VALUE=DATE:20261107", "RRULE:FREQ=WEEKLY;BYDAY=SA;UNTIL=20261231")));
    }

    @Test @DisplayName("매주 수요일 오후 2시, 총 8회 (COUNT는 그대로)") void c8() {
        assertTrue(event(KoChrono.toICS(p("매주 수요일 오후 2시, 총 8회"), FIXED.withTitle("영어 회화"))).containsAll(List.of(
                "SUMMARY:영어 회화", "RRULE:FREQ=WEEKLY;BYDAY=WE;COUNT=8")));
    }

    @Test @DisplayName("날짜가 없으면 null") void c11() { assertNull(KoChrono.toICS(ev("우유 사기"))); }
    @Test @DisplayName("제목이 없으면 문장에서 찾은 부분") void c12() {
        assertTrue(event(KoChrono.toICS(p("내일 3시"), FIXED)).contains("SUMMARY:내일 3시"));
    }
}
