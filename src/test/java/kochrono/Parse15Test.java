package kochrono;

import static kochrono.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.*;

/** 원본 tests/parse.test.ts (음력 날짜 (규칙 32), 모호성 정보 (규칙 31)). 기준: 2026-10-30 (금) 14:00 */
class Parse15Test {
    @Nested @DisplayName("음력 날짜 (규칙 32)")
    class G40 {
        // 음력 2026년 3월 1일 = 양력 4/17 → 3/3 = 4/19 (지났음) → 음력 2027년 3/3 = 2027-04-09
        @Test @DisplayName("음력 3월 3일") void c397() {
            ParseResult r = p("음력 3월 3일 할머니 생신");
            assertEquals(d(2027, 4, 9), r.date());
            assertEquals("음력 3월 3일", r.text());
        }
        @Test @DisplayName("음력 10월 15일") void c398() { assertEquals(d(2026, 11, 23), p("음력 10월 15일").date()); }
        @Test @DisplayName("음력 12월 1일 (양력으로는 다음 해 1월)") void c399() { assertEquals(d(2027, 1, 8), p("음력 12월 1일").date()); }
        @Test @DisplayName("음력 9월 9일 오후 3시") void c400() { assertEquals(d(2027, 10, 8, 15), p("음력 9월 9일 오후 3시").date()); }
        @Test @DisplayName("2028년 음력 윤5월 1일") void c401() { assertEquals(d(2028, 6, 23), p("2028년 음력 윤5월 1일").date()); }
        @Test @DisplayName("음력 윤5월 1일 (윤5월이 있는 해까지)") void c402() { assertEquals(d(2028, 6, 23), p("음력 윤5월 1일").date()); }
        @Test @DisplayName("음력 2월 30일 (30일이 있는 해의 것)") void c403() { assertEquals(d(2027, 4, 6), p("음력 2월 30일").date()); }
        @Test @DisplayName("2029년 음력 1월 1일 (자료 없음 → null)") void c404() { assertNull(p("2029년 음력 1월 1일")); }
        @Test @DisplayName("음력 생일 (날짜 없음)") void c405() { assertNull(p("음력 생일")); }
    }

    @Nested @DisplayName("모호성 정보 (규칙 31)")
    class G41 {
        @Test @DisplayName("3시에 보자 → 15시, 후보 새벽 3시") void c406() {
            ParseResult r = p("3시에 보자");
            assertEquals(d(2026, 10, 30, 15), r.date());
            assertEquals(amb("meridiem", d(2026, 10, 31, 3)), r.ambiguity());
        }
        @Test @DisplayName("내일 7시 → 7시, 후보 19시") void c407() {
            assertEquals(amb("meridiem", d(2026, 10, 31, 19)), p("내일 7시").ambiguity());
        }
        @Test @DisplayName("12시 → 정오, 후보 자정") void c408() {
            assertEquals(amb("meridiem", d(2026, 10, 31, 0)), p("12시").ambiguity());
        }
        @Test @DisplayName("5시까지 → 마감의 후보") void c409() {
            assertEquals(amb("meridiem", d(2026, 10, 31, 5)), p("5시까지 보내줘").ambiguity());
        }
        @Test @DisplayName("오후 3시 (모호하지 않음)") void c410() { assertNull(p("오후 3시").ambiguity()); }
        @Test @DisplayName("15:30 (모호하지 않음)") void c411() { assertNull(p("15:30").ambiguity()); }
        @Test @DisplayName("다음주 화요일 오후 3시 (모호하지 않음)") void c412() { assertNull(p("다음주 화요일 오후 3시").ambiguity()); }
        @Test @DisplayName("다음 토요일 → 내일, 후보 다음 주 토요일") void c413() {
            ParseResult r = p("다음 토요일");
            assertEquals(d(2026, 10, 31), r.date());
            assertEquals(amb("next-weekday", d(2026, 11, 7)), r.ambiguity());
        }
        @Test @DisplayName("다음 금요일 (두 해석이 같으면 모호하지 않음)") void c414() { assertNull(p("다음 금요일").ambiguity()); }
        @Test @DisplayName("내일 저녁 → 관례 시각") void c415() { assertEquals(amb("approximate-time"), p("내일 저녁").ambiguity()); }
        @Test @DisplayName("timezone에서도 후보가 실제 시점으로 바뀐다") void c416() {
            ParseResult r = parse("3시에 보자", utc(2026, 10, 30, 5), "Asia/Seoul");
            assertEquals(List.of(utc(2026, 10, 30, 18).toInstant()), instants(r.ambiguity().alternatives()));
        }
    }
}
