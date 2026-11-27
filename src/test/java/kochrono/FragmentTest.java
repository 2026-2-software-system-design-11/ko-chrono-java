package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class FragmentTest {
    @Test void 정규식이_맞은_부분이_조각이_된다() {
        Matcher m = Pattern.compile("내일").matcher("그럼 내일 봐");
        m.find();
        Components c = new Components();
        c.dayOffset = 1L;
        Fragment f = Fragment.of(m, c);
        assertEquals("내일", f.text());
        assertEquals(3, f.index());
        assertEquals(1L, f.components().dayOffset);
    }

    @Test void 모르는_정보는_null() {
        assertNull(new Components().dayOffset);
    }
}
