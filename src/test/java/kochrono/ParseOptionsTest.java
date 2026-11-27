package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.DateTimeException;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class ParseOptionsTest {
    @Test void 기본값은_비어_있다() {
        ParseOptions o = ParseOptions.defaults();
        assertNull(o.referenceDate());
        assertNull(o.weekStart());
        assertNull(o.defaultHour());
        assertNull(o.timezone());
    }

    @Test void with로_바꾼다() {
        ParseOptions o = ParseOptions.defaults().withWeekStart(WeekStart.SUNDAY).withDefaultHour(9).withTimezone("Asia/Seoul");
        assertEquals(WeekStart.SUNDAY, o.weekStart());
        assertEquals(9, o.defaultHour());
        assertEquals(ZoneId.of("Asia/Seoul"), o.timezone());
    }

    @Test void 없는_시간대는_예외() {
        assertThrows(DateTimeException.class, () -> ParseOptions.defaults().withTimezone("Mars/Olympus"));
    }
}
