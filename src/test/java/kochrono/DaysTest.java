package kochrono;

import static kochrono.ResolveFixtures.at;
import static kochrono.ResolveFixtures.resolve;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class DaysTest {
    @Test void 주와_요일() {
        assertEquals(at(2026, 11, 3), resolve("다음주 화요일", WeekdayParser::parse).date);
        assertEquals(at(2026, 10, 30), resolve("금요일", WeekdayParser::parse).date);
        assertEquals(at(2026, 10, 23), resolve("지난 금요일", WeekdayParser::parse).date);
        assertEquals(at(2026, 11, 6), resolve("다음주 이맘때", WeekdayParser::parse).date);
        assertEquals(at(2026, 10, 31), resolve("주말", WeekdayParser::parse).date);
    }

    @Test void N째_주와_날짜_뒤_요일() {
        assertEquals(at(2026, 11, 2), resolve("11월 첫째 주 월요일", WeekdayParser::parse).date);
        assertNull(resolve("11월 다섯째 주 토요일", WeekdayParser::parse));
        assertEquals(at(2026, 11, 5), resolve("11월 5일 목요일", AbsoluteDateParser::parse, WeekdayParser::parse).date);
        assertNull(resolve("11월 5일 수요일", AbsoluteDateParser::parse, WeekdayParser::parse));
    }
}
