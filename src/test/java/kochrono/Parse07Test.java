package kochrono;

import static kochrono.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/** 원본 tests/parse.test.ts의 연도 상대부터 월까지. 기준: 2026-10-30 (금) 14:00 */
class Parse07Test {
    @Nested @DisplayName("연도 상대")
    class G27 {
        @Test @DisplayName("올해 말") void c196() { assertEquals(d(2026, 12, 31), p("올해 말").date()); }
        @Test @DisplayName("내년 초") void c197() { assertEquals(d(2027, 1, 1), p("내년 초").date()); }
        @Test @DisplayName("작년") void c198() {
            ParseResult r = p("작년에 갔던 곳");
            assertEquals(d(2025, 1, 1), r.date());
            assertEquals("작년", r.text());
        }
        @Test @DisplayName("재작년") void c199() { assertEquals(d(2024, 1, 1), p("재작년").date()); }
        @Test @DisplayName("내후년") void c200() { assertEquals(d(2028, 1, 1), p("내후년").date()); }
        // 연도가 정해졌으므로 "지난 월일 → 내년" 규칙을 적용하지 않는다
        @Test @DisplayName("올해 3월 1일") void c201() { assertEquals(d(2026, 3, 1), p("올해 3월 1일").date()); }
        @Test @DisplayName("내년 3월 1일") void c202() { assertEquals(d(2027, 3, 1), p("내년 3월 1일").date()); }
        @Test @DisplayName("작년 12월") void c203() { assertEquals(d(2025, 12, 1), p("작년 12월").date()); }
        @Test @DisplayName("올해 말까지") void c204() { assertEquals(d(2026, 12, 31, 23, 59), p("올해 말까지").end()); }
    }

    @Nested @DisplayName("두 자리 연도")
    class G28 {
        // 00~69 → 20xx, 70~99 → 19xx
        @Test @DisplayName("26년 12월 1일") void c205() {
            ParseResult r = p("26년 12월 1일");
            assertEquals(d(2026, 12, 1), r.date());
            assertEquals("26년 12월 1일", r.text());
        }
        // 연도가 없었다면 "지난 월일 → 내년" 규칙으로 2027년이 됐을 날짜
        @Test @DisplayName("25년 3월 1일") void c206() { assertEquals(d(2025, 3, 1), p("25년 3월 1일").date()); }
        @Test @DisplayName("99년 1월 1일") void c207() { assertEquals(d(1999, 1, 1), p("99년 1월 1일").date()); }
        @Test @DisplayName("27.1.2") void c208() {
            ParseResult r = p("27.1.2");
            assertEquals(d(2027, 1, 2), r.date());
            assertEquals("27.1.2", r.text());
        }
        @Test @DisplayName("25-03-01") void c209() { assertEquals(d(2025, 3, 1), p("25-03-01").date()); }
        @Test @DisplayName("70.5.6") void c210() { assertEquals(d(1970, 5, 6), p("70.5.6").date()); }
        @Test @DisplayName("26.13.1 (없는 월은 무시)") void c211() { assertNull(p("26.13.1")); }
    }

    @Nested @DisplayName("월")
    class G29 {
        @Test @DisplayName("이번달 말") void c212() { assertEquals(d(2026, 10, 31), p("이번달 말").date()); }
        @Test @DisplayName("다음달 초") void c213() { assertEquals(d(2026, 11, 1), p("다음달 초").date()); }
        @Test @DisplayName("다음 달 중순") void c214() { assertEquals(d(2026, 11, 15), p("다음 달 중순").date()); }
        @Test @DisplayName("12월 말") void c215() { assertEquals(d(2026, 12, 31), p("12월 말").date()); }
    }
}
