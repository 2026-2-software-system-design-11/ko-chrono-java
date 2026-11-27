package kochrono;

import static kochrono.ParserFixtures.one;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class HolidayParserTest {
    @Test void 양력_기념일() {
        Components c = one(HolidayParser.parse("광복절 전날")).components();
        assertEquals(Arrays.asList(8, 15, -1L), Arrays.asList(c.month, c.day, c.dayOffset));
        assertEquals(12, one(HolidayParser.parse("크리스마스이브")).components().month);
        assertEquals(15, one(HolidayParser.parse("스승의날")).components().day);
    }
}
