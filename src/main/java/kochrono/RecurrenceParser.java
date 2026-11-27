package kochrono;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 반복 일정. 날짜는 계산하지 않고 규칙만 뽑는다 (첫 발생일은 resolve가 계산한다) */
final class RecurrenceParser {
    private static final Map<String, List<Integer>> SETS = Map.of(
            "평일", List.of(1, 2, 3, 4, 5), "주중", List.of(1, 2, 3, 4, 5), "주말", List.of(6, 0));

    // 요일 목록 "월수금", "화, 목", "월요일이랑 수요일", "월~금". 목록이 끝난 뒤에는 다른 한글이 붙으면 안 된다
    // ("매주 일요일 일정"의 "일"을 요일로 잡지 않게). 조사와 "마다"는 허용. 구분자에는 가운뎃점(U+00B7)도 있다
    private static final String ITEM = "[월화수목금토일](?:요일|욜)?(?:\\s*~\\s*[월화수목금토일](?:요일|욜)?)?";
    private static final String LIST = ITEM + "(?:\\s*(?:,|\\u00B7|/|이랑|랑|하고|과|와)?\\s*" + ITEM + ")*";
    private static final String LIST_END = "(?:(?![가-힣])|(?=마다|에|엔|은|는|도))";

    // 주기 말. 간격 숫자를 그룹 하나로 잡는다 (없으면 1, "격주"는 2)
    // "매주", "격주", "일주일마다", "2주마다", "두 주에 한 번"
    private static final String WEEK_EVERY =
            "매주|매\\s*주|격주|(?:일|한)\\s*주일?\\s*마다|(\\d{1,2}|두|세|네)\\s*주일?\\s*(?:마다|에\\s*한\\s*번)";
    // "매주 평일", "평일마다", "주말마다", "평일 매일"
    private static final Pattern WEEKDAY_SET = JsRegex.compile("매주\\s*(평일|주중|주말)|(?<![가-힣])(평일|주중|주말)\\s*(?:마다|매일)");
    // "매주 월요일", "매주 월수금", "격주 금요일", "2주마다 수요일", "2주에 한 번 화요일", "일주일마다 월요일"
    private static final Pattern WEEKLY = JsRegex.compile("(" + WEEK_EVERY + ")\\s*(" + LIST + ")" + LIST_END + "(?:\\s*마다)?");
    // "월요일마다", "화요일이랑 목요일마다"
    private static final Pattern DAYS_EVERY = JsRegex.compile(
            "(?<![가-힣])([월화수목금토일](?:요일|욜)(?:\\s*(?:,|\\u00B7|이랑|랑|하고|과|와)?\\s*[월화수목금토일](?:요일|욜))*)\\s*마다");

    private static final Map<String, Integer> NTH = Map.of("첫째", 1, "첫", 1, "둘째", 2, "셋째", 3, "넷째", 4, "마지막", -1);
    // "매달", "매월", "달마다", "한 달마다", "2개월마다", "두 달에 한 번"
    private static final String MONTH_EVERY = "매달|매월|매\\s*달|다달이|(?<![가-힣])달\\s*마다"
            + "|(\\d{1,2}|한|두|세|네|다섯|여섯)\\s*(?:개월|달)\\s*(?:마다|에\\s*한\\s*번)";
    // "매년", "해마다", "2년마다"
    private static final String YEAR_EVERY = "매년|매해|해마다|(\\d{1,2}|한|두|세)\\s*년\\s*마다";
    // "매달 첫째 주 월요일", "매월 마지막 주 금요일"
    private static final Pattern MONTHLY_NTH = JsRegex.compile(
            "(?:매달|매월|매\\s*달)\\s*(첫째|첫|둘째|셋째|넷째|마지막)\\s*주\\s*([월화수목금토일])(?:요일|욜)");
    // "매달 15일", "매월 말일", "매달 말", "매달 초", "2개월마다 1일", "달마다 14일"
    private static final Pattern MONTHLY_DAY = JsRegex.compile(
            "(?:" + MONTH_EVERY + ")\\s*(?:(\\d{1,2})\\s*일|(말일|마지막\\s*날|말)|(초))");
    // "매년 3월 1일", "해마다 12월 25일", "2년마다 3월 1일"
    private static final Pattern YEARLY = JsRegex.compile("(?:" + YEAR_EVERY + ")\\s*(\\d{1,2})\\s*월\\s*(\\d{1,2})\\s*일");

    private static final Pattern DAY_NAMES = Pattern.compile("요일|욜");
    private static final Pattern DAY_ITEMS = JsRegex.compile("([월화수목금토일])\\s*~\\s*([월화수목금토일])|([월화수목금토일])");

    private RecurrenceParser() {
    }

    /** "월수금", "화, 목요일", "월~금"을 요일 번호들로. "요일" 안의 "일"은 일요일이 아니다 */
    static List<Integer> weekdaysOf(String list) {
        LinkedHashSet<Integer> out = new LinkedHashSet<>();
        String plain = DAY_NAMES.matcher(list).replaceAll("");
        // "월~금"은 월화수목금 (월요일부터 센다, "금~일"은 금토일)
        Matcher m = DAY_ITEMS.matcher(plain);
        while (m.find()) {
            if (m.group(3) != null) {
                out.add(WeekdayParser.WEEKDAY.get(m.group(3)));
                continue;
            }
            int from = (WeekdayParser.WEEKDAY.get(m.group(1)) + 6) % 7;
            int to = (WeekdayParser.WEEKDAY.get(m.group(2)) + 6) % 7;
            for (int i = from; i != (to + 1) % 7; i = (i + 1) % 7) {
                out.add((i + 1) % 7);
            }
        }
        return List.copyOf(out);
    }

    /** 주기 말의 간격. 숫자 그룹이 없으면 1, "격주"는 2. 0이거나 못 읽으면 null */
    private static Integer intervalOf(String word, String num) {
        if (word.startsWith("격주")) {
            return 2;
        }
        if (num == null) {
            return 1;
        }
        Long n = KoreanNumber.parse(num);
        return n == null || n == 0 ? null : n.intValue();
    }

    static List<Fragment> parse(String text) {
        List<Fragment> out = new ArrayList<>();
        Taken taken = new Taken();
        for (Matcher m = MONTHLY_NTH.matcher(text); m.find(); ) {
            push(out, taken, m, RecurSpec.of(Frequency.MONTHLY, 1)
                    .withWeekdays(List.of(WeekdayParser.WEEKDAY.get(m.group(2)))).withNth(NTH.get(m.group(1))));
        }
        for (Matcher m = MONTHLY_DAY.matcher(text); m.find(); ) {
            Integer n = intervalOf(m.group(), m.group(1));
            int day = m.group(2) != null ? Integer.parseInt(m.group(2)) : m.group(3) != null ? -1 : 1;
            if (n != null && day != 0 && day <= 31) {
                push(out, taken, m, RecurSpec.of(Frequency.MONTHLY, n).withMonthDay(day));
            }
        }
        for (Matcher m = YEARLY.matcher(text); m.find(); ) {
            Integer n = intervalOf(m.group(), m.group(1));
            int month = Integer.parseInt(m.group(2));
            int day = Integer.parseInt(m.group(3));
            if (n != null && month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                push(out, taken, m, RecurSpec.of(Frequency.YEARLY, n).withMonth(month).withMonthDay(day));
            }
        }
        for (Matcher m = WEEKDAY_SET.matcher(text); m.find(); ) {
            String set = m.group(1) != null ? m.group(1) : m.group(2);
            push(out, taken, m, RecurSpec.of(Frequency.WEEKLY, 1).withWeekdays(SETS.get(set)));
        }
        for (Matcher m = WEEKLY.matcher(text); m.find(); ) {
            Integer n = intervalOf(m.group(1), m.group(2));
            if (n != null) {
                push(out, taken, m, RecurSpec.of(Frequency.WEEKLY, n).withWeekdays(weekdaysOf(m.group(3))));
            }
        }
        for (Matcher m = DAYS_EVERY.matcher(text); m.find(); ) {
            push(out, taken, m, RecurSpec.of(Frequency.WEEKLY, 1).withWeekdays(weekdaysOf(m.group(1))));
        }
        return out;
    }

    private static void push(List<Fragment> out, Taken taken, Matcher m, RecurSpec recur) {
        if (taken.take(m.start(), m.end())) {
            Components c = new Components();
            c.recur = recur;
            out.add(Fragment.of(m, c));
        }
    }
}
