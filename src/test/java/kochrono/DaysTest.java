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

    @Test void 음력() {
        assertEquals(at(2027, 2, 7), resolve("설날", HolidayParser::parse).date);
        assertEquals(at(2026, 9, 25), resolve("올해 추석", RelativeYearParser::parse, HolidayParser::parse).date);
        assertEquals(at(2027, 4, 9), resolve("음력 3월 3일", HolidayParser::parse).date);
        assertNull(resolve("2030년 설날", AbsoluteDateParser::parse, HolidayParser::parse));
    }
}
