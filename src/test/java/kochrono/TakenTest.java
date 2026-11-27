package kochrono;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TakenTest {
    @Test void 겹치는_구간은_잡지_않는다() {
        Taken taken = new Taken();
        assertTrue(taken.take(3, 6));
        assertFalse(taken.take(5, 8));
        assertTrue(taken.overlaps(0, 4));
        assertTrue(taken.take(6, 9));
        assertFalse(taken.overlaps(9, 12));
    }
}
