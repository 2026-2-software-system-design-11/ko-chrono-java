package kochrono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 요일 관련 표현 */
final class WeekdayParser {
    static final Map<String, Integer> WEEKDAY = Map.of("일", 0, "월", 1, "화", 2, "수", 3, "목", 4, "금", 5, "토", 6);
    private static final Map<String, Integer> WEEK_WORDS = Map.of("지지난", -2, "저저번", -2, "지난", -1, "저번", -1,
            "이번", 0, "다음", 1, "다다음", 2, "다담", 2, "담", 1);
    private static final String WEEK_WORD = "지지난|저저번|지난|저번|이번|다다음|다음|다담|담";
    private static final String DAY = "월|화|수|목|금|토|일";
    // 요일 글자만 쓸 때(규칙 36) 뒤에 와야 하는 것: 끝, 구두점, 시각("월 2시", "금 저녁"), 다른 요일 글자("수, 목"),
    // 조사("금에", "월까지"). "다음주 일정", "다음주 일 많아", "이번주 목 아파"의 일, 목은 요일이 아니다
    private static final String DAY_ALONE = "(?=\\s*$|\\s*[,.!?~～)]|\\s*(?:\\d|오전|오후|새벽|아침|점심|낮(?!잠)|저녁|밤(?!새)|(?:"
            + DAY + ")(?![가-힣]))|(?:에|엔|은|는|도|나|이나|쯤)(?![가-힣])|에는|까지|부터)";

    // "이번 주말", "다음주말", "지난 주말"은 그 주의 토요일
    private static final Pattern WEEKEND = JsRegex.compile("(" + WEEK_WORD + ")\\s*주말");
    // "월~금", "화~목요일"은 두 조각 (KoChrono가 "~"로 구간을 합친다)
    private static final Pattern RANGE = JsRegex.compile(
            "(?<![\\d가-힣])(" + DAY + ")\\s*~\\s*((" + DAY + ")(?:요일|욜)?)(?![가-힣])");
    // "11/3(화)", "11월 5일 (목)" 뒤에 붙는 요일 표시
    private static final Pattern PAREN = JsRegex.compile("\\((" + DAY + ")\\)");
    // "2주 뒤 월요일", "3주 후 금요일"은 N주 뒤의 그 주의 X요일 ("다다음주 월요일"과 같은 계산)
    private static final Pattern WEEKS_LATER = JsRegex.compile(
            "(\\d{1,2}|한|두|세|네|다섯)\\s*주\\s*(?:뒤|후)\\s*(" + DAY + ")(?:요일|욜)");
    // "지난 금요일"은 오늘 제외 가장 가까운 과거, "다음 금요일"은 오늘 제외 가장 가까운 미래
    private static final Pattern DIR = JsRegex.compile("(지난|저번|다음)\\s*(" + DAY + ")(?:요일|욜)");
    // "주말에 보자" ("이번 주말"은 위 WEEKEND가 먼저 잡는다)
    private static final Pattern BARE_WEEKEND = JsRegex.compile("(?<![가-힣])주말(?=[\\s,에엔은는도까부]|$)");
    // "다음주 화요일", "이번 주 금", "다다음주 월요일", "담주 월욜", "담주 월 2시"
    private static final Pattern WITH_WEEK = JsRegex.compile(
            "(" + WEEK_WORD + ")\\s*주\\s*(" + DAY + ")(?:요일|욜|" + DAY_ALONE + ")");
    // "화요일", "화욜" 단독 (요일 글자만은 오탐이 많아 "요일"이나 "욜"까지 요구)
    private static final Pattern BARE = JsRegex.compile("(?<![가-힣])(월|화|수|목|금|토|일)(?:요일|욜)");
    // "다음주", "이번주" 단독 (요일 없음)은 그 주의 시작 요일
    private static final Pattern WEEK_ONLY = JsRegex.compile(
            "(" + WEEK_WORD + ")\\s*주(\\s*이맘때)?(?!\\s*(?:" + DAY + ")(?:요일|욜|" + DAY_ALONE + "))");

    private WeekdayParser() {
    }

    static List<Fragment> parse(String text) {
        List<Fragment> out = new ArrayList<>();
        Taken taken = new Taken();
        for (Matcher m = WEEKS_LATER.matcher(text); m.find(); ) {
            Long n = KoreanNumber.parse(m.group(1));
            if (n != null) {
                add(out, taken, m, weekday(n.intValue(), WEEKDAY.get(m.group(2))));
            }
        }
        for (Matcher m = RANGE.matcher(text); m.find(); ) {
            out.add(new Fragment(m.group(1), m.start(), weekday(null, WEEKDAY.get(m.group(1)))));
            out.add(new Fragment(m.group(2), m.start(2), weekday(null, WEEKDAY.get(m.group(3)))));
            taken.add(m.start(), m.end());
        }
        for (Matcher m = PAREN.matcher(text); m.find(); ) {
            add(out, taken, m, weekday(null, WEEKDAY.get(m.group(1))));
        }
        for (Matcher m = DIR.matcher(text); m.find(); ) {
            Components c = weekday(null, WEEKDAY.get(m.group(2)));
            c.weekdayDir = m.group(1).equals("다음") ? Components.WeekdayDir.NEXT : Components.WeekdayDir.PAST;
            add(out, taken, m, c);
        }
        for (Matcher m = WEEKEND.matcher(text); m.find(); ) {
            Components c = weekday(WEEK_WORDS.get(m.group(1)), 6);
            c.weekend = true;
            add(out, taken, m, c);
        }
        for (Matcher m = BARE_WEEKEND.matcher(text); m.find(); ) {
            if (!taken.overlaps(m.start(), m.end())) {
                Components c = weekday(null, 6);
                c.weekend = true;
                add(out, taken, m, c);
            }
        }
        for (Matcher m = WITH_WEEK.matcher(text); m.find(); ) {
            if (!taken.overlaps(m.start(), m.end())) {
                add(out, taken, m, weekday(WEEK_WORDS.get(m.group(1)), WEEKDAY.get(m.group(2))));
            }
        }
        for (Matcher m = BARE.matcher(text); m.find(); ) {
            if (!taken.overlaps(m.start(), m.end())) {
                add(out, taken, m, weekday(null, WEEKDAY.get(m.group(1))));
            }
        }
        for (Matcher m = WEEK_ONLY.matcher(text); m.find(); ) {
            if (!taken.overlaps(m.start(), m.end())) {
                Components c = weekday(WEEK_WORDS.get(m.group(1)), null);
                c.sameDay = m.group(2) != null;
                out.add(Fragment.of(m, c));
            }
        }
        return out;
    }

    private static Components weekday(Integer weekOffset, Integer weekday) {
        Components c = new Components();
        c.weekOffset = weekOffset;
        c.weekday = weekday;
        return c;
    }

    private static void add(List<Fragment> out, Taken taken, Matcher m, Components c) {
        out.add(Fragment.of(m, c));
        taken.add(m.start(), m.end());
    }
}
