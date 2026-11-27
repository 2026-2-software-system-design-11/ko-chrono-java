package kochrono;

import static kochrono.ResolveFixtures.at;
import static kochrono.ResolveFixtures.resolve;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class PeriodsTest {
    @Test void 기간의_끝() {
        LocalResult r = resolve("오늘부터 3일간", RelativeDayParser::parse, DurationParser::parse);
        assertEquals(List.of(at(2026, 10, 30), at(2026, 11, 1, 23, 59)), List.of(r.date, r.end));
        assertEquals(at(2026, 11, 30, 23, 59), resolve("11월 1일 한 달간", AbsoluteDateParser::parse, DurationParser::parse).end);
        assertNull(resolve("5일간", DurationParser::parse));
    }
}
