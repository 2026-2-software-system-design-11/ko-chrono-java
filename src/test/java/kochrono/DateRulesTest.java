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
        // 매주 반복의 날짜도 생성자로 만든다 (1950-06-05는 월요일)
        assertEquals(at(1950, 6, 5, 0, 0), date("매주 월요일", at(50, 6, 1, 10, 0), "UTC"));
    }

    @Test void timezone_옵션이_있으면_기준_시각부터_1900년대가_된다() {
        ParseOptions o = ParseOptions.defaults().withReferenceDate(at(50, 6, 1, 10, 0).atZone(ZoneId.of("UTC")))
                .withTimezone("UTC");
        assertEquals(at(1951, 3, 1, 0, 0), KoChrono.parse("3월 1일", o).date().toLocalDateTime());
    }

    @Test void 서머타임으로_없는_시각은_그_자리에서_밀린_값으로_지났는지_본다() {
        // 로스앤젤레스 2027-03-14 02:00에 서머타임이 시작된다. 02:30은 03:30이 되고, 기준 03:00보다 뒤라 오늘이다
        assertEquals(at(2027, 3, 14, 3, 30), date("새벽 2시 30분", at(2027, 3, 14, 3, 0), "America/Los_Angeles"));
        assertEquals(at(2027, 3, 14, 3, 30), date("내일 새벽 2시 30분", at(2027, 3, 13, 12, 0), "America/Los_Angeles"));
    }
}
