package kochrono;

import static kochrono.ResolveFixtures.REF;
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

    /** "까지"는 KoChrono가 붙이는 표시라 여기서 붙인다 */
    private static LocalResult deadline(String text, Parser parser) {
        List<Fragment> f = parser.parse(text);
        f.get(f.size() - 1).components().isEnd = true;
        return Resolver.resolve(f, ResolveFixtures.OPTS);
    }

    @Test void 마감은_기준_시각부터_그날_끝까지() {
        LocalResult r = deadline("이번달 말", RelativeMonthParser::parse);
        assertEquals(List.of(REF, at(2026, 10, 31, 23, 59)), List.of(r.date, r.end));
        assertEquals(at(2026, 11, 1, 23, 59), deadline("이번주", WeekdayParser::parse).end);
        LocalResult passed = deadline("어제", RelativeDayParser::parse);
        assertEquals(List.of(at(2026, 10, 29, 23, 59), at(2026, 10, 29, 23, 59)), List.of(passed.date, passed.end));
    }
}
