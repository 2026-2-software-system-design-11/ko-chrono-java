package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class KoChronoTest {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final ParseOptions OPTS = ParseOptions.defaults().withReferenceDate(at(2026, 10, 30, 14, 0));

    private static ZonedDateTime at(int y, int m, int d, int h, int mi) {
        return ZonedDateTime.of(y, m, d, h, mi, 0, 0, SEOUL);
    }

    @Test void 문장_속_표현() {
        ParseResult r = KoChrono.parse("그럼 다음주 수요일 저녁 7시에 강남에서 볼까?", OPTS);
        assertEquals(List.of(at(2026, 11, 4, 19, 0), "다음주 수요일 저녁 7시", 3), List.of(r.date(), r.text(), r.index()));
        assertNull(KoChrono.parse("안녕하세요", OPTS));
        assertEquals(at(2026, 10, 31, 0, 0), KoChrono.parseDate("내일", OPTS));
        assertEquals(2, KoChrono.parseAll("내일 10시 아니면 모레 2시", OPTS).size());
    }

    @Test void 시간대를_주면_그_시간대의_벽시계로_읽는다() {
        ParseResult r = KoChrono.parse("오후 3시", OPTS.withTimezone("UTC"));
        assertEquals(at(2026, 10, 31, 0, 0).toInstant(), r.date().toInstant());
    }

    @Test void 옵션_없이_부르면_지금이_기준() {
        assertEquals("내일", KoChrono.parse("내일").text());
        assertEquals(2, KoChrono.parseAll("내일 아니면 모레").size());
        assertNull(KoChrono.parseDate("안녕하세요"));
    }
}
