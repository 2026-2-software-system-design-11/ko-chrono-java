package kochrono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 절대 날짜 표현 */
final class AbsoluteDateParser {
    // "2026년 11월 5일", "26년 11월 5일", "11월 5일"
    private static final Pattern FULL = JsRegex.compile(
            "(?:(?<!\\d)(\\d{4}|\\d{2})\\s*년\\s*)?(\\d{1,2})\\s*월\\s*(\\d{1,2})\\s*일(?!간|\\s*동안)");
    // "2026-11-05", "26.11.5", "2027-01-01T09:00" (ISO 8601 날짜와 시각)
    private static final Pattern ISO = JsRegex.compile(
            "(?<![\\d/.-])(\\d{4}|\\d{2})[-./]\\s*(\\d{1,2})[-./]\\s*(\\d{1,2})(?!\\d)(?:T(\\d{2}):(\\d{2}))?");
    // "11/5", "11.5" (월일로 해석)
    private static final Pattern SLASH = JsRegex.compile("(?<![\\d/.-])(\\d{1,2})\\s*[/.]\\s*(\\d{1,2})(?![\\d/.:])");
    // "십일월 오일", "유월 이십오일" (오탐을 막으려고 월과 일이 모두 있을 때만)
    private static final Pattern HANGUL = JsRegex.compile(
            "(?<![가-힣])(유|시|[일이삼사오육칠팔구십]{1,2})월\\s*([일이삼사오육칠팔구십]{1,3})일(?!요일)");
    private static final Map<String, Long> HANGUL_MONTH = Map.of("유", 6L, "시", 10L);

    private AbsoluteDateParser() {
    }

    /**
     * 두 자리 연도: 00~69는 20xx, 70~99는 19xx.
     * 네 자리 0000~0099년은 원본의 Date 생성자가 1900년대로 읽으므로 같은 결과가 되게 여기서 바꾼다
     */
    static int expandYear(String y) {
        int n = Integer.parseInt(y);
        if (y.length() != 2) {
            return n < 100 ? 1900 + n : n;
        }
        return n < 70 ? 2000 + n : 1900 + n;
    }

    static List<Fragment> parse(String text) {
        List<Fragment> out = new ArrayList<>();
        Taken taken = new Taken();

        for (Matcher m = ISO.matcher(text); m.find(); ) {
            int month = Integer.parseInt(m.group(2));
            int day = Integer.parseInt(m.group(3));
            if (month < 1 || month > 12 || day < 1 || day > 31 || !taken.take(m.start(), m.end())) {
                continue;
            }
            Components c = date(expandYear(m.group(1)), month, day);
            if (m.group(4) != null && Integer.parseInt(m.group(4)) < 24 && Integer.parseInt(m.group(5)) < 60) {
                c.hour = Integer.parseInt(m.group(4));
                c.minute = Integer.parseInt(m.group(5));
                c.meridiem = true;
            }
            out.add(Fragment.of(m, c));
        }
        for (Matcher m = FULL.matcher(text); m.find(); ) {
            if (!taken.take(m.start(), m.end())) {
                continue;
            }
            Integer year = m.group(1) != null ? expandYear(m.group(1)) : null;
            out.add(Fragment.of(m, date(year, Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)))));
        }
        for (Matcher m = HANGUL.matcher(text); m.find(); ) {
            Long month = HANGUL_MONTH.containsKey(m.group(1)) ? HANGUL_MONTH.get(m.group(1)) : KoreanNumber.parse(m.group(1));
            Long day = KoreanNumber.parse(m.group(2));
            if (month == null || day == null || month < 1 || month > 12 || day < 1 || day > 31
                    || !taken.take(m.start(), m.end())) {
                continue;
            }
            out.add(Fragment.of(m, date(null, month.intValue(), day.intValue())));
        }
        for (Matcher m = SLASH.matcher(text); m.find(); ) {
            // 원본은 범위 검사 전에 구간을 잡는다. 없는 월일도 그 자리를 막는다
            if (!taken.take(m.start(), m.end())) {
                continue;
            }
            int month = Integer.parseInt(m.group(1));
            int day = Integer.parseInt(m.group(2));
            if (month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                out.add(Fragment.of(m, date(null, month, day)));
            }
        }
        return out;
    }

    private static Components date(Integer year, int month, int day) {
        Components c = new Components();
        c.year = year;
        c.month = month;
        c.day = day;
        return c;
    }
}
