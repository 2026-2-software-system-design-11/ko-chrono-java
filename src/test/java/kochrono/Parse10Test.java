package kochrono;

import static kochrono.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.*;

/** 원본 tests/parse.test.ts (빠진 표현 메우기). 기준: 2026-10-30 (금) 14:00 */
class Parse10Test {
    @Nested @DisplayName("빠진 표현 메우기")
    class G33 {
        @Test @DisplayName("내일 9시부터 6시까지 (text 잘림 버그)") void c255() {
            ParseResult r = p("내일 9시부터 6시까지 근무");
            assertEquals("내일 9시부터 6시까지", r.text());
            assertEquals(d(2026, 10, 31, 18), r.end());
        }
        @Test @DisplayName("회의는 오후 3시 (index는 공백이 아니라 글자 위치)") void c256() { assertEquals(4, p("회의는 오후 3시").index()); }
        // 규칙 25: 시간대 단어만 있으면 오전 9시, 오후 15시
        @Test @DisplayName("내일 오후") void c257() {
            ParseResult r = p("내일 오후에 연락드릴게요");
            assertEquals(d(2026, 10, 31, 15), r.date());
            assertEquals(true, r.hasTime());
        }
        @Test @DisplayName("글피 오전") void c258() { assertEquals(d(2026, 11, 2, 9), p("글피 오전").date()); }
        @Test @DisplayName("오후에 연락할게") void c259() { assertEquals(d(2026, 10, 30, 15), p("오후에 연락할게").date()); }
        @Test @DisplayName("내일 오후 3시 (기존 동작 유지)") void c260() { assertEquals(d(2026, 10, 31, 15), p("내일 오후 3시").date()); }
        @Test @DisplayName("2시부터 4시 (까지 없이)") void c261() {
            ParseResult r = p("2시부터 4시 회의");
            assertEquals(Arrays.asList(d(2026, 10, 30, 14), d(2026, 10, 30, 16)), Arrays.asList(r.date(), r.end()));
            assertEquals("2시부터 4시", r.text());
        }
        @Test @DisplayName("11월 1일부터 3일 (까지 없이)") void c262() { assertEquals(d(2026, 11, 3, 23, 59), p("11월 1일부터 3일 휴가").end()); }
        @Test @DisplayName("30분쯤 뒤") void c263() { assertEquals(d(2026, 10, 30, 14, 30), p("30분쯤 뒤").date()); }
        @Test @DisplayName("한 시간 정도 후") void c264() { assertEquals(d(2026, 10, 30, 15), p("한 시간 정도 후").date()); }
        @Test @DisplayName("2주 뒤 월요일") void c265() {
            ParseResult r = p("2주 뒤 월요일");
            assertEquals(d(2026, 11, 9), r.date());
            assertEquals("2주 뒤 월요일", r.text());
        }
        @Test @DisplayName("3주 후 금요일 오후 2시") void c266() { assertEquals(d(2026, 11, 20, 14), p("3주 후 금요일 오후 2시").date()); }
        @Test @DisplayName("2027-01-01T09:00") void c267() { assertEquals(d(2027, 1, 1, 9), p("2027-01-01T09:00").date()); }
        @Test @DisplayName("2027-01-01 09:30") void c268() { assertEquals(d(2027, 1, 1, 9, 30), p("2027-01-01 09:30").date()); }
        @Test @DisplayName("내일 이 시간") void c269() {
            ParseResult r = p("내일 이 시간에 다시 봐요");
            assertEquals(d(2026, 10, 31, 14), r.date());
            assertEquals(true, r.hasTime());
        }
        @Test @DisplayName("다음 주 이 시간") void c270() { assertEquals(d(2026, 11, 6, 14), p("다음 주 이 시간").date()); }
        @Test @DisplayName("조금 이따 (잡지 않음)") void c271() { assertNull(p("조금 이따 갈게")); }
        @Test @DisplayName("같이 시간 내자 (잡지 않음)") void c272() { assertNull(p("같이 시간 내자")); }
        @Test @DisplayName("이 시간대엔 막혀 (잡지 않음)") void c273() { assertNull(p("이 시간대엔 막혀")); }
        @Test @DisplayName("오전반 수업 (잡지 않음)") void c274() { assertNull(p("오전반 수업")); }
        @Test @DisplayName("오후 반차 (잡지 않음)") void c275() { assertNull(p("오후 반차 쓸게")); }
        @Test @DisplayName("오전오후 나눠서 (잡지 않음)") void c276() { assertNull(p("오전오후 나눠서")); }
        @Test @DisplayName("나중에 (잡지 않음)") void c277() { assertNull(p("나중에 얘기하자")); }
    }
}
