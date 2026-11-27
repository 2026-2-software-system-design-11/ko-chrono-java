package kochrono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 문장 하나를 파서에 돌려 결과 목록으로 만든다. 계산은 벽시계 시각으로 한다 */
final class Pipeline {
    private static final List<Parser> PARSERS = List.of(RelativeDayParser::parse, OffsetParser::parse,
            WeekdayParser::parse, AbsoluteDateParser::parse, RelativeMonthParser::parse, RelativeYearParser::parse,
            TimeParser::parse, DurationParser::parse, RecurrenceParser::parse, HolidayParser::parse);

    // "~까지", "~부터" 접미 감지. "3시~5시"의 "~"도 시작 표시로 본다. "다음주 중", "오늘 중에"도 마감
    static final Pattern UNTIL = JsRegex.compile("^\\s*(전까지|까지|이내|안에|안으로|내로|중으로|중에(?![가-힣])|중(?![가-힣]))");
    static final Pattern FROM = JsRegex.compile("^\\s*(부터|~)\\s*");
    // "이번 주 내내"는 그 기간 전체
    private static final Pattern WHOLE = JsRegex.compile("^\\s*내내");
    // 두 날짜 표현이 한 절에 있다고 보는 사이 글자: "1시에 점심 먹었어"의 "에 "
    private static final Pattern SAME_CLAUSE = JsRegex.compile("^(?:\\s|에|엔|은|는|도|쯤)*$");

    private Pipeline() {
    }

    /** 원본의 parseLocal */
    static List<LocalResult> run(String text, ResolveOptions opts) {
        List<List<Fragment>> groups = Grouping.group(dedupe(text), text);
        boolean[] pastOf = pastOf(groups, text);
        List<LocalResult> results = new ArrayList<>();
        List<Components> comps = new ArrayList<>();
        List<List<Fragment>> resultGroups = new ArrayList<>();
        for (int gi = 0; gi < groups.size(); gi++) {
            List<Fragment> g = groups.get(gi);
            Fragment last = g.get(g.size() - 1);
            String tail = Texts.slice(text, last.index() + last.text().length());
            Matcher until = UNTIL.matcher(tail);
            boolean hasUntil = until.find();
            if (!hasUntil) {
                until = WHOLE.matcher(tail);
                hasUntil = until.find();
            }
            if (hasUntil && until.group().contains("내내")) {
                last.components().whole = true;
            } else if (hasUntil) {
                last.components().isEnd = true;
            }
            if (FROM.matcher(tail).find()) {
                last.components().isStart = true;
            }
            // 규칙 37: 마감이나 구간이 아닌 표현 뒤의 말이 과거형이면 가까운 과거로 해석한다
            if (!hasUntil && !last.components().isStart && pastOf[gi]) {
                last.components().past = true;
            }
            LocalResult r = Resolver.resolve(g, opts);
            if (r == null) {
                continue;
            }
            int end = r.endIndex + (hasUntil ? until.group().length() : 0);
            // 조각이 공백으로 시작할 수 있다 ("오후 3시" 앞의 공백). index는 첫 글자를 가리키게
            String raw = Texts.slice(text, g.get(0).index(), end);
            r.index = g.get(0).index() + (raw.length() - JsRegex.trimStart(raw).length());
            r.text = JsRegex.trim(raw);
            results.add(r);
            comps.add(Grouping.merge(g));
            resultGroups.add(g);
        }
        RangeMerger.Merged merged = RangeMerger.merge(text, results, resultGroups, comps, opts);
        return RecurrenceBinder.bind(text, merged.results(), merged.groups(), merged.comps(), opts);
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

    /**
     * 규칙 37: 표현 뒤의 말이 과거형인지. 뒤에서부터 본다. "1시에 점심 먹었어"처럼 다음 표현이
     * 조사만 사이에 두고 이어지면 같은 절이라 그쪽 시제를 따른다
     */
    private static boolean[] pastOf(List<List<Fragment>> groups, String text) {
        boolean[] pastOf = new boolean[groups.size()];
        for (int gi = groups.size() - 1; gi >= 0; gi--) {
            List<Fragment> g = groups.get(gi);
            Fragment last = g.get(g.size() - 1);
            List<Fragment> next = gi + 1 < groups.size() ? groups.get(gi + 1) : null;
            int from = last.index() + last.text().length();
            String tail = Texts.slice(text, from, next != null ? next.get(0).index() : text.length());
            pastOf[gi] = next != null && SAME_CLAUSE.matcher(tail).find() ? pastOf[gi + 1] : PastTense.isPastClause(tail);
        }
        return pastOf;
    }
}
