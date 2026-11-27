package kochrono;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/** "A부터 B까지", "A~B"를 하나의 구간으로 합친다 */
final class RangeMerger {
    record Merged(List<LocalResult> results, List<List<Fragment>> groups, List<Components> comps) {
    }

    private RangeMerger() {
    }

    static Merged merge(String text, List<LocalResult> results, List<List<Fragment>> groups, List<Components> comps,
                        ResolveOptions opts) {
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
            LocalDateTime end = endOf(a, b, comps.get(i + 1), opts);
            // "추석 ~ 11월"처럼 끝이 시작보다 앞이면 구간이 아니라 별개 표현
            if (end.isBefore(a.date)) {
                merged.add(a);
                continue;
            }
            // 원본은 a를 펼쳐 새 객체를 만든다. a는 이 뒤에 다시 쓰이지 않으므로 그 자리에서 고쳐도 같다
            a.end = end;
            a.text = JsRegex.trim(Texts.slice(text, a.index, b.index + b.text.length()));
            merged.add(a);
            i++;
        }
        return new Merged(merged, mergedGroups, mergedComps);
    }

    private static LocalDateTime endOf(LocalResult a, LocalResult b, Components bc, ResolveOptions opts) {
        if (Grouping.isTimeOnly(bc)) {
            return Grouping.alignEnd(a.date, bc, opts);
        }
        LocalDateTime end = b.end != null ? b.end : b.date;
        // "11월 1일부터 3일까지": 끝에 일만 있으면 시작의 연월을 이어받는다
        // ("11월 28일부터 2일까지"처럼 시작보다 이르면 다음 달)
        // 그 달에 없는 날("11월 1일부터 31일까지")은 원본처럼 넘치는 만큼 다음 달로 간다
        if (Grouping.isDayOnly(bc)) {
            YearMonth month = Calendars.yearMonth(a.date.getYear(), a.date.getMonthValue() - 1);
            LocalTime time = end.toLocalTime().withSecond(0).withNano(0);
            end = opts.local(Calendars.dayCountedFrom(month, bc.day).atTime(time));
            if (end.isBefore(a.date)) {
                end = opts.local(Calendars.dayCountedFrom(month.plusMonths(1), bc.day).atTime(time));
            }
        }
        // "월요일부터 금요일까지": 끝 요일이 시작보다 앞이면 다음 주.
        // 원본은 B의 Date를 그 자리에서 고치므로 합치지 못해도 B에 남는다. 같은 결과를 내려고 B에도 쓴다
        if (Grouping.isWeekdayOnly(bc) && end.isBefore(a.date)) {
            end = opts.local(end.plusDays(7));
            if (b.end != null) {
                b.end = end;
            } else {
                b.date = end;
            }
        }
        if (b.end == null && !b.hasTime) {
            end = opts.local(Calendars.constructed(end.toLocalDate().atTime(23, 59)));
        }
        return end;
    }
}
