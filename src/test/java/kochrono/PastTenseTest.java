package kochrono;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PastTenseTest {
    @Test void 과거형_어미() {
        assertTrue(PastTense.isPastClause("에 회의했어"));
        assertTrue(PastTense.isPastClause("에 도착했습니다"));
        assertTrue(PastTense.isPastClause("에 만났어?"));
        assertTrue(PastTense.isPastClause("에 혼자 갔어"));
    }

    @Test void 과거형이_아닌_것() {
        assertFalse(PastTense.isPastClause("에 보자"));
        assertFalse(PastTense.isPastClause("에 회의 있어"));
        assertFalse(PastTense.isPastClause("로 예약했어"));
        assertFalse(PastTense.isPastClause("에 만나기로 했어"));
        assertFalse(PastTense.isPastClause("에 봐 저번엔 늦었잖아"));
        assertFalse(PastTense.isPastClause("에 만나자. 어제는 미안했어"));
    }
}
