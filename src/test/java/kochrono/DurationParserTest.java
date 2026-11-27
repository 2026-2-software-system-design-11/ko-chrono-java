package kochrono;

import static kochrono.ParserFixtures.one;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DurationParserTest {
    @Test void 기간의_길이만_뽑는다() {
        assertEquals(3L, one(DurationParser.parse("3일간")).components().durationDays);
        assertEquals(14L, one(DurationParser.parse("2주 동안")).components().durationDays);
        assertEquals(1L, one(DurationParser.parse("한 달간")).components().durationMonths);
        assertEquals(90L, one(DurationParser.parse("90분간")).components().durationMinutes);
        assertEquals(120L, one(DurationParser.parse("2시간 동안")).components().durationMinutes);
        assertEquals(2L, one(DurationParser.parse("이틀간")).components().durationDays);
    }

    @Test void 몇_박_며칠은_며칠() {
        assertEquals(4L, DurationParser.parse("3박 4일").get(0).components().durationDays);
    }

    @Test void 영일간은_기간이_아니다() {
        assertTrue(DurationParser.parse("0일간").isEmpty());
        assertTrue(DurationParser.parse("3박 0일").isEmpty());
    }
}
