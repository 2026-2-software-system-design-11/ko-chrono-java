package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 원본이 없는 날짜를 넘치는 만큼 다음 달로 굴린 결과를 쓰는 네 자리(Calendars.dayCountedFrom)를 parse 결과로 확인한다.
 * 기대값은 원본 코드로 정했다. 시간대는 Asia/Seoul, 기준을 적지 않으면 2026-10-30 14:00.
 */
class DayOverflowTest {
    private static final LocalDateTime REF = at(2026, 10, 30, 14, 0);

    private static LocalDateTime at(int y, int m, int d, int h, int mi) {
        return LocalDateTime.of(y, m, d, h, mi);
    }

    private static ParseResult p(String text, LocalDateTime ref) {
        return KoChrono.parse(text, ParseOptions.defaults().withReferenceDate(ref.atZone(ZoneId.of("Asia/Seoul"))));
    }

    private static List<Object> range(String text) {
        ParseResult r = p(text, REF);
        return Arrays.asList(r.date().toLocalDateTime(), r.end().toLocalDateTime());
    }

    @Test @DisplayName("연도 없는 월일이 지났는지: 평년의 2월 29일은 3월 1일로 비교한다") void passed() {
        assertNull(p("2월 29일", at(2027, 3, 1, 15, 0)));
        assertEquals(at(2028, 2, 29, 0, 0), p("2월 29일", at(2027, 3, 2, 0, 0)).date().toLocalDateTime());
        assertEquals(at(2028, 2, 29, 0, 0), p("2월 29일", at(2027, 10, 1, 10, 0)).date().toLocalDateTime());
        ParseResult past = p("3월 0일에 갔어", at(2027, 2, 28, 12, 0));
        assertEquals(Arrays.asList(at(2027, 2, 28, 0, 0), Tense.PAST), Arrays.asList(past.date().toLocalDateTime(), past.tense()));
    }

    @Test @DisplayName("끝에 일만 있는 구간") void dayOnlyEnd() {
        assertEquals(List.of(at(2026, 11, 1, 0, 0), at(2026, 12, 1, 23, 59)), range("11월 1일부터 31일까지"));
        assertEquals(List.of(at(2027, 1, 31, 0, 0), at(2027, 3, 2, 23, 59)), range("1월 31일부터 30일까지"));
    }

    @Test @DisplayName("N달간") void months() {
        assertEquals(List.of(at(2027, 1, 31, 0, 0), at(2027, 3, 2, 23, 59)), range("1월 31일부터 한 달간"));
    }

    @Test @DisplayName("범위를 검사하지 않는 0일과 13월") void zeroAndThirteen() {
        assertEquals(at(2026, 10, 31, 0, 0), p("11월 0일", REF).date().toLocalDateTime());
        assertEquals(at(2027, 1, 31, 0, 0), p("13월 말", REF).date().toLocalDateTime());
        assertEquals(at(2027, 1, 5, 0, 0), p("13월 5일", REF).date().toLocalDateTime());
        assertEquals(at(2029, 9, 1, 0, 0), p("45월 1일", REF).date().toLocalDateTime());
    }
}
