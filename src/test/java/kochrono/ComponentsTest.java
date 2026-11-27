package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ComponentsTest {
    @Test void 뒤_조각의_값이_앞의_값을_덮는다() {
        Components a = new Components();
        a.dayOffset = 1L;
        a.hour = 9;
        Components b = new Components();
        b.hour = 15;
        b.meridiem = true;
        Components c = Components.merge(List.of(a, b));
        assertEquals(1L, c.dayOffset);
        assertEquals(15, c.hour);
        assertTrue(c.meridiem);
    }
}
