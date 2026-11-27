package kochrono;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class LocalResultTest {
    @Test void 원본_Date의_범위를_넘는_시각이_있으면_결과가_아니다() {
        LocalResult r = new LocalResult();
        r.date = LocalDateTime.of(2026, 10, 30, 14, 0);
        assertTrue(r.valid());
        r.end = LocalDateTime.of(300000, 1, 1, 0, 0);
        assertFalse(r.valid());
    }
}
