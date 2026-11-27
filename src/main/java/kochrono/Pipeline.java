package kochrono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** 문장 하나를 파서에 돌려 결과 목록으로 만든다. 계산은 벽시계 시각으로 한다 */
final class Pipeline {
    private static final List<Parser> PARSERS = List.of(RelativeDayParser::parse, OffsetParser::parse,
            WeekdayParser::parse, AbsoluteDateParser::parse, RelativeMonthParser::parse, RelativeYearParser::parse,
            TimeParser::parse, DurationParser::parse, RecurrenceParser::parse, HolidayParser::parse);

    private Pipeline() {
    }

    /** 원본의 parseLocal */
    static List<LocalResult> run(String text, ResolveOptions opts) {
        List<List<Fragment>> groups = Grouping.group(dedupe(text), text);
        List<LocalResult> results = new ArrayList<>();
        for (int gi = 0; gi < groups.size(); gi++) {
            List<Fragment> g = groups.get(gi);
            LocalResult r = Resolver.resolve(g, opts);
            if (r == null) {
                continue;
            }
            int end = r.endIndex;
            // 조각이 공백으로 시작할 수 있다 ("오후 3시" 앞의 공백). index는 첫 글자를 가리키게
            String raw = Texts.slice(text, g.get(0).index(), end);
            r.index = g.get(0).index() + (raw.length() - JsRegex.trimStart(raw).length());
            r.text = JsRegex.trim(raw);
            results.add(r);
        }
        return results;
    }

    /** 모든 파서의 조각을 모으고 겹치는 조각은 더 긴 것만 남긴다 */
    private static List<Fragment> dedupe(String text) {
        List<Fragment> sorted = new ArrayList<>();
        for (Parser p : PARSERS) {
            sorted.addAll(p.parse(text));
        }
        sorted.sort(Comparator.comparingInt((Fragment f) -> f.text().length()).reversed());
        List<Fragment> kept = new ArrayList<>();
        Taken taken = new Taken();
        for (Fragment f : sorted) {
            if (taken.take(f.index(), f.index() + f.text().length())) {
                kept.add(f);
            }
        }
        return kept;
    }
}
