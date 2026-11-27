package kochrono;

import static kochrono.ParserFixtures.one;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RecurrenceParserTest {
    private static String rule(String s) {
        return Rrule.of(one(RecurrenceParser.parse(s)).components().recur, false);
    }

    @Test void 요일_반복() {
        assertEquals("FREQ=WEEKLY;BYDAY=MO,WE,FR", rule("매주 월수금"));
        assertEquals("FREQ=WEEKLY;INTERVAL=2;BYDAY=FR", rule("격주 금요일"));
    }
}
