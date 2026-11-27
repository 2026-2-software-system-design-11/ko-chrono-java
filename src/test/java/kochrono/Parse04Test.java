package kochrono;

import static kochrono.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/** 원본 tests/parse.test.ts의 기간 표현은 날짜가 아니다부터 기간 마감까지. 기준: 2026-10-30 (금) 14:00 */
class Parse04Test {
    @Nested @DisplayName("기간 표현은 날짜가 아니다")
    class G14 {
        @Test @DisplayName("5일간 (잡지 않음)") void c101() { assertNull(p("5일간 여행")); }
        @Test @DisplayName("3박 4일 (잡지 않음)") void c102() { assertNull(p("3박 4일")); }
        @Test @DisplayName("3일 동안 (잡지 않음)") void c103() { assertNull(p("3일 동안")); }
        @Test @DisplayName("월요일부터 금요일까지") void c104() {
            ParseResult r = p("월요일부터 금요일까지");
            assertEquals(d(2026, 11, 2), r.date());
            assertEquals(d(2026, 11, 6, 23, 59), r.end());
        }
    }

    @Nested @DisplayName("구어체")
    class G15 {
        @Test @DisplayName("담주 월욜") void c105() { assertEquals(d(2026, 11, 2), p("담주 월욜").date()); }
        @Test @DisplayName("화욜") void c106() { assertEquals(d(2026, 11, 3), p("화욜").date()); }
        @Test @DisplayName("낼 오후 2시") void c107() { assertEquals(d(2026, 10, 31, 14), p("낼 오후 2시").date()); }
        @Test @DisplayName("낼 봐") void c108() { assertEquals(d(2026, 10, 31), p("낼 봐").date()); }
        @Test @DisplayName("내가 낼게 (잡지 않음)") void c109() { assertNull(p("내가 낼게")); }
        @Test @DisplayName("밥값은 내가 낼 거야 (잡지 않음)") void c110() { assertNull(p("밥값은 내가 낼 거야")); }
        @Test @DisplayName("내일모레") void c111() { assertEquals(d(2026, 11, 1), p("내일모레 시간 돼?").date()); }
        @Test @DisplayName("낼모레") void c112() { assertEquals(d(2026, 11, 1), p("낼모레").date()); }
    }

    @Nested @DisplayName("요일 글자만 (규칙 36)")
    class G16 {
        // 요일로 본다: 뒤가 끝, 시각, 다른 요일 글자, 조사, 구두점
        @Test @DisplayName("다음주 월") void c113() { assertEquals(d(2026, 11, 2), p("다음주 월").date()); }
        @Test @DisplayName("다음주 월 2시") void c114() { assertEquals(d(2026, 11, 2, 14), p("다음주 월 2시").date()); }
        @Test @DisplayName("담주 금 저녁") void c115() { assertEquals(6, p("담주 금 저녁").date().getDayOfMonth()); }
        @Test @DisplayName("다음주 금에 보자") void c116() { assertEquals(d(2026, 11, 6), p("다음주 금에 보자").date()); }
        @Test @DisplayName("담주 월까지") void c117() { assertEquals(d(2026, 11, 2, 23, 59), p("담주 월까지").end()); }
        @Test @DisplayName("다음주 수, 목 가능") void c118() { assertEquals(d(2026, 11, 4), p("다음주 수, 목 가능").date()); }
        @Test @DisplayName("첫째 주 월 2시") void c119() { assertEquals(d(2026, 10, 5, 14), p("첫째 주 월 2시").date()); }
        // 요일이 아니다: 다른 단어의 첫 글자 (붙여 쓰든 띄어 쓰든) → 주만 남는다
        @Test @DisplayName("다음주 일정 공유") void c120() { assertEquals("다음주", p("다음주 일정 공유").text()); }
        @Test @DisplayName("이번주 목표") void c121() { assertEquals(d(2026, 10, 26), p("이번주 목표").date()); }
        @Test @DisplayName("지난주 토론 정리") void c122() { assertEquals(d(2026, 10, 19), p("지난주 토론 정리").date()); }
        @Test @DisplayName("담주 월급날") void c123() { assertEquals("담주", p("담주 월급날").text()); }
        @Test @DisplayName("다음주 일 많아") void c124() { assertEquals(d(2026, 11, 2), p("다음주 일 많아").date()); }
        @Test @DisplayName("이번주 목 아파") void c125() { assertEquals("이번주", p("이번주 목 아파").text()); }
        @Test @DisplayName("다음주 화 났어") void c126() { assertEquals("다음주", p("다음주 화 났어").text()); }
        @Test @DisplayName("다음주 금 모으기") void c127() { assertEquals("다음주", p("다음주 금 모으기").text()); }
        @Test @DisplayName("둘째 주 일 많아") void c128() { assertEquals(d(2026, 10, 12), p("둘째 주 일 많아").date()); }
    }

    @Nested @DisplayName("날짜 + 요일")
    class G17 {
        @Test @DisplayName("11월 5일 목요일 오후 4시") void c129() {
            ParseResult r = p("11월 5일 목요일 오후 4시");
            assertEquals(d(2026, 11, 5, 16), r.date());
            assertEquals("11월 5일 목요일 오후 4시", r.text());
        }
        @Test @DisplayName("11월 5일 수요일 (요일 불일치 → null)") void c130() { assertNull(p("11월 5일 수요일")); }
    }

    @Nested @DisplayName("기간 마감")
    class G18 {
        @Test @DisplayName("이번 주 안에") void c131() { assertEquals(d(2026, 11, 1, 23, 59), p("이번 주 안에").end()); }
        @Test @DisplayName("다음주 중으로") void c132() { assertEquals(d(2026, 11, 8, 23, 59), p("다음주 중으로").end()); }
        @Test @DisplayName("이번달 안에") void c133() { assertEquals(d(2026, 10, 31, 23, 59), p("이번달 안에").end()); }
        @Test @DisplayName("올해 안에") void c134() { assertEquals(d(2026, 12, 31, 23, 59), p("올해 안에").end()); }
        @Test @DisplayName("다음주 월요일까지 (요일은 그대로)") void c135() { assertEquals(d(2026, 11, 2, 23, 59), p("다음주 월요일까지").end()); }
    }
}
