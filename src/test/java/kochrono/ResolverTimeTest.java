package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ResolverTimeTest {
    private static final ResolveOptions OPTS = new ResolveOptions(LocalDateTime.of(2026, 10, 30, 14, 0), WeekStart.MONDAY, 0);

    private static LocalDateTime resolve(String text) {
        return Resolver.resolve(TimeParser.parse(text), OPTS).date;
    }

    @Test void 날짜_없는_시각은_오늘_지났으면_내일() {
        assertEquals(LocalDateTime.of(2026, 10, 30, 15, 0), resolve("3시"));
        assertEquals(LocalDateTime.of(2026, 10, 31, 7, 0), resolve("7시"));
        assertEquals(LocalDateTime.of(2026, 10, 31, 10, 30), resolve("오전 10시 30분"));
    }
}
