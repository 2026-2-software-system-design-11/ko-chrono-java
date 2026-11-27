package kochrono;

import static kochrono.Fixtures.utc;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 원본 tests/ics.test.ts. 실행 환경 시간대와 상관없이 같은 결과가 나오도록 서울 시간대로 고정한다 */
@DisplayName("toICS")
class IcsTest {
    private static final ZonedDateTime REF = utc(2026, 10, 30, 5); // 2026-10-30 (금) 14:00 서울
    private static final ParseOptions OPTS = ParseOptions.defaults().withReferenceDate(REF).withTimezone("Asia/Seoul");
    private static final IcsOptions FIXED = IcsOptions.defaults().withUid("test@ko-chrono").withNow(REF.toInstant())
            .withTimezone("Asia/Seoul");

    private static List<String> lines(String ics) {
        return Arrays.asList(ics.split("\r\n", -1));
    }

    private static List<String> event(String ics) {
        List<String> ls = lines(ics);
        return ls.subList(ls.indexOf("BEGIN:VEVENT") + 1, ls.indexOf("END:VEVENT"));
    }

    private static ParsedEvent ev(String s) {
        return KoChrono.parseEvent(s, OPTS);
    }

    private static ParseResult p(String s) {
        return KoChrono.parse(s, OPTS);
    }
}
