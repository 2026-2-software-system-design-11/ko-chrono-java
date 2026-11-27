package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class RangeMergerTest {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final ParseOptions OPTS = ParseOptions.defaults()
            .withReferenceDate(ZonedDateTime.of(2026, 10, 30, 14, 0, 0, 0, SEOUL));

    private static List<ZonedDateTime> range(String s) {
        ParseResult r = KoChrono.parse(s, OPTS);
        return List.of(r.date(), r.end());
    }

    private static ZonedDateTime at(int m, int d, int h, int mi) {
        return ZonedDateTime.of(2026, m, d, h, mi, 0, 0, SEOUL);
    }

    @Test void 시_범위() {
        assertEquals(List.of(at(10, 30, 15, 0), at(10, 30, 17, 0)), range("3시부터 5시까지"));
        assertEquals(List.of(at(10, 30, 23, 0), at(10, 31, 1, 0)), range("오후 11시부터 1시까지"));
        assertEquals(List.of(at(10, 30, 15, 0), at(10, 30, 17, 0)), range("오후 3~5시"));
    }

    @Test void 날짜_범위() {
        assertEquals(List.of(at(11, 1, 0, 0), at(11, 3, 23, 59)), range("11월 1일부터 11월 3일까지"));
    }

    @Test void 끝이_시작보다_앞이면_별개_표현() {
        assertEquals(2, KoChrono.parseAll("추석 ~ 11월", OPTS).size());
    }

    @Test void 끝에_일이나_요일만_있는_범위() {
        assertEquals(List.of(at(11, 1, 0, 0), at(11, 3, 23, 59)), range("11월 1일부터 3일까지"));
        assertEquals(List.of(at(11, 28, 0, 0), at(12, 2, 23, 59)), range("11월 28일부터 2일까지"));
        assertEquals(List.of(at(11, 2, 0, 0), at(11, 6, 23, 59)), range("월~금"));
    }
}
