package kochrono;

import static kochrono.ParserFixtures.one;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RelativeYearParserTest {
    @Test void 상대_연도() {
        assertEquals(-2, one(RelativeYearParser.parse("재작년")).components().yearOffset);
        Components c = one(RelativeYearParser.parse("내년 초")).components();
        assertEquals(1, c.yearOffset);
        assertEquals(1, c.month);
        assertEquals(Components.MonthPart.START, c.monthPart);
        assertTrue(one(RelativeYearParser.parse("내년 이맘때")).components().sameDay);
    }

    @Test void 연말과_연초는_연도가_없다() {
        Components c = one(RelativeYearParser.parse("연말")).components();
        assertNull(c.yearOffset);
        assertEquals(12, c.month);
        assertTrue(RelativeYearParser.parse("연말정산").isEmpty());
    }
}
