package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class RecurrenceBinderTest {
    private static final ParseOptions OPTS = ParseOptions.defaults()
            .withReferenceDate(ZonedDateTime.of(2026, 10, 30, 14, 0, 0, 0, ZoneId.of("Asia/Seoul")));

    private static String rrule(String s) {
        return KoChrono.parse(s, OPTS).recurrence().rrule();
    }

    @Test void 시작과_횟수() {
        assertEquals("FREQ=WEEKLY;BYDAY=WE;COUNT=8", rrule("매주 수요일 오후 2시, 총 8회"));
        assertEquals("FREQ=DAILY", rrule("매일 3번 약 먹기"));
    }

    @Test void 시각만_있는_마감과_지난_마감은_묶지_않는다() {
        assertEquals(2, KoChrono.parseAll("5시까지 매일 보고", OPTS).size());
        assertEquals(List.of(false, true),
                KoChrono.parseAll("어제까지 매주 월요일", OPTS).stream().map(r -> r.recurrence() != null).toList());
    }
}
