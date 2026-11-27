package kochrono;

import static kochrono.ParserFixtures.one;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class WeekdayParserTest {
    private static List<Integer> week(String s) {
        Components c = one(WeekdayParser.parse(s)).components();
        return Arrays.asList(c.weekOffset, c.weekday);
    }

    @Test void 주와_요일() {
        assertEquals(Arrays.asList(1, 2), week("다음주 화요일"));
        assertEquals(Arrays.asList(1, 1), week("담주 월 2시"));
        assertEquals(Arrays.asList(2, 1), week("2주 뒤 월요일"));
        assertEquals(Arrays.asList(null, 5), week("금욜"));
        assertEquals(Arrays.asList(1, null), week("다음주 일정"));
        assertTrue(one(WeekdayParser.parse("다음주 이맘때")).components().sameDay);
    }

    @Test void 주말과_지난_다음_요일() {
        Components c = one(WeekdayParser.parse("이번 주말")).components();
        assertEquals(Arrays.asList(0, 6), Arrays.asList(c.weekOffset, c.weekday));
        assertTrue(c.weekend);
        assertNull(one(WeekdayParser.parse("주말에")).components().weekOffset);
        assertEquals(Components.WeekdayDir.PAST, one(WeekdayParser.parse("저번 수욜")).components().weekdayDir);
    }
}
