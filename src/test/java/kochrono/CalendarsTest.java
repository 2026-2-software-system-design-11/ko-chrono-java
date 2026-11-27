package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class CalendarsTest {
    @Test void 요일_번호는_0이_일요일() {
        assertEquals(5, Calendars.weekdayNumber(LocalDate.of(2026, 10, 30)));
        assertEquals(0, Calendars.weekdayNumber(LocalDate.of(2026, 11, 1)));
    }

    @Test void 음수_시각은_전날로() {
        assertEquals(LocalDateTime.of(2026, 10, 29, 22, 50), Calendars.atClock(LocalDate.of(2026, 10, 30), -1, -10));
        assertEquals(LocalDateTime.of(2026, 10, 31, 1, 0), Calendars.atClock(LocalDate.of(2026, 10, 30), 25, 0));
    }

    @Test void 그_달_1일부터_센_날() {
        assertEquals(LocalDate.of(2026, 3, 2), Calendars.dayCountedFrom(YearMonth.of(2026, 2), 30));
        assertEquals(LocalDate.of(2026, 10, 31), Calendars.dayCountedFrom(YearMonth.of(2026, 11), 0));
        assertEquals(LocalDate.of(2027, 2, 28), Calendars.sameDayIn(YearMonth.of(2027, 2), 30));
    }

    @Test void N번째_요일() {
        assertEquals(2, Calendars.nthWeekday(YearMonth.of(2026, 11), 1, 1));
        assertEquals(28, Calendars.nthWeekday(YearMonth.of(2026, 11), 6, -1));
        assertNull(Calendars.nthWeekday(YearMonth.of(2026, 11), 6, 5));
    }

    @Test void 원본_Date의_범위() {
        assertTrue(Calendars.representable(LocalDateTime.of(275000, 1, 1, 0, 0)));
        assertFalse(Calendars.representable(LocalDateTime.of(300000, 1, 1, 0, 0)));
    }
}
