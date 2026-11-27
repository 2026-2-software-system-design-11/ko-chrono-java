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
    // "2027년" 단독 (월이 뒤따르지 않을 때). "26년"은 "26년 동안" 오탐이 있어 4자리만
    private static final Pattern YEAR_ONLY = JsRegex.compile("(?<!\\d)((?:19|20)\\d{2})\\s*년(?!\\s*\\d{1,2}\\s*월)");
    // "11월" 단독
    private static final Pattern MONTH_ONLY = JsRegex.compile("(?<![\\d가-힣])(\\d{1,2})\\s*월(?!\\s*\\d{1,2}\\s*일|생|세)");
    // "5일" 단독. "5일간", "3일 동안", "3박 4일"은 기간이고 "주 5일 근무"(횟수), "30일 챌린지"도 날짜가 아니다.
    // 원본의 앞쪽 조건 중 길이가 정해지지 않은 두 가지("N박 " 뒤, "주 " 뒤)는 Java 정규식에 쓸 수 없어
    // afterNightsOrWeek에서 따로 본다
    private static final Pattern DAY_ONLY = JsRegex.compile(
            "(?<!\\d)(\\d{1,2})\\s*일(?!요일|간|장|째|차|분|\\s*동안|\\s*연속|\\s*챌린지)");

    private AbsoluteDateParser() {
    }

    /** 두 자리 연도: 00~69는 20xx, 70~99는 19xx */
    static int expandYear(String y) {
        int n = Integer.parseInt(y);
        if (y.length() != 2) {
            return n;
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
        for (Matcher m = YEAR_ONLY.matcher(text); m.find(); ) {
            if (taken.take(m.start(), m.end())) {
                Components c = new Components();
                c.year = Integer.parseInt(m.group(1));
                out.add(Fragment.of(m, c));
            }
        }
        for (Matcher m = MONTH_ONLY.matcher(text); m.find(); ) {
            if (!taken.take(m.start(), m.end())) {
                continue;
            }
            int month = Integer.parseInt(m.group(1));
            if (month >= 1 && month <= 12) {
                Components c = new Components();
                c.month = month;
                out.add(Fragment.of(m, c));
            }
        }
        Matcher m = DAY_ONLY.matcher(text);
        int from = 0;
        while (m.find(from)) {
            if (afterNightsOrWeek(text, m.start())) {
                // 정규식 엔진처럼 다음 글자부터 다시 찾는다
                from = m.start() + 1;
                continue;
            }
            from = m.end();
            if (!taken.take(m.start(), m.end())) {
                continue;
            }
            int day = Integer.parseInt(m.group(1));
            if (day >= 1 && day <= 31) {
                Components c = new Components();
                c.day = day;
                out.add(Fragment.of(m, c));
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

    /**
     * 원본의 {@code (?<!\d\s*박\s*)(?<!(?<![가-힣])주\s*)}. i 바로 앞이 "숫자 + 공백 + 박 + 공백"이거나
     * "(한글이 아닌 글자 뒤의) 주 + 공백"이면 참. 공백은 이어져 있으므로 뒤에서부터 공백을 모두 건너뛰고 보면 같은 뜻이다.
     */
    static boolean afterNightsOrWeek(String text, int i) {
        int j = skipSpacesBack(text, i);
        if (j == 0) {
            return false;
        }
        char before = text.charAt(j - 1);
        if (before == '박') {
            int k = skipSpacesBack(text, j - 1);
            return k > 0 && text.charAt(k - 1) >= '0' && text.charAt(k - 1) <= '9';
        }
        if (before == '주') {
            return j - 1 == 0 || !isHangul(text.charAt(j - 2));
        }
        return false;
    }

    private static int skipSpacesBack(String text, int i) {
        while (i > 0 && JsRegex.isSpace(text.charAt(i - 1))) {
            i--;
        }
        return i;
    }

    private static boolean isHangul(char c) {
        return c >= '가' && c <= '힣';
    }
}
