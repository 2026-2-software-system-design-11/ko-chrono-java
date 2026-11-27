package kochrono;

import static kochrono.ResolveFixtures.at;
import static kochrono.ResolveFixtures.resolve;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class AmbiguitiesTest {
    @Test void 오전_오후가_없는_시각() {
        LocalResult r = resolve("3시", TimeParser::parse);
        assertEquals(Ambiguity.Reason.MERIDIEM, r.ambiguity);
        assertEquals(List.of(at(2026, 10, 31, 3)), r.alternatives);
        assertNull(resolve("오후 3시", TimeParser::parse).ambiguity);
    }

    @Test void 다음_요일과_관례_시각() {
        LocalResult r = resolve("다음 토요일", WeekdayParser::parse);
        assertEquals(Ambiguity.Reason.NEXT_WEEKDAY, r.ambiguity);
        assertEquals(List.of(at(2026, 11, 7)), r.alternatives);
        assertNull(resolve("다음 금요일", WeekdayParser::parse).ambiguity);
        assertEquals(Ambiguity.Reason.APPROXIMATE_TIME, resolve("저녁", TimeParser::parse).ambiguity);
    }
}
