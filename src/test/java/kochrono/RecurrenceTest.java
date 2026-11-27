package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class RecurrenceTest {
    @Test void 요일_목록은_복사해_둔다() {
        List<DayOfWeek> days = new ArrayList<>(List.of(DayOfWeek.MONDAY));
        Recurrence r = new Recurrence(Frequency.WEEKLY, 1, days, null, null, null, null, null, "FREQ=WEEKLY;BYDAY=MO");
        days.add(DayOfWeek.FRIDAY);
        assertEquals(List.of(DayOfWeek.MONDAY), r.byWeekday());
        assertThrows(UnsupportedOperationException.class, () -> r.byWeekday().add(DayOfWeek.SUNDAY));
    }
}
