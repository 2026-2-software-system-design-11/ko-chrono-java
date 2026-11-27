package kochrono;

import static kochrono.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/** 원본 tests/parse.test.ts의 상대 일부터 주말까지. 기준: 2026-10-30 (금) 14:00 */
class Parse01Test {
    @Nested @DisplayName("상대 일")
    class G01 {
        @Test @DisplayName("오늘/내일/모레/글피") void c1() {
            assertEquals(d(2026, 10, 30), p("오늘").date());
            assertEquals(d(2026, 10, 31), p("내일").date());
            assertEquals(d(2026, 11, 1), p("모레").date());
            assertEquals(d(2026, 11, 2), p("글피").date());
        }
        @Test @DisplayName("어제/그저께") void c2() {
            assertEquals(d(2026, 10, 29), p("어제").date());
            assertEquals(d(2026, 10, 28), p("그저께").date());
        }
        @Test @DisplayName("내일 오후 3시") void c3() {
            ParseResult r = p("내일 오후 3시에 치과");
            assertEquals(d(2026, 10, 31, 15), r.date());
            assertEquals("내일 오후 3시", r.text());
            assertEquals(true, r.hasTime());
        }
    }

    @Nested @DisplayName("N단위 뒤/전")
    class G02 {
        @Test @DisplayName("3일 뒤") void c4() { assertEquals(d(2026, 11, 2), p("3일 뒤").date()); }
        @Test @DisplayName("2주 후") void c5() { assertEquals(d(2026, 11, 13), p("2주 후").date()); }
        // 규칙 24: N달 뒤/전은 같은 날짜, 그 달에 없으면 말일
        @Test @DisplayName("한 달 전") void c6() { assertEquals(d(2026, 9, 30), p("한 달 전").date()); }
        @Test @DisplayName("한 달 뒤") void c7() { assertEquals(d(2026, 11, 30), p("한 달 뒤").date()); }
        @Test @DisplayName("3개월 후") void c8() { assertEquals(d(2027, 1, 30), p("3개월 후").date()); }
        @Test @DisplayName("4달 뒤 (2월 30일은 없어서 말일)") void c9() { assertEquals(d(2027, 2, 28), p("4달 뒤").date()); }
        @Test @DisplayName("한 달 안에") void c10() { assertEquals(d(2026, 11, 30, 23, 59), p("한 달 안에 갚을게").end()); }
        @Test @DisplayName("5일 뒤") void c11() { assertEquals(d(2026, 11, 4), p("5일 뒤").date()); }
        @Test @DisplayName("세 시간 뒤") void c12() { assertEquals(d(2026, 10, 30, 17), p("세 시간 뒤").date()); }
        @Test @DisplayName("30분 후") void c13() { assertEquals(d(2026, 10, 30, 14, 30), p("30분 후").date()); }
        @Test @DisplayName("열흘 뒤") void c14() { assertEquals(d(2026, 11, 9), p("열흘 뒤").date()); }
        @Test @DisplayName("10분 뒤") void c15() { assertEquals(d(2026, 10, 30, 14, 10), p("10분 뒤").date()); }
        @Test @DisplayName("한 시간 반 뒤") void c16() {
            ParseResult r = p("한 시간 반 뒤에 출발");
            assertEquals(d(2026, 10, 30, 15, 30), r.date());
            assertEquals("한 시간 반 뒤", r.text());
        }
        @Test @DisplayName("1시간 30분 후") void c17() { assertEquals(d(2026, 10, 30, 15, 30), p("1시간 30분 후").date()); }
        @Test @DisplayName("2시간반 전") void c18() { assertEquals(d(2026, 10, 30, 11, 30), p("2시간반 전").date()); }
    }

    @Nested @DisplayName("요일")
    class G03 {
        // 2026-10-30은 금요일. 월요일 시작 주 기준.
        @Test @DisplayName("다음주 화요일") void c19() { assertEquals(d(2026, 11, 3), p("다음주 화요일").date()); }
        @Test @DisplayName("이번주 일요일") void c20() { assertEquals(d(2026, 11, 1), p("이번주 일요일").date()); }
        @Test @DisplayName("다다음주 월요일") void c21() { assertEquals(d(2026, 11, 9), p("다다음주 월요일").date()); }
        @Test @DisplayName("지난주 수요일") void c22() { assertEquals(d(2026, 10, 21), p("지난주 수요일").date()); }
        @Test @DisplayName("요일 단독 → 오늘 포함 가장 가까운 미래") void c23() {
            assertEquals(d(2026, 10, 30), p("금요일").date());
            assertEquals(d(2026, 11, 2), p("월요일").date());
        }
        @Test @DisplayName("다음주 (요일 없음) → 그 주 월요일") void c24() { assertEquals(d(2026, 11, 2), p("다음주").date()); }
        @Test @DisplayName("다음주 화요일 오후 3시") void c25() { assertEquals(d(2026, 11, 3, 15), p("다음주 화요일 오후 3시").date()); }
    }

    @Nested @DisplayName("주말")
    class G04 {
        // 주말 = 그 주의 토요일
        @Test @DisplayName("이번 주말") void c26() {
            ParseResult r = p("이번 주말");
            assertEquals(d(2026, 10, 31), r.date());
            assertEquals("이번 주말", r.text());
        }
        @Test @DisplayName("다음 주말") void c27() { assertEquals(d(2026, 11, 7), p("다음 주말").date()); }
        @Test @DisplayName("다음주말 저녁") void c28() { assertEquals(d(2026, 11, 7, 19), p("다음주말 저녁").date()); }
        @Test @DisplayName("지난 주말") void c29() { assertEquals(d(2026, 10, 24), p("지난 주말").date()); }
        @Test @DisplayName("이번 주말까지") void c30() { assertEquals(d(2026, 10, 31, 23, 59), p("이번 주말까지").end()); }
        // 규칙 11: 단독 "주말"은 다가오는 토요일 (오늘이 토/일이면 오늘)
        @Test @DisplayName("주말에 보자") void c31() {
            ParseResult r = p("주말에 보자");
            assertEquals(d(2026, 10, 31), r.date());
            assertEquals("주말", r.text());
        }
        @Test @DisplayName("평일에만 가능 (잡지 않음)") void c32() { assertNull(p("평일에만 가능")); }
    }
}
