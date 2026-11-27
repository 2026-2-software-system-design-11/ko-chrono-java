package kochrono;

import static kochrono.ParserFixtures.one;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class RecurrenceParserTest {
    private static String rule(String s) {
        return Rrule.of(one(RecurrenceParser.parse(s)).components().recur, false);
    }

    @Test void 요일_반복() {
        assertEquals("FREQ=WEEKLY;BYDAY=MO,WE,FR", rule("매주 월수금"));
        assertEquals("FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR", rule("매주 월~금"));
        assertEquals("FREQ=WEEKLY;INTERVAL=2;BYDAY=FR", rule("격주 금요일"));
        assertEquals("FREQ=WEEKLY;BYDAY=SA,SU", rule("주말마다"));
        assertEquals("FREQ=WEEKLY;BYDAY=TU,TH", rule("화요일이랑 목요일마다"));
        assertEquals(List.of(5, 6, 0), RecurrenceParser.weekdaysOf("금~일"));
    }
}
