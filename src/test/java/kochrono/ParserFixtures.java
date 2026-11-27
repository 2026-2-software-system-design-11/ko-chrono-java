package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

/** 파서 단위 시험의 도우미 */
final class ParserFixtures {
    private ParserFixtures() {
    }

    /** 조각 하나만 나왔는지 보고 그 조각을 돌려준다 */
    static Fragment one(List<Fragment> fragments) {
        assertEquals(1, fragments.size(), fragments::toString);
        return fragments.get(0);
    }
}
