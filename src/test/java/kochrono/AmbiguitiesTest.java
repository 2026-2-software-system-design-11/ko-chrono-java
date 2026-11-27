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
}
