package kochrono;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 반복 일정 앞뒤의 날짜와 횟수를 반복에 묶는다 (규칙 34).
 * <ul>
 *   <li>뒤의 "총 N회", "N번 반복": COUNT</li>
 * </ul>
 */
/*
 * 원본은 결과 객체를 펼쳐 복사(spread)하며 고친다. 고치는 결과는 이 뒤에 다시 쓰이지 않으므로 그 자리에서 고쳐도 같다.
 */
final class RecurrenceBinder {
    // "총 8회", "10번 반복", "총 열 번". "매일 3번"은 하루 횟수일 수 있어 "총"이나 "반복"이 있을 때만
    private static final Pattern COUNT = JsRegex.compile("^[\\s,]*(?:총\\s*(\\d{1,3}|[한두세네다섯여섯일곱여덟아홉열스무]+)\\s*(?:번|회)"
            + "(?:\\s*반복)?|(\\d{1,3}|[한두세네다섯여섯일곱여덟아홉열스무]+)\\s*(?:번|회)\\s*반복)");

    private RecurrenceBinder() {
    }

    static List<LocalResult> bind(String text, List<LocalResult> results, List<List<Fragment>> groups,
                                  List<Components> comps, ResolveOptions opts) {
        List<LocalResult> out = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            LocalResult r = results.get(i);
            if (r.recur == null) {
                out.add(r);
                continue;
            }
            Binding b = new Binding(r, groups.get(i), opts);
            int from = r.index;
            int to = r.index + r.text.length();
            boolean bound = false;

            Matcher count = COUNT.matcher(Texts.slice(text, to));
            Long n = count.find() ? KoreanNumber.parse(count.group(1) != null ? count.group(1) : count.group(2)) : null;
            if (n != null && n != 0 && b.r.recur.until() == null) {
                b.r.recur = b.r.recur.withCount(n.intValue());
                to += count.group().length();
                bound = true;
            }
            if (bound) {
                b.r.index = from;
                b.r.text = JsRegex.trim(Texts.slice(text, from, to));
                b.r.rrule = Rrule.of(b.r.recur, !b.r.hasTime);
            }
            out.add(b.r);
        }
        return out;
    }

    /** 반복 결과 하나와, 그 결과를 다른 날짜에 묶는 일 */
    private static final class Binding {
        final LocalResult r;
        final List<Fragment> group;
        final ResolveOptions opts;

        Binding(LocalResult r, List<Fragment> group, ResolveOptions opts) {
            this.r = r;
            this.group = group;
            this.opts = opts;
        }
    }
}
