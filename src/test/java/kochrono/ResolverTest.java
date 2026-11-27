package kochrono;

import static kochrono.ResolveFixtures.REF;
import static kochrono.ResolveFixtures.at;
import static kochrono.ResolveFixtures.resolve;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ResolverTest {
    @Test void 상대_일과_시간_이동() {
        assertEquals(at(2026, 10, 31, 15), resolve("내일 오후 3시", RelativeDayParser::parse, TimeParser::parse).date);
        assertEquals(at(2026, 10, 30, 16, 0), resolve("2시간 뒤", OffsetParser::parse).date);
        assertEquals(at(2026, 11, 1), resolve("내일 자정", RelativeDayParser::parse, TimeParser::parse).date);
    }

    @Test void 시간_없는_날짜는_기본_시() {
        LocalResult r = resolve("모레", RelativeDayParser::parse);
        assertEquals(at(2026, 11, 1), r.date);
        assertFalse(r.hasTime);
        assertEquals(REF.toLocalDate(), resolve("오늘", RelativeDayParser::parse).date.toLocalDate());
    }

    @Test void 절대_날짜와_지난_날짜() {
        assertEquals(at(2027, 3, 1), resolve("3월 1일", AbsoluteDateParser::parse).date);
        assertEquals(at(2026, 11, 5), resolve("5일", AbsoluteDateParser::parse).date);
        assertNull(resolve("11월 31일", AbsoluteDateParser::parse));
        assertEquals(at(2027, 2, 28), resolve("4달 뒤", OffsetParser::parse).date);
        assertEquals(at(2026, 12, 31), resolve("다다음달 말", RelativeMonthParser::parse).date);
        assertNull(resolve("다음달 31일", RelativeMonthParser::parse, AbsoluteDateParser::parse));
    }
}
