package kochrono;

import static kochrono.Fixtures.REF;
import static kochrono.Fixtures.amb;
import static kochrono.Fixtures.at;
import static kochrono.Fixtures.d;
import static kochrono.Fixtures.utc;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 원본 tests/event.test.ts. 규칙 28: 제목은 날짜로 인식된 부분과 거기 붙은 조사를 뺀 나머지 */
@DisplayName("parseEvent: 제목 분리")
class EventTest {
    private static ParsedEvent ev(String s) {
        return KoChrono.parseEvent(s, at(REF));
    }

    @Test @DisplayName("내일 오후 3시 치과") void c1() {
        assertEquals(new ParsedEvent("치과", d(2026, 10, 31, 15), null, true, null, null, null), ev("내일 오후 3시 치과"));
    }

    @Test @DisplayName("치과 예약 내일 3시") void c2() {
        ParsedEvent e = ev("치과 예약 내일 3시");
        assertEquals("치과 예약", e.title());
        assertEquals(d(2026, 10, 31, 15), e.date());
    }

    @Test @DisplayName("치과 내일 3시 (모호성도 전달)") void c3() {
        assertEquals(amb("meridiem", d(2026, 10, 31, 3)), ev("치과 내일 3시").ambiguity());
    }

    @Test @DisplayName("다음주 금요일까지 보고서 제출") void c4() {
        assertEquals(new ParsedEvent("보고서 제출", REF, d(2026, 11, 6, 23, 59), false, null, null, null),
                ev("다음주 금요일까지 보고서 제출"));
    }

    @Test @DisplayName("내일 오후 3시에 강남역에서 친구 만나기") void c5() {
        assertEquals("강남역에서 친구 만나기", ev("내일 오후 3시에 강남역에서 친구 만나기").title());
    }

    @Test @DisplayName("모레까지는 꼭 끝내기") void c6() { assertEquals("꼭 끝내기", ev("모레까지는 꼭 끝내기").title()); }
    @Test @DisplayName("11월 1일부터 3일까지 제주도 여행") void c7() {
        assertEquals(new ParsedEvent("제주도 여행", d(2026, 11, 1), d(2026, 11, 3, 23, 59), false, null, null, null),
                ev("11월 1일부터 3일까지 제주도 여행"));
    }

    @Test @DisplayName("오늘부터 3일간 휴가") void c8() { assertEquals("휴가", ev("오늘부터 3일간 휴가").title()); }
    @Test @DisplayName("매주 월요일 오전 9시 팀 회의") void c9() {
        ParsedEvent e = ev("매주 월요일 오전 9시 팀 회의");
        assertEquals("팀 회의", e.title());
        assertEquals(d(2026, 11, 2, 9), e.date());
        assertEquals("FREQ=WEEKLY;BYDAY=MO", e.recurrence().rrule());
    }

    @Test @DisplayName("팀 회의, 매주 월요일") void c10() { assertEquals("팀 회의", ev("팀 회의, 매주 월요일").title()); }
    @Test @DisplayName("우유 사기 (날짜 없음)") void c11() {
        assertEquals(new ParsedEvent("우유 사기", null, null, false, null, null, null), ev("우유 사기"));
    }

    @Test @DisplayName("내일 (제목 없음)") void c12() {
        ParsedEvent e = ev("내일");
        assertEquals("", e.title());
        assertEquals(d(2026, 10, 31), e.date());
    }

    @Test @DisplayName("timezone 옵션도 전달된다") void c13() {
        ParsedEvent e = KoChrono.parseEvent("내일 오후 3시 치과", at(utc(2026, 10, 30, 5)).withTimezone("Asia/Seoul"));
        assertEquals(utc(2026, 10, 31, 6).toInstant(), e.date().toInstant());
    }
}
