package kochrono;

import java.util.ArrayList;
import java.util.List;

/** 한 파서 안에서 이미 잡은 구간. 먼저 잡은 패턴이 이긴다 */
final class Taken {
    private final List<int[]> spans = new ArrayList<>();

    /** [start, end)가 이미 잡은 구간과 겹치는지 */
    boolean overlaps(int start, int end) {
        for (int[] s : spans) {
            if (start < s[1] && end > s[0]) {
                return true;
            }
        }
        return false;
    }

    void add(int start, int end) {
        spans.add(new int[] {start, end});
    }

    /** 겹치지 않으면 잡고 true, 겹치면 false */
    boolean take(int start, int end) {
        if (overlaps(start, end)) {
            return false;
        }
        add(start, end);
        return true;
    }
}
