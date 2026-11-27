package kochrono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 올해, 내년, 작년 (초, 말, 이맘때), 연말, 연초 */
final class RelativeYearParser {
    private static final Map<String, Integer> WORDS = Map.of("재작년", -2, "작년", -1, "지난해", -1, "올해", 0, "금년", 0,
            "내년", 1, "명년", 1, "익년", 1, "내후년", 2);

    // "올해 말", "내년 초", "작년", "재작년", "내후년", "지난해", "내년 이맘때", "연말", "연초"
    private static final Pattern RE = JsRegex.compile(
            "(재작년|작년|지난해|올해|금년|내후년|내년|명년|익년)(?:\\s*(초|말|이맘때))?|(?<![가-힣])(연말(?!정산)|연초)");

    private RelativeYearParser() {
    }

    static List<Fragment> parse(String text) {
        List<Fragment> out = new ArrayList<>();
        for (Matcher m = RE.matcher(text); m.find(); ) {
            Components c = new Components();
            if (m.group(3) != null) {
                // "연초"는 연도가 없으므로 지난 월일은 내년 규칙(13)을 따른다
                boolean start = m.group(3).equals("연초");
                c.month = start ? 1 : 12;
                c.monthPart = start ? Components.MonthPart.START : Components.MonthPart.END;
            } else {
                c.yearOffset = WORDS.get(m.group(1));
                String part = m.group(2);
                if ("초".equals(part)) {
                    c.month = 1;
                    c.monthPart = Components.MonthPart.START;
                } else if ("말".equals(part)) {
                    c.month = 12;
                    c.monthPart = Components.MonthPart.END;
                } else if ("이맘때".equals(part)) {
                    c.sameDay = true;
                }
            }
            out.add(Fragment.of(m, c));
        }
        return out;
    }
}
