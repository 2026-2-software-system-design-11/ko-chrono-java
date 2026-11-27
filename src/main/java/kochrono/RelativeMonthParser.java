package kochrono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 이번달, 다음달 (초, 중순, 말), 업무용 한자어 달, "11월 말" */
final class RelativeMonthParser {
    private static final Map<String, Long> WORDS = Map.of("지난", -1L, "저번", -1L, "이번", 0L, "다음", 1L, "다다음", 2L,
            "담", 1L);
    private static final Map<String, Components.MonthPart> PART = Map.of(
            "초", Components.MonthPart.START, "중순", Components.MonthPart.MIDDLE, "말", Components.MonthPart.END);

    // "이번달 말", "다음 달 초", "이번달 중순", "다음달", "다음달 이맘때", "월초", "월말"
    private static final Pattern RE = JsRegex.compile(
            "(지난|저번|이번|다다음|다음|담)\\s*달\\s*(초|중순|말|이맘때)?|(?<![가-힣])월(초|말)");
    // "11월 말", "12월 초" (absoluteDate의 월 단독과 겹치지만 초, 중순, 말이 있으면 이쪽이 길어서 남는다)
    private static final Pattern ABS_PART = JsRegex.compile("(?<![\\d가-힣])(\\d{1,2})\\s*월\\s*(초|중순|말)");
    // 업무용 한자어 달: "익월 초", "금월 말까지", "전월 실적". "전월세"는 제외
    private static final Pattern FORMAL_MONTH = JsRegex.compile("(?<![가-힣])(금|익|차|전)월(?!세)(?:\\s*(초|중순|말))?");
    private static final Map<String, Long> FORMAL_OFFSET = Map.of("금", 0L, "익", 1L, "차", 1L, "전", -1L);

    private RelativeMonthParser() {
    }

    static List<Fragment> parse(String text) {
        List<Fragment> out = new ArrayList<>();
        for (Matcher m = RE.matcher(text); m.find(); ) {
            Components c = new Components();
            if (m.group(3) != null) {
                c.monthOffset = 0L;
                c.monthPart = PART.get(m.group(3));
            } else {
                c.monthOffset = WORDS.get(m.group(1));
                if ("이맘때".equals(m.group(2))) {
                    c.sameDay = true;
                } else if (m.group(2) != null) {
                    c.monthPart = PART.get(m.group(2));
                }
            }
            out.add(Fragment.of(m, c));
        }
        for (Matcher m = FORMAL_MONTH.matcher(text); m.find(); ) {
            Components c = new Components();
            c.monthOffset = FORMAL_OFFSET.get(m.group(1));
            if (m.group(2) != null) {
                c.monthPart = PART.get(m.group(2));
            }
            out.add(Fragment.of(m, c));
        }
        for (Matcher m = ABS_PART.matcher(text); m.find(); ) {
            Components c = new Components();
            c.month = Integer.parseInt(m.group(1));
            c.monthPart = PART.get(m.group(2));
            out.add(Fragment.of(m, c));
        }
        return out;
    }
}
