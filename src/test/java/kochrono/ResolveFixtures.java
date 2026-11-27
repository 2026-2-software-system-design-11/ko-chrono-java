package kochrono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** resolve 단위 시험의 도우미. 기준: 2026-10-30 (금) 14:00 */
final class ResolveFixtures {
    static final LocalDateTime REF = LocalDateTime.of(2026, 10, 30, 14, 0);
    static final ResolveOptions OPTS = new ResolveOptions(REF, WeekStart.MONDAY, 0);

    private ResolveFixtures() {
    }

    static LocalDateTime at(int y, int m, int d, int... hm) {
        return LocalDateTime.of(y, m, d, hm.length > 0 ? hm[0] : 0, hm.length > 1 ? hm[1] : 0);
    }

    /** 주어진 파서로 조각을 찾아 묶고, 첫 묶음을 계산한다 */
    static LocalResult resolve(String text, Parser... parsers) {
        List<Fragment> fragments = new ArrayList<>();
        for (Parser p : parsers) {
            fragments.addAll(p.parse(text));
        }
        return Resolver.resolve(Grouping.group(fragments, text).get(0), OPTS);
    }
}
