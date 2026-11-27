package kochrono;

import static kochrono.ParserFixtures.one;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class RelativeDayParserTest {
    @Test void 상대_일() {
        Fragment f = one(RelativeDayParser.parse("그럼 모레 보자"));
        assertEquals("모레", f.text());
        assertEquals(3, f.index());
        assertEquals(2L, f.components().dayOffset);
        assertEquals(2L, one(RelativeDayParser.parse("내일모레")).components().dayOffset);
        assertEquals(-1L, one(RelativeDayParser.parse("작일 자료")).components().dayOffset);
    }

    @Test void 낼은_뒤에_시각이나_약속_표현이_올_때만() {
        assertEquals(1L, one(RelativeDayParser.parse("낼 봐")).components().dayOffset);
        assertTrue(RelativeDayParser.parse("내가 낼게").isEmpty());
        assertEquals(List.of(), RelativeDayParser.parse("휴일"));
    }
}
