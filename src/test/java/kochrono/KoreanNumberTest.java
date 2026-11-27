package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class KoreanNumberTest {
    @Test void 숫자열() {
        assertEquals(12L, KoreanNumber.parse("12"));
        assertEquals(7L, KoreanNumber.parse("007"));
        assertEquals(KoreanNumber.CAP, KoreanNumber.parse("99999999999999999999"));
    }

    @Test void 고유어() {
        assertEquals(3L, KoreanNumber.parse("세"));
        assertEquals(12L, KoreanNumber.parse("열두"));
        assertEquals(25L, KoreanNumber.parse("스물다섯"));
        assertEquals(15L, KoreanNumber.parse("보름"));
    }

    @Test void 읽지_못하면_null() {
        assertNull(KoreanNumber.parse("열셋셋"));
        assertNull(KoreanNumber.parse("abc"));
    }
}
