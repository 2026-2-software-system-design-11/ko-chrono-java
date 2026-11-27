package kochrono;

import static kochrono.ParserFixtures.one;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class HolidayParserTest {
    @Test void 양력_기념일() {
        Components c = one(HolidayParser.parse("광복절 전날")).components();
        assertEquals(Arrays.asList(8, 15, -1L), Arrays.asList(c.month, c.day, c.dayOffset));
        assertEquals(12, one(HolidayParser.parse("크리스마스이브")).components().month);
        assertEquals(15, one(HolidayParser.parse("스승의날")).components().day);
    }

    @Test void 음력_명절() {
        Components c = one(HolidayParser.parse("설 연휴")).components();
        assertEquals(Components.LunarHoliday.SEOLLAL, c.lunar);
        assertEquals(Arrays.asList(-1L, 3L), Arrays.asList(c.dayOffset, c.durationDays));
        assertEquals(Components.LunarHoliday.CHUSEOK, one(HolidayParser.parse("한가위 다음날")).components().lunar);
        assertTrue(HolidayParser.parse("설명해줘").isEmpty());
    }

    @Test void 음력_날짜() {
        assertEquals(new Components.LunarDate(5, 1, true), one(HolidayParser.parse("음력 윤5월 1일")).components().lunarDate);
        assertTrue(HolidayParser.parse("음력 2월 31일").isEmpty());
    }
}
