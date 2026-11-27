package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

/** 원본이 JavaScript Date의 성질에 기대는 동작. 기대값은 원본 코드로 정했다 */
class DateRulesTest {
    private static LocalDateTime date(String text, LocalDateTime ref, String zone) {
        ParseResult r = KoChrono.parse(text, ParseOptions.defaults().withReferenceDate(ref.atZone(ZoneId.of(zone))));
        return r == null ? null : r.date().toLocalDateTime();
    }

    private static LocalDateTime at(int y, int m, int d, int h, int mi) {
        return LocalDateTime.of(y, m, d, h, mi);
    }

    @Test void 원본_Date_생성자는_0에서_99년을_1900년대로_읽는다() {
        assertEquals(at(1950, 3, 1, 0, 0), date("3월 1일", at(50, 6, 1, 10, 0), "UTC"));
        assertEquals(at(1950, 7, 1, 0, 0), date("다음달", at(50, 6, 1, 10, 0), "UTC"));
        assertEquals(at(1950, 1, 1, 0, 0), date("0050-01-01", at(2026, 10, 30, 14, 0), "UTC"));
    }
}
