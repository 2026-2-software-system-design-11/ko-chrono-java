package kochrono;

import static kochrono.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.*;

/** 원본 tests/parse.test.ts의 엣지 케이스부터 한글 월일까지. 기준: 2026-10-30 (금) 14:00 */
class Parse06Test {
    @Nested @DisplayName("엣지 케이스")
    class G23 {
        // 연말/월말 넘어감
        @Test @DisplayName("62일 뒤") void c158() { assertEquals(d(2026, 12, 31), p("62일 뒤").date()); }
        @Test @DisplayName("63일 뒤") void c159() { assertEquals(d(2027, 1, 1), p("63일 뒤").date()); }
        @Test @DisplayName("다다음달 말") void c160() { assertEquals(d(2026, 12, 31), p("다다음달 말").date()); }
        // 윤년
        @Test @DisplayName("2028년 2월 29일") void c161() { assertEquals(d(2028, 2, 29), p("2028년 2월 29일").date()); }
        @Test @DisplayName("2028-02-29") void c162() { assertEquals(d(2028, 2, 29), p("2028-02-29").date()); }
        // 없는 날짜는 다음 달로 넘기지 않고 null
        @Test @DisplayName("2027년 2월 29일 (윤년 아님)") void c163() { assertNull(p("2027년 2월 29일")); }
        @Test @DisplayName("2027-02-29") void c164() { assertNull(p("2027-02-29")); }
        @Test @DisplayName("11월 31일") void c165() { assertNull(p("11월 31일")); }
        @Test @DisplayName("11/31") void c166() { assertNull(p("11/31")); }
        @Test @DisplayName("다음달 31일") void c167() { assertNull(p("다음달 31일")); }
        // 24시 / 밤 12시
        @Test @DisplayName("24시") void c168() { assertEquals(d(2026, 10, 31, 0), p("24시").date()); }
        @Test @DisplayName("25시") void c169() { assertNull(p("25시")); }
        @Test @DisplayName("밤 12시") void c170() { assertEquals(d(2026, 10, 31, 0), p("밤 12시").date()); }
        @Test @DisplayName("오전 12시") void c171() { assertEquals(d(2026, 10, 31, 0), p("오전 12시").date()); }
        // 연도 단독
        @Test @DisplayName("2027년") void c172() {
            ParseResult r = p("2027년에 출시");
            assertEquals(d(2027, 1, 1), r.date());
            assertEquals("2027년", r.text());
        }
        @Test @DisplayName("3000년 (범위 밖, 잡지 않음)") void c173() { assertNull(p("3000년")); }
    }

    @Nested @DisplayName("띄어쓰기 변형")
    class G24 {
        @Test @DisplayName("다음주화요일") void c174() { assertEquals(d(2026, 11, 3), p("다음주화요일").date()); }
        @Test @DisplayName("오후3시") void c175() { assertEquals(d(2026, 10, 30, 15), p("오후3시").date()); }
        @Test @DisplayName("11월5일") void c176() { assertEquals(d(2026, 11, 5), p("11월5일").date()); }
        @Test @DisplayName("다음 주 화요일") void c177() { assertEquals(d(2026, 11, 3), p("다음 주 화요일").date()); }
        @Test @DisplayName("내일오후3시반에봐") void c178() {
            ParseResult r = p("내일오후3시반에봐");
            assertEquals(d(2026, 10, 31, 15, 30), r.date());
            assertEquals("내일오후3시반", r.text());
        }
        @Test @DisplayName("3일뒤") void c179() { assertEquals(d(2026, 11, 2), p("3일뒤").date()); }
        @Test @DisplayName("다음달말") void c180() { assertEquals(d(2026, 11, 30), p("다음달말").date()); }
        @Test @DisplayName("이번주말까지") void c181() { assertEquals(d(2026, 10, 31, 23, 59), p("이번주말까지").end()); }
        @Test @DisplayName("2027년1월2일") void c182() { assertEquals(d(2027, 1, 2), p("2027년1월2일").date()); }
    }

    @Nested @DisplayName("조사 변형")
    class G25 {
        @Test @DisplayName("내일은") void c183() { assertEquals(d(2026, 10, 31), p("내일은 안 돼").date()); }
        @Test @DisplayName("모레까지는") void c184() { assertEquals(d(2026, 11, 1, 23, 59), p("모레까지는 끝낼게").end()); }
        @Test @DisplayName("3시쯤") void c185() {
            ParseResult r = p("오후 3시쯤 갈게");
            assertEquals(d(2026, 10, 30, 15), r.date());
            assertEquals("오후 3시", r.text());
        }
        @Test @DisplayName("3시경") void c186() { assertEquals(d(2026, 10, 30, 15), p("오후 3시경").date()); }
        // 날짜와 시각 사이에 조사가 끼어도 하나로 묶는다
        @Test @DisplayName("내일은 오후 3시에") void c187() {
            List<ParseResult> r = parseAll("내일은 오후 3시에 시간 돼", REF);
            assertEquals(1, r.size());
            assertEquals(d(2026, 10, 31, 15), r.get(0).date());
        }
        @Test @DisplayName("토요일쯤 저녁에") void c188() { assertEquals(d(2026, 10, 31, 19), p("토요일쯤 저녁에").date()); }
        @Test @DisplayName("다음주 화요일이나 오후 3시") void c189() {
            // "이나"는 묶지 않는다 (다른 선택지일 수 있음)
            assertEquals(2, parseAll("다음주 화요일이나 오후 3시", REF).size());
        }
        @Test @DisplayName("모레도 오후 2시") void c190() { assertEquals(d(2026, 11, 1, 14), p("모레도 오후 2시 괜찮아").date()); }
    }

    @Nested @DisplayName("한글 월일")
    class G26 {
        @Test @DisplayName("십일월 오일") void c191() {
            ParseResult r = p("십일월 오일에 봐");
            assertEquals(d(2026, 11, 5), r.date());
            assertEquals("십일월 오일", r.text());
        }
        @Test @DisplayName("유월 이십오일") void c192() { assertEquals(d(2027, 6, 25), p("유월 이십오일").date()); }
        @Test @DisplayName("시월 삼십일일") void c193() { assertEquals(d(2026, 10, 31), p("시월 삼십일일").date()); }
        @Test @DisplayName("십이월 이십오일") void c194() { assertEquals(d(2026, 12, 25), p("십이월 이십오일").date()); }
        // 오탐 방지: 월 없이 "오일"만 있으면 잡지 않는다
        @Test @DisplayName("오일 교환 (잡지 않음)") void c195() { assertNull(p("오일 교환")); }
    }
}
