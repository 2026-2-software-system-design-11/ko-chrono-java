package kochrono;

import static kochrono.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.*;

/** 원본 tests/parse.test.ts의 A부터 N일간 (규칙 21)부터 내내까지. 기준: 2026-10-30 (금) 14:00 */
class Parse03Test {
    @Nested @DisplayName("A부터 N일간 (규칙 21)")
    class G10 {
        List<ZonedDateTime> range(String s) {
            ParseResult r = p(s);
            return Arrays.asList(r.date(), r.end());
        }

        @Test @DisplayName("오늘부터 3일간") void c68() {
            ParseResult r = p("오늘부터 3일간 휴가");
            assertEquals(Arrays.asList(d(2026, 10, 30), d(2026, 11, 1, 23, 59)), Arrays.asList(r.date(), r.end()));
            assertEquals("오늘부터 3일간", r.text());
        }
        @Test @DisplayName("내일부터 2주간") void c69() {
            assertEquals(Arrays.asList(d(2026, 10, 31), d(2026, 11, 13, 23, 59)), range("내일부터 2주간"));
        }
        @Test @DisplayName("11월 1일부터 한 달간") void c70() {
            assertEquals(Arrays.asList(d(2026, 11, 1), d(2026, 11, 30, 23, 59)), range("11월 1일부터 한 달간"));
        }
        @Test @DisplayName("내일부터 3박 4일") void c71() {
            assertEquals(Arrays.asList(d(2026, 10, 31), d(2026, 11, 3, 23, 59)), range("내일부터 3박 4일"));
        }
        @Test @DisplayName("3시부터 2시간 동안") void c72() {
            assertEquals(Arrays.asList(d(2026, 10, 30, 15), d(2026, 10, 30, 17)), range("3시부터 2시간 동안"));
        }
        @Test @DisplayName("내일 오전 10시부터 90분간") void c73() {
            assertEquals(Arrays.asList(d(2026, 10, 31, 10), d(2026, 10, 31, 11, 30)), range("내일 오전 10시부터 90분간"));
        }
    }

    @Nested @DisplayName("N째 주 (규칙 22)")
    class G11 {
        // 2026-10: 월 5,12,19,26 / 금 2,9,16,23,30. 2026-11: 월 2,9,16,23,30 / 토 7,14,21,28
        @Test @DisplayName("11월 첫째 주 월요일") void c74() { assertEquals(d(2026, 11, 2), p("11월 첫째 주 월요일").date()); }
        @Test @DisplayName("11월 첫째 주 일요일") void c75() { assertEquals(d(2026, 11, 1), p("11월 첫째 주 일요일").date()); }
        @Test @DisplayName("첫째 주 월요일 (이번 달)") void c76() { assertEquals(d(2026, 10, 5), p("첫째 주 월요일").date()); }
        @Test @DisplayName("11월 둘째주") void c77() { assertEquals(d(2026, 11, 9), p("11월 둘째주").date()); }
        @Test @DisplayName("둘째 주 일정 (요일 아님)") void c78() { assertEquals(d(2026, 10, 12), p("둘째 주 일정").date()); }
        @Test @DisplayName("마지막 주 금요일") void c79() { assertEquals(d(2026, 10, 30), p("마지막 주 금요일").date()); }
        @Test @DisplayName("다음 달 마지막 주 토요일") void c80() { assertEquals(d(2026, 11, 28), p("다음 달 마지막 주 토요일").date()); }
        @Test @DisplayName("11월 다섯째 주 월요일") void c81() { assertEquals(d(2026, 11, 30), p("11월 다섯째 주 월요일").date()); }
        @Test @DisplayName("11월 다섯째 주 토요일 (없음 → null)") void c82() { assertNull(p("11월 다섯째 주 토요일")); }
        @Test @DisplayName("11월 둘째 주 수요일 오후 3시") void c83() { assertEquals(d(2026, 11, 11, 15), p("11월 둘째 주 수요일 오후 3시").date()); }
        @Test @DisplayName("2월 첫째 주 (지난 달 → 내년)") void c84() { assertEquals(d(2027, 2, 1), p("2월 첫째 주").date()); }
    }

    @Nested @DisplayName("오탐 방지")
    class G12 {
        @Test @DisplayName("2시간짜리 영화") void c85() { assertNull(p("2시간짜리 영화")); }
        @Test @DisplayName("12시간 근무") void c86() { assertNull(p("12시간 근무")); }
        @Test @DisplayName("24시간 편의점") void c87() { assertNull(p("24시간 편의점")); }
        @Test @DisplayName("한 시간짜리") void c88() { assertNull(p("한 시간짜리")); }
        @Test @DisplayName("3일 연속") void c89() { assertNull(p("3일 연속")); }
        @Test @DisplayName("1월생") void c90() { assertNull(p("1월생")); }
        @Test @DisplayName("12월생이야") void c91() { assertNull(p("12월생이야")); }
        @Test @DisplayName("밤새 작업했어") void c92() { assertNull(p("밤새 작업했어")); }
        @Test @DisplayName("주말농장") void c93() { assertNull(p("주말농장")); }
        @Test @DisplayName("연말정산") void c94() { assertNull(p("연말정산 서류")); }
        @Test @DisplayName("주말에 (정상)") void c95() { assertEquals(d(2026, 10, 31), p("주말에").date()); }
        @Test @DisplayName("3시간 뒤 (정상)") void c96() { assertEquals(d(2026, 10, 30, 17), p("3시간 뒤").date()); }
    }

    @Nested @DisplayName("내내")
    class G13 {
        @Test @DisplayName("이번 주 내내") void c97() {
            ParseResult r = p("이번 주 내내 비 온대");
            assertEquals(d(2026, 10, 26), r.date());
            assertEquals(d(2026, 11, 1, 23, 59), r.end());
            assertEquals("이번 주 내내", r.text());
        }
        @Test @DisplayName("다음 달 내내") void c98() {
            ParseResult r = p("다음 달 내내");
            assertEquals(Arrays.asList(d(2026, 11, 1), d(2026, 11, 30, 23, 59)), Arrays.asList(r.date(), r.end()));
        }
        @Test @DisplayName("내년 내내") void c99() { assertEquals(d(2027, 12, 31, 23, 59), p("내년 내내").end()); }
        @Test @DisplayName("내일 내내") void c100() { assertEquals(d(2026, 10, 31, 23, 59), p("내일 내내").end()); }
    }
}
