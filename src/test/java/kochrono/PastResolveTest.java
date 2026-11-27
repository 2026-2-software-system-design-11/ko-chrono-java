package kochrono;

import static kochrono.ResolveFixtures.OPTS;
import static kochrono.ResolveFixtures.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PastResolveTest {
    /** 과거형 표시는 KoChrono가 붙이므로 여기서 붙인다 */
    private static LocalResult past(String text, Parser... parsers) {
        List<Fragment> f = new ArrayList<>();
        for (Parser p : parsers) {
            f.addAll(p.parse(text));
        }
        f.forEach(x -> x.components().past = true);
        return Resolver.resolve(Grouping.group(f, text).get(0), OPTS);
    }

    @Test void 아직_안_온_시각은_어제() {
        assertEquals(at(2026, 10, 29, 15), past("3시", TimeParser::parse).date);
        assertEquals(at(2026, 10, 30, 9), past("9시", TimeParser::parse).date);
    }

    @Test void 아직_안_온_날은_한_주기_앞() {
        assertEquals(at(2025, 12, 1), past("12월 1일", AbsoluteDateParser::parse).date);
        assertEquals(at(2026, 10, 5), past("5일", AbsoluteDateParser::parse).date);
        assertEquals(at(2026, 10, 26), past("월요일", WeekdayParser::parse).date);
        assertEquals(at(2026, 10, 23, 17), past("금요일 5시", WeekdayParser::parse, TimeParser::parse).date);
        assertNull(past("31일", AbsoluteDateParser::parse));
    }
}
