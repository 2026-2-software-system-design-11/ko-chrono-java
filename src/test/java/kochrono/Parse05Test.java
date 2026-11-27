package kochrono;

import static kochrono.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.*;

/** 원본 tests/parse.test.ts의 날짜 + 상대 시간은 따로 (규칙 15)부터 요일/날짜 + 시각 단어까지. 기준: 2026-10-30 (금) 14:00 */
class Parse05Test {
    @Nested @DisplayName("날짜 + 상대 시간은 따로 (규칙 15)")
    class G19 {
        List<ParseResult> all(String s) {
            return parseAll(s, REF);
        }

        @Test @DisplayName("내일 30분 후") void c136() {
            var r = all("내일 30분 후");
            assertEquals(2, r.size());
            assertEquals(d(2026, 10, 31), r.get(0).date());
            assertEquals(d(2026, 10, 30, 14, 30), r.get(1).date());
        }
        @Test @DisplayName("이번 주말 한 시간 반 뒤") void c137() {
            var r = all("이번 주말 한 시간 반 뒤");
            assertEquals(2, r.size());
            assertEquals(d(2026, 10, 31), r.get(0).date());
        }
        // 일 단위 상대 이동은 시각과 묶인다
        @Test @DisplayName("3일 뒤 오후 3시") void c138() {
            var r = all("3일 뒤 오후 3시");
            assertEquals(1, r.size());
            assertEquals(d(2026, 11, 2, 15), r.get(0).date());
        }
    }

    @Nested @DisplayName("날짜가 명시된 지난 시각 (규칙 14)")
    class G20 {
        @Test @DisplayName("오늘 아침") void c139() { assertEquals(d(2026, 10, 30, 8), p("오늘 아침").date()); }
        @Test @DisplayName("금요일 아침") void c140() { assertEquals(d(2026, 10, 30, 8), p("금요일 아침").date()); }
        @Test @DisplayName("오늘 오전 10시") void c141() { assertEquals(d(2026, 10, 30, 10), p("오늘 오전 10시").date()); }
    }

    @Nested @DisplayName("절대 날짜")
    class G21 {
        @Test @DisplayName("11월 5일") void c142() { assertEquals(d(2026, 11, 5), p("11월 5일").date()); }
        @Test @DisplayName("2027년 1월 2일") void c143() { assertEquals(d(2027, 1, 2), p("2027년 1월 2일").date()); }
        @Test @DisplayName("11/5 10시 반") void c144() { assertEquals(d(2026, 11, 5, 10, 30), p("11/5 10시 반").date()); }
        @Test @DisplayName("2026-12-25") void c145() { assertEquals(d(2026, 12, 25), p("2026-12-25").date()); }
        @Test @DisplayName("지난 월일은 내년으로") void c146() { assertEquals(d(2027, 3, 1), p("3월 1일").date()); }
        // 일 단독: 오늘이 N일을 넘었으면 다음 달, 아니면 이번 달 (오늘 포함)
        @Test @DisplayName("5일 (일 단독, 지났으면 다음 달)") void c147() { assertEquals(d(2026, 11, 5), p("5일에 보자").date()); }
        @Test @DisplayName("30일 (오늘이면 오늘)") void c148() { assertEquals(d(2026, 10, 30), p("30일에 보자").date()); }
        @Test @DisplayName("31일 (아직 안 지났으면 이번 달)") void c149() { assertEquals(d(2026, 10, 31), p("31일에 보자").date()); }
        @Test @DisplayName("5일까지 (1일에 말하면 이번 달)") void c150() {
            assertEquals(d(2026, 11, 5, 23, 59), parse("5일까지 주세요", d(2026, 11, 1, 10)).end());
        }
        @Test @DisplayName("5일까지 (지났으면 다음 달)") void c151() { assertEquals(d(2026, 11, 5, 23, 59), p("5일까지 주세요").end()); }
        // 그 달에 없는 날이면 추측하지 않는다 (chrono-node처럼 보수적으로)
        @Test @DisplayName("31일 (이번 달에 없으면 null)") void c152() { assertNull(parse("31일에 보자", d(2026, 11, 15))); }
        @Test @DisplayName("30일 (지나서 넘어간 다음 달에 없으면 null)") void c153() { assertNull(parse("30일까지", d(2027, 1, 31))); }
    }

    @Nested @DisplayName("요일/날짜 + 시각 단어")
    class G22 {
        @Test @DisplayName("토요일 아침") void c154() {
            ParseResult r = p("토요일 아침");
            assertEquals(d(2026, 10, 31, 8), r.date());
            assertEquals("토요일 아침", r.text());
            assertEquals(true, r.hasTime());
        }
        @Test @DisplayName("내일 저녁") void c155() {
            ParseResult r = p("내일 저녁");
            assertEquals(d(2026, 10, 31, 19), r.date());
            assertEquals("내일 저녁", r.text());
        }
        @Test @DisplayName("다음주 화요일 점심") void c156() { assertEquals(d(2026, 11, 3, 12), p("다음주 화요일 점심").date()); }
        @Test @DisplayName("모레 밤에 전화할게") void c157() {
            ParseResult r = p("모레 밤에 전화할게");
            assertEquals(d(2026, 11, 1, 21), r.date());
            assertEquals("모레 밤", r.text());
        }
    }
}
