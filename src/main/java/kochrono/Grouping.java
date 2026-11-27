package kochrono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/** 인접한 조각을 하나의 표현으로 묶고, 묶인 정보의 종류를 판단한다 */
final class Grouping {
    // 조각 사이에 와도 같은 표현으로 묶는 것: "내일 오후", "내일은 오후", "토요일쯤 저녁", "모레도 2시".
    // "이나", "아니면", "랑"은 다른 선택지일 수 있어 묶지 않는다
    private static final Pattern JOINER = JsRegex.compile("^(?:\\s|,|에|의|엔|은|는|도|쯤|경|께|부터)*$");

    private Grouping() {
    }

    /**
     * 인접한 조각을 하나로 합친다. 예: "다음주 화요일"과 "오후 3시"는 하나의 결과.
     * 사이에 공백, 쉼표, 조사(에, 의, 엔, 은, 는, 도), 어림말(쯤, 경, 께)만 있고 정보가 겹치지 않을 때 묶는다.
     */
    static List<List<Fragment>> group(List<Fragment> fragments, String text) {
        List<Fragment> sorted = new ArrayList<>(fragments);
        sorted.sort(Comparator.comparingInt(Fragment::index));
        List<List<Fragment>> groups = new ArrayList<>();
        for (Fragment f : sorted) {
            if (!groups.isEmpty()) {
                List<Fragment> g = groups.get(groups.size() - 1);
                Fragment last = g.get(g.size() - 1);
                String between = Texts.slice(text, last.index() + last.text().length(), f.index());
                boolean conflict = hasConflict(merge(g), f.components());
                if (JOINER.matcher(between).find() && !conflict) {
                    g.add(f);
                    continue;
                }
            }
            List<Fragment> g = new ArrayList<>();
            g.add(f);
            groups.add(g);
        }
        return groups;
    }

    static Components merge(List<Fragment> group) {
        return Components.merge(group.stream().map(Fragment::components).toList());
    }

    private static boolean hasDayKey(Components c) {
        return c.day != null || c.dayOffset != null || c.weekday != null || c.monthPart != null;
    }

    /** "11월 5일 목요일": 날짜 뒤의 요일은 덧붙이는 말이라 충돌이 아니다 */
    private static boolean weekdayNote(Components x, Components y) {
        return x.day != null && y.weekday != null && !y.weekend && y.weekOffset == null && y.day == null
                && y.dayOffset == null && y.monthPart == null;
    }

    /** 같은 종류의 정보가 두 번 나오면 다른 날짜 표현으로 본다 (예: "3일 5일") */
    static boolean hasConflict(Components a, Components b) {
        boolean dayConflict = hasDayKey(a) && hasDayKey(b) && !weekdayNote(a, b) && !weekdayNote(b, a);
        boolean bothHour = a.hour != null && b.hour != null;
        return dayConflict || bothHour;
    }
}
