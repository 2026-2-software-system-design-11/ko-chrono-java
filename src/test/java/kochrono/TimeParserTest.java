package kochrono;

import static kochrono.ParserFixtures.one;
import static kochrono.ParserFixtures.texts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TimeParserTest {
    private static List<Integer> hm(String s) {
        Components c = one(TimeParser.parse(s)).components();
        return List.of(c.hour, c.minute);
    }

    @Test void 시와_분() {
        assertEquals(List.of(15, 0), hm("오후 3시"));
        assertEquals(List.of(3, 30), hm("3시 반"));
        assertEquals(List.of(10, 30), hm("오전 10시 30분"));
        assertEquals(List.of(0, 0), hm("밤 12시"));
        assertEquals(List.of(7, 30), hm("7시30"));
        assertFalse(one(TimeParser.parse("3시")).components().meridiem);
        assertTrue(one(TimeParser.parse("24시")).components().midnight);
        assertTrue(TimeParser.parse("24시 편의점").isEmpty());
        assertTrue(TimeParser.parse("2시간").isEmpty());
    }

    @Test void 몇_분_전과_시간대의_끝() {
        assertEquals(List.of(4, 50), hm("5시 10분 전"));
        assertEquals(List.of(-1, -10), hm("0시 10분 전"));
        Components c = one(TimeParser.parse("오전 중으로")).components();
        assertEquals(12, c.hour);
        assertTrue(c.isEnd);
    }

    @Test void 짧은_시_범위는_두_조각() {
        assertEquals(List.of("오후 3", "5시"), texts(TimeParser.parse("오후 3~5시")));
    }
}
