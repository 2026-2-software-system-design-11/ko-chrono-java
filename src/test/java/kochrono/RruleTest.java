package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class RruleTest {
    @Test void 주기와_간격과_요일() {
        assertEquals("FREQ=DAILY", Rrule.of(RecurSpec.of(Frequency.DAILY, 1), false));
        assertEquals("FREQ=WEEKLY;INTERVAL=2;BYDAY=MO,WE,SU",
                Rrule.of(RecurSpec.of(Frequency.WEEKLY, 2).withWeekdays(List.of(0, 3, 1)), false));
        assertEquals("FREQ=MONTHLY;BYDAY=-1FR",
                Rrule.of(RecurSpec.of(Frequency.MONTHLY, 1).withWeekdays(List.of(5)).withNth(-1), false));
    }
}
