package kochrono;

import static kochrono.ParserFixtures.one;
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
    }
}
