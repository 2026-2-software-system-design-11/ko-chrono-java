package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class GroupingTest {
    private static List<Integer> sizes(String text) {
        List<Fragment> f = new ArrayList<>(RelativeDayParser.parse(text));
        f.addAll(TimeParser.parse(text));
        f.addAll(OffsetParser.parse(text));
        return Grouping.group(f, text).stream().map(List::size).toList();
    }

    @Test void 조사와_공백만_사이에_있으면_묶는다() {
        assertEquals(List.of(2), sizes("내일은 오후 3시"));
        assertEquals(List.of(1, 1), sizes("내일이나 오후 3시"));
        assertEquals(List.of(2, 2), sizes("내일 10시 아니면 모레 2시"));
    }

    @Test void 겹치는_정보는_다른_표현() {
        Components day = new Components();
        day.day = 5;
        Components weekday = new Components();
        weekday.weekday = 4;
        assertFalse(Grouping.hasConflict(day, weekday));
        assertTrue(Grouping.hasConflict(day, day));
    }
}
