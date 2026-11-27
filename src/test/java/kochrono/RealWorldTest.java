package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;
import kochrono.MessengerCases.Case;
import org.junit.jupiter.api.*;

/** 원본 tests/real-world.test.ts: 예문 하나가 시험 하나. 'YYYY-MM-DDTHH:mm'은 로컬 시각 */
class RealWorldTest {
    private static ZonedDateTime local(String s) {
        return LocalDateTime.parse(s).atZone(Fixtures.ZONE);
    }

    private static Stream<DynamicTest> run(List<Case> list) {
        ZonedDateTime ref = local(MessengerCases.REF);
        return list.stream().map(c -> DynamicTest.dynamicTest(c.text(), () -> {
            ParseResult r = Fixtures.parse(c.text(), ref);
            if (c.date() == null) {
                assertNull(r);
                return;
            }
            assertEquals(local(c.date()), r == null ? null : r.date());
            if (c.end() != null) {
                assertEquals(local(c.end()), r.end());
            }
        }));
    }

    @TestFactory @DisplayName("메신저 문장") Stream<DynamicTest> messenger() {
        return run(MessengerCases.CASES);
    }
}
