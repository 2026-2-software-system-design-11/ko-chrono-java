package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class LunarCalendarTest {
    @Test void 음력_표() {
        assertEquals(LocalDate.of(2027, 4, 9), LunarCalendar.toSolar(2027, 3, 3, false));
        assertEquals(LocalDate.of(2028, 6, 23), LunarCalendar.toSolar(2028, 5, 1, true));
        assertNull(LunarCalendar.toSolar(2026, 2, 30, false));
        assertNull(LunarCalendar.toSolar(2030, 1, 1, false));
        assertEquals(List.of(2025, 2026, 2027, 2028), LunarCalendar.YEARS);
    }

    @Test void 표의_마지막_달은_29일까지() {
        assertEquals(LocalDate.of(2029, 2, 12), LunarCalendar.toSolar(2028, 12, 29, false));
        assertNull(LunarCalendar.toSolar(2028, 12, 30, false));
    }
}
