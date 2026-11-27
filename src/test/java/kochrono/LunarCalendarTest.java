package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LunarCalendarTest {
    @Test void 음력_표() {
        assertEquals(LocalDate.of(2027, 4, 9), LunarCalendar.toSolar(2027, 3, 3, false));
        assertEquals(LocalDate.of(2028, 6, 23), LunarCalendar.toSolar(2028, 5, 1, true));
        assertNull(LunarCalendar.toSolar(2026, 2, 30, false));
        assertNull(LunarCalendar.toSolar(2030, 1, 1, false));
    }
}
