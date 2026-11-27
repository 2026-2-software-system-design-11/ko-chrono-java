package kochrono;

import static kochrono.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/** 원본 tests/parse.test.ts (시각, 구간). 기준: 2026-10-30 (금) 14:00 */
class Parse08Test {
    @Nested @DisplayName("시각")
    class G30 {
        @Test @DisplayName("오전/오후") void c216() {
            assertEquals(d(2026, 10, 31, 10, 30), p("오전 10시 30분").date());
            // 14:00 기준 지났으니 내일
            assertEquals(d(2026, 10, 30, 15), p("오후 3시").date());
            assertEquals(d(2026, 10, 30, 19), p("저녁 7시").date());
            assertEquals(d(2026, 10, 31, 2), p("새벽 2시").date());
            assertEquals(d(2026, 10, 31, 12), p("낮 12시").date());
        }
        @Test @DisplayName("고유어 시") void c217() {
            assertEquals(d(2026, 10, 31, 15), p("내일 세 시").date());
            assertEquals(d(2026, 10, 31, 14, 30), p("내일 오후 두시 반").date());
        }
        @Test @DisplayName("15:30") void c218() { assertEquals(d(2026, 10, 31, 15, 30), p("내일 15:30").date()); }
        @Test @DisplayName("오후 12시 (24시가 아니라 정오)") void c219() { assertEquals(d(2026, 10, 31, 12), p("내일 오후 12시").date()); }
        @Test @DisplayName("오후 12시 30분") void c220() { assertEquals(d(2026, 10, 31, 12, 30), p("내일 오후 12시 30분").date()); }
        @Test @DisplayName("오전/오후 없는 1~6시는 오후 (규칙 20)") void c221() {
            assertEquals(d(2026, 10, 30, 15), p("3시에 보자").date());
            assertEquals(d(2026, 10, 31, 14, 30), p("내일 2시 반").date());
            assertEquals(d(2026, 10, 30, 18), p("6시").date());
            assertEquals(d(2026, 10, 31, 7), p("7시").date());
            // 7~11시는 그대로
            assertEquals(d(2026, 10, 31, 12), p("12시").date());
            assertEquals(d(2026, 10, 30, 17), p("5시까지 보내줘").end());
            assertEquals(d(2026, 10, 31, 3, 30), p("내일 3:30").date());
            // 24시간 표기는 그대로
        }
        @Test @DisplayName("밤 1시 (새벽)") void c222() { assertEquals(d(2026, 10, 31, 1), p("밤 1시").date()); }
        @Test @DisplayName("밤 11시 반") void c223() { assertEquals(d(2026, 10, 30, 23, 30), p("밤 11시 반").date()); }
        @Test @DisplayName("오후 3:30") void c224() { assertEquals(d(2026, 10, 30, 15, 30), p("오후 3:30").date()); }
        @Test @DisplayName("오전 9:00") void c225() { assertEquals(d(2026, 10, 31, 9), p("내일 오전 9:00").date()); }
        @Test @DisplayName("7시30") void c226() { assertEquals(d(2026, 10, 31, 7, 30), p("내일 7시30").date()); }
        @Test @DisplayName("3시 정각") void c227() {
            ParseResult r = p("3시 정각에");
            assertEquals(d(2026, 10, 30, 15), r.date());
            assertEquals("3시 정각", r.text());
        }
        @Test @DisplayName("3pm") void c228() { assertEquals(d(2026, 10, 30, 15), p("3pm").date()); }
        @Test @DisplayName("9 AM") void c229() { assertEquals(d(2026, 10, 31, 9), p("내일 9 AM").date()); }
        @Test @DisplayName("12pm") void c230() { assertEquals(d(2026, 10, 31, 12), p("내일 12pm").date()); }
        @Test @DisplayName("점심/정오/자정") void c231() {
            assertEquals(d(2026, 11, 1, 12), p("모레 점심").date());
            // 규칙 12: 날짜 + 자정 = 그 날의 끝 (다음날 00:00)
            assertEquals(d(2026, 11, 1, 0), p("내일 자정").date());
        }
        @Test @DisplayName("자정") void c232() { assertEquals(d(2026, 10, 31, 0), p("자정").date()); }
        @Test @DisplayName("오늘 자정") void c233() { assertEquals(d(2026, 10, 31, 0), p("오늘 자정").date()); }
        @Test @DisplayName("내일 24시") void c234() { assertEquals(d(2026, 11, 1, 0), p("내일 24시").date()); }
        @Test @DisplayName("내일 밤 12시") void c235() { assertEquals(d(2026, 11, 1, 0), p("내일 밤 12시").date()); }
        @Test @DisplayName("내일 자정까지") void c236() { assertEquals(d(2026, 11, 1, 0), p("내일 자정까지").end()); }
        @Test @DisplayName("내일 오전 12시") void c237() { assertEquals(d(2026, 10, 31, 0), p("내일 오전 12시").date()); }
    }

    @Nested @DisplayName("구간")
    class G31 {
        @Test @DisplayName("이번달 말까지") void c238() {
            ParseResult r = p("이번달 말까지 제출");
            assertEquals(REF, r.date());
            assertEquals(d(2026, 10, 31, 23, 59), r.end());
        }
        @Test @DisplayName("3일 이내") void c239() { assertEquals(d(2026, 11, 2, 23, 59), p("3일 이내").end()); }
        @Test @DisplayName("11월 1일부터 11월 3일까지") void c240() {
            ParseResult r = p("11월 1일부터 11월 3일까지 휴가");
            assertEquals(d(2026, 11, 1), r.date());
            assertEquals(d(2026, 11, 3, 23, 59), r.end());
            assertEquals("11월 1일부터 11월 3일까지", r.text());
        }
    }
}
