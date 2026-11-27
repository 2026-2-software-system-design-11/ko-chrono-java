package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class RruleTest {
    @Test void 주기와_간격과_요일() {
        assertEquals("FREQ=DAILY", Rrule.of(RecurSpec.of(Frequency.DAILY, 1), false));
        assertEquals("FREQ=WEEKLY;INTERVAL=2;BYDAY=MO,WE,SU",
                Rrule.of(RecurSpec.of(Frequency.WEEKLY, 2).withWeekdays(List.of(0, 3, 1)), false));
        assertEquals("FREQ=MONTHLY;BYDAY=-1FR",
                Rrule.of(RecurSpec.of(Frequency.MONTHLY, 1).withWeekdays(List.of(5)).withNth(-1), false));
        assertEquals("FREQ=YEARLY;BYMONTH=3;BYMONTHDAY=1",
                Rrule.of(RecurSpec.of(Frequency.YEARLY, 1).withMonth(3).withMonthDay(1), false));
    }

    @Test void 끝과_횟수() {
        RecurSpec until = RecurSpec.of(Frequency.DAILY, 1).withUntil(LocalDateTime.of(2026, 11, 30, 23, 59));
        assertEquals("FREQ=DAILY;UNTIL=20261130T235900", Rrule.of(until, false));
        assertEquals("FREQ=DAILY;UNTIL=20261130", Rrule.of(until, true));
        assertEquals("FREQ=DAILY;COUNT=8", Rrule.of(RecurSpec.of(Frequency.DAILY, 1).withCount(8), false));
    }
}
