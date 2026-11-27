package kochrono;

import static kochrono.ParserFixtures.one;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OffsetParserTest {
    @Test void 일_주_달_년_단위() {
        assertEquals(3L, one(OffsetParser.parse("3일 뒤")).components().dayOffset);
        assertEquals(-14L, one(OffsetParser.parse("2주 전")).components().dayOffset);
        Components month = one(OffsetParser.parse("한 달 뒤")).components();
        assertEquals(1L, month.monthOffset);
        assertTrue(month.sameDay);
        assertEquals(1, one(OffsetParser.parse("1년 뒤")).components().yearOffset);
        assertTrue(OffsetParser.parse("2028년 후").isEmpty());
    }

    @Test void 시와_분_단위와_마감() {
        assertEquals(3L, one(OffsetParser.parse("세 시간 뒤")).components().hourOffset);
        Components c = one(OffsetParser.parse("30분 내로")).components();
        assertEquals(30L, c.minuteOffset);
        assertTrue(c.isEnd);
        assertEquals(30L, one(OffsetParser.parse("30분쯤 뒤")).components().minuteOffset);
    }

    @Test void 시간과_분을_함께() {
        Components c = one(OffsetParser.parse("한 시간 반 뒤")).components();
        assertEquals(1L, c.hourOffset);
        assertEquals(30L, c.minuteOffset);
        Components before = one(OffsetParser.parse("2시간 10분 전")).components();
        assertEquals(-2L, before.hourOffset);
        assertEquals(-10L, before.minuteOffset);
    }
}
