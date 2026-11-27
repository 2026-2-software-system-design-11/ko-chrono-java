package kochrono;

import static kochrono.Fixtures.REF;
import static kochrono.Fixtures.amb;
import static kochrono.Fixtures.at;
import static kochrono.Fixtures.d;
import static kochrono.Fixtures.utc;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 원본 tests/event.test.ts. 규칙 28: 제목은 날짜로 인식된 부분과 거기 붙은 조사를 뺀 나머지 */
@DisplayName("parseEvent: 제목 분리")
class EventTest {
    private static ParsedEvent ev(String s) {
        return KoChrono.parseEvent(s, at(REF));
    }

    @Test @DisplayName("내일 오후 3시 치과") void c1() {
        assertEquals(new ParsedEvent("치과", d(2026, 10, 31, 15), null, true, null, null, null), ev("내일 오후 3시 치과"));
    }

    @Test @DisplayName("치과 예약 내일 3시") void c2() {
        ParsedEvent e = ev("치과 예약 내일 3시");
        assertEquals("치과 예약", e.title());
        assertEquals(d(2026, 10, 31, 15), e.date());
    }
}
