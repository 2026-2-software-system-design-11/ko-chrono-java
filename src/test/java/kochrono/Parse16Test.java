package kochrono;

import static kochrono.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.*;

/** 원본 tests/parse.test.ts (timezone 옵션, 문장 속). 기준: 2026-10-30 (금) 14:00 */
class Parse16Test {
    @Nested @DisplayName("timezone 옵션")
    class G42 {
        // 기준 시각 ref = 2026-10-30 05:00 UTC = 서울 14:00 = 뉴욕 01:00 (EDT, 11/1에 EST로 바뀜)
        // 결과는 기계 시간대와 무관한 실제 시점이어야 한다 (서버가 UTC여도 같은 답)
        // 원본은 Date(실제 시점)를 비교한다. 여기서도 시점(Instant)을 비교한다
        // 파일 상단의 ref는 기계 로컬 14:00이라, 여기서는 기계와 무관한 실제 시점으로 고정한다
        final ZonedDateTime at = utc(2026, 10, 30, 5);

        ParseResult tz(String s, String timezone) {
            return parse(s, at, timezone);
        }

        @Test @DisplayName("오후 3시 (Asia/Seoul)") void c417() { assertEquals(utc(2026, 10, 30, 6).toInstant(), tz("오후 3시", "Asia/Seoul").date().toInstant()); }
        @Test @DisplayName("다음주 화요일 오후 3시 (Asia/Seoul)") void c418() {
            assertEquals(utc(2026, 11, 3, 6).toInstant(), tz("다음주 화요일 오후 3시", "Asia/Seoul").date().toInstant());
        }
        @Test @DisplayName("오후 3시 (UTC)") void c419() { assertEquals(utc(2026, 10, 30, 15).toInstant(), tz("오후 3시", "UTC").date().toInstant()); }
        @Test @DisplayName("내일 (UTC)") void c420() { assertEquals(utc(2026, 10, 31).toInstant(), tz("내일", "UTC").date().toInstant()); }
        @Test @DisplayName("오후 3시 (America/New_York)") void c421() {
            assertEquals(utc(2026, 10, 30, 19).toInstant(), tz("오후 3시", "America/New_York").date().toInstant());
        }
        // 서머타임이 끝난 뒤(EST, UTC-5)의 날짜
        @Test @DisplayName("다음주 월요일 (America/New_York)") void c422() {
            assertEquals(utc(2026, 11, 2, 5).toInstant(), tz("다음주 월요일", "America/New_York").date().toInstant());
        }
        @Test @DisplayName("이번 주 안에 (America/New_York)") void c423() {
            ParseResult r = tz("이번 주 안에", "America/New_York");
            assertEquals(at.toInstant(), r.date().toInstant());
            assertEquals(utc(2026, 11, 2, 4, 59).toInstant(), r.end().toInstant());
        }
        @Test @DisplayName("오늘부터 3일간 (Asia/Seoul)") void c424() {
            ParseResult r = tz("오늘부터 3일간", "Asia/Seoul");
            assertEquals(instants(List.of(utc(2026, 10, 29, 15), utc(2026, 11, 1, 14, 59))), instants(List.of(r.date(), r.end())));
        }
        @Test @DisplayName("없는 시간대는 RangeError") void c425() {
            // 원본의 RangeError는 Java의 DateTimeException이다
            assertThrows(DateTimeException.class, () -> tz("내일", "Mars/Olympus"));
        }
    }

    @Nested @DisplayName("문장 속")
    class G43 {
        @Test @DisplayName("문장 중간") void c426() {
            ParseResult r = p("그럼 다음주 수요일 저녁 7시에 강남에서 볼까?");
            assertEquals(d(2026, 11, 4, 19), r.date());
            assertEquals("다음주 수요일 저녁 7시", r.text());
            assertEquals(3, r.index());
        }
        @Test @DisplayName("여러 개") void c427() {
            List<ParseResult> rs = parseAll("내일 10시 아니면 모레 2시", REF);
            assertEquals(2, rs.size());
            assertEquals(d(2026, 10, 31, 10), rs.get(0).date());
            assertEquals(d(2026, 11, 1, 14), rs.get(1).date());
        }
        @Test @DisplayName("없으면 null") void c428() {
            assertNull(p("안녕하세요"));
            assertNull(p("일단 해보자"));
        }
    }
}
