package kochrono;

import static kochrono.ResolveFixtures.at;
import static kochrono.ResolveFixtures.resolve;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RecurrenceResolverTest {
    private static LocalResult rec(String s) {
        return resolve(s, RecurrenceParser::parse, TimeParser::parse, DurationParser::parse);
    }

    @Test void 첫_발생() {
        assertEquals(at(2026, 10, 31, 8), rec("매일 아침 8시").date);
        assertEquals(at(2026, 11, 2, 9), rec("매주 월요일 오전 9시").date);
        assertEquals(at(2026, 10, 30), rec("매주").date);
    }

    @Test void 규칙과_기간() {
        assertEquals("FREQ=WEEKLY;BYDAY=MO", rec("매주 월요일").rrule);
    }
}
