package kochrono;

import static kochrono.ParserFixtures.one;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RelativeMonthParserTest {
    @Test void 이번달_다음달과_초_중순_말() {
        Components c = one(RelativeMonthParser.parse("다음 달 중순")).components();
        assertEquals(1L, c.monthOffset);
        assertEquals(Components.MonthPart.MIDDLE, c.monthPart);
        assertTrue(one(RelativeMonthParser.parse("다음달 이맘때")).components().sameDay);
        assertEquals(Components.MonthPart.END, one(RelativeMonthParser.parse("월말")).components().monthPart);
    }

    @Test void 숫자_월과_초_중순_말() {
        Components c = one(RelativeMonthParser.parse("12월 말")).components();
        assertEquals(12, c.month);
        assertEquals(Components.MonthPart.END, c.monthPart);
    }
}
