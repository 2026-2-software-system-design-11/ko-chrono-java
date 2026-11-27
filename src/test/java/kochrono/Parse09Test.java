package kochrono;

import static kochrono.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.*;

/** 원본 tests/parse.test.ts (시 범위 (규칙 16)). 기준: 2026-10-30 (금) 14:00 */
class Parse09Test {
    @Nested @DisplayName("시 범위 (규칙 16)")
    class G32 {
        List<ZonedDateTime> range(String s) {
            ParseResult r = p(s);
            return Arrays.asList(r.date(), r.end());
        }

        @Test @DisplayName("3시부터 5시까지") void c241() {
            assertEquals(Arrays.asList(d(2026, 10, 30, 15), d(2026, 10, 30, 17)), range("3시부터 5시까지"));
        }
        @Test @DisplayName("오후 3시부터 5시까지") void c242() {
            assertEquals(Arrays.asList(d(2026, 10, 30, 15), d(2026, 10, 30, 17)), range("오후 3시부터 5시까지"));
        }
        @Test @DisplayName("내일 오후 2시부터 4시 반까지") void c243() {
            assertEquals(Arrays.asList(d(2026, 10, 31, 14), d(2026, 10, 31, 16, 30)), range("내일 오후 2시부터 4시 반까지"));
        }
        @Test @DisplayName("내일 오전 9시부터 6시까지") void c244() {
            assertEquals(Arrays.asList(d(2026, 10, 31, 9), d(2026, 10, 31, 18)), range("내일 오전 9시부터 6시까지"));
        }
        @Test @DisplayName("오후 11시부터 1시까지") void c245() {
            assertEquals(Arrays.asList(d(2026, 10, 30, 23), d(2026, 10, 31, 1)), range("오후 11시부터 1시까지"));
        }
        @Test @DisplayName("오후 3시부터 오전 1시까지") void c246() {
            assertEquals(Arrays.asList(d(2026, 10, 30, 15), d(2026, 10, 31, 1)), range("오후 3시부터 오전 1시까지"));
        }
        @Test @DisplayName("3~5시") void c247() {
            assertEquals(Arrays.asList(d(2026, 10, 30, 15), d(2026, 10, 30, 17)), range("3~5시"));
        }
        @Test @DisplayName("내일 오후 3~5시 회의") void c248() {
            ParseResult r = p("내일 오후 3~5시 회의");
            assertEquals(Arrays.asList(d(2026, 10, 31, 15), d(2026, 10, 31, 17)), Arrays.asList(r.date(), r.end()));
            assertEquals("내일 오후 3~5시", r.text());
        }
        @Test @DisplayName("오후 3시~5시") void c249() {
            assertEquals(Arrays.asList(d(2026, 10, 30, 15), d(2026, 10, 30, 17)), range("오후 3시~5시"));
        }
        // 끝에 일만 있으면 시작의 연월을 이어받는다
        @Test @DisplayName("11월 1일부터 3일까지") void c250() {
            assertEquals(Arrays.asList(d(2026, 11, 1), d(2026, 11, 3, 23, 59)), range("11월 1일부터 3일까지"));
        }
        @Test @DisplayName("11월 1일~3일") void c251() {
            assertEquals(Arrays.asList(d(2026, 11, 1), d(2026, 11, 3, 23, 59)), range("11월 1일~3일"));
        }
        @Test @DisplayName("11월 28일부터 2일까지") void c252() {
            assertEquals(Arrays.asList(d(2026, 11, 28), d(2026, 12, 2, 23, 59)), range("11월 28일부터 2일까지"));
        }
        @Test @DisplayName("11/1~11/3") void c253() {
            assertEquals(Arrays.asList(d(2026, 11, 1), d(2026, 11, 3, 23, 59)), range("11/1~11/3"));
        }
        @Test @DisplayName("15:00~17:00") void c254() {
            assertEquals(Arrays.asList(d(2026, 10, 30, 15), d(2026, 10, 30, 17)), range("15:00~17:00"));
        }
    }
}
