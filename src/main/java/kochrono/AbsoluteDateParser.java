package kochrono;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 절대 날짜 표현 */
final class AbsoluteDateParser {
    // "2026-11-05", "26.11.5", "2027-01-01T09:00" (ISO 8601 날짜와 시각)
    private static final Pattern ISO = JsRegex.compile(
            "(?<![\\d/.-])(\\d{4}|\\d{2})[-./]\\s*(\\d{1,2})[-./]\\s*(\\d{1,2})(?!\\d)(?:T(\\d{2}):(\\d{2}))?");

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
