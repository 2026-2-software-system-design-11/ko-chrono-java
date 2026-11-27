package kochrono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 반복 일정 앞뒤의 날짜와 횟수를 반복에 묶는다 (규칙 34).
 * <ul>
 *   <li>앞이나 뒤의 마감, 구간 ("다음달까지", "11월 1일부터 12월 31일까지"): 그 시작 이후 첫 발생, 끝은 UNTIL</li>
 *   <li>앞의 "A부터": A 이후 첫 발생</li>
 *   <li>뒤의 "총 N회", "N번 반복": COUNT</li>
 * </ul>
 * 끝이 첫 발생보다 앞이면 묶지 않는다 ("어제까지 매주 월요일").
 */
/*
 * 원본은 결과 객체를 펼쳐 복사(spread)하며 고친다. 고치는 결과는 이 뒤에 다시 쓰이지 않으므로 그 자리에서 고쳐도 같다.
 */
final class RecurrenceBinder {
    // 반복 일정과 그 앞뒤 날짜 사이: "다음달까지 매주 월요일", "매주 월요일, 12월 20일까지"
    private static final Pattern GAP = JsRegex.compile("^[\\s,]*$");
    // "다음주부터 매주 월요일"
    private static final Pattern FROM_GAP = JsRegex.compile("^\\s*부터[\\s,]*$");
   
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

            LocalResult prev = out.isEmpty() ? null : out.get(out.size() - 1);
            if (prev != null && prev.recur == null) {
                int prevIndex = results.indexOf(prev);
                String gap = Texts.slice(text, prev.index + prev.text.length(), r.index);
                if ((prev.end != null ? GAP : FROM_GAP).matcher(gap).find() && b.bindTo(prev, comps.get(prevIndex))) {
                    from = prev.index;
                    out.remove(out.size() - 1);
                    bound = true;
                }
            }
            LocalResult next = i + 1 < results.size() ? results.get(i + 1) : null;
            if (b.r.recur.until() == null && next != null && next.end != null && next.recur == null
                    && GAP.matcher(Texts.slice(text, to, next.index)).find() && b.bindTo(next, comps.get(i + 1))) {
                to = next.index + next.text.length();
                bound = true;
                i++;
            }
           
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

        /** 시작이나 끝이 되는 날짜를 적용해 첫 발생을 다시 계산한다 */
        boolean bindTo(LocalResult other, Components otherComps) {
            // 시각만 있는 마감("5시까지 매일")은 그날 하루의 끝이지 반복의 끝이 아니다
            if (Grouping.isTimeOnly(otherComps)) {
                return false;
            }
            // 마감("다음달까지")의 date는 기준 시각이라 그대로, 지난 시작("지난주부터")은 기준 시각부터
            LocalDateTime ref = opts.referenceDate();
            LocalDateTime begin = other.date.isAfter(ref) ? other.date : ref;
            LocalResult again = Resolver.resolve(group, opts.withReferenceDate(begin));
            if (again == null || again.recur == null) {
                return false;
            }
            // "N주 동안"은 새 시작부터 다시 센 값(again)을 쓴다
            LocalDateTime until = again.recur.until() != null ? again.recur.until() : other.end;
            if (until != null && until.isBefore(again.date)) {
                return false;
            }
            Duration shift = Duration.between(r.date, again.date);
            r.date = again.date;
            r.recur = again.recur.withUntil(until);
            if (r.end != null) {
                r.end = r.end.plus(shift);
            }
            return true;
        }
    }
}
