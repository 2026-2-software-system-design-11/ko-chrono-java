package kochrono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/** "A부터 B까지", "A~B"를 하나의 구간으로 합친다 */
final class RangeMerger {
    record Merged(List<LocalResult> results, List<List<Fragment>> groups, List<Components> comps) {
    }

    private RangeMerger() {
    }

    static Merged merge(String text, List<LocalResult> results, List<List<Fragment>> groups, List<Components> comps) {
        List<LocalResult> merged = new ArrayList<>();
        List<List<Fragment>> mergedGroups = new ArrayList<>();
        List<Components> mergedComps = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            mergedGroups.add(groups.get(i));
            mergedComps.add(comps.get(i));
            LocalResult a = results.get(i);
            LocalResult b = i + 1 < results.size() ? results.get(i + 1) : null;
            Matcher from = Pipeline.FROM.matcher(Texts.slice(text, a.index + a.text.length()));
            boolean hasFrom = from.find();
            // "~"는 바로 뒤에 B가 붙어 있을 때만. "부터"는 B가 "까지"로 끝나거나,
            // 바로 뒤에 붙은 B가 시각이나 일뿐일 때 ("2시부터 4시", "1일부터 3일")
            boolean adjacent = b != null && hasFrom && b.index == a.index + a.text.length() + from.group().length();
            boolean tilde = adjacent && from.group(1).equals("~");
            boolean bareFrom = adjacent && from.group(1).equals("부터")
                    && (Grouping.isTimeOnly(comps.get(i + 1)) || Grouping.isDayOnly(comps.get(i + 1)));
            if (b == null || !hasFrom || !(b.end != null || tilde || bareFrom)) {
                merged.add(a);
                continue;
            }
            LocalDateTime end = endOf(a, b, comps.get(i + 1));
            // 원본은 a를 펼쳐 새 객체를 만든다. a는 이 뒤에 다시 쓰이지 않으므로 그 자리에서 고쳐도 같다
            a.end = end;
            a.text = JsRegex.trim(Texts.slice(text, a.index, b.index + b.text.length()));
            merged.add(a);
            i++;
        }
        return new Merged(merged, mergedGroups, mergedComps);
    }

    private static LocalDateTime endOf(LocalResult a, LocalResult b, Components bc) {
        if (Grouping.isTimeOnly(bc)) {
            return Grouping.alignEnd(a.date, bc);
        }
        LocalDateTime end = b.end != null ? b.end : b.date;
        if (b.end == null && !b.hasTime) {
            end = end.toLocalDate().atTime(23, 59);
        }
        return end;
    }
}
