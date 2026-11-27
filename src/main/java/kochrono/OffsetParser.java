package kochrono;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** "N일 뒤", "N주 후", "N달 전", "N시간 뒤", "N분 후" */
final class OffsetParser {
    // "3일 뒤", "2주 후", "한 달 전", "세 시간 뒤", "30분 후", "열흘 뒤", "이틀 뒤", "1년 뒤", "30분 내로", "30분쯤 뒤"
    private static final Pattern RE = JsRegex.compile(
            "(?:(\\d+|[일이삼사오육칠팔구십한두세네다섯여섯일곱여덟아홉열]+)\\s*(일|주|주일|달|개월|년|시간|분)"
                    + "|(보름|열흘|이틀|사흘|나흘|닷새|엿새|이레|여드레|아흐레))\\s*(?:쯤|정도)?\\s*(뒤|후|전|이내|안에|안으로|내로|이후)");
    private static final Set<String> WITHIN = Set.of("이내", "안에", "안으로", "내로");

    private OffsetParser() {
    }

    static List<Fragment> parse(String text) {
        List<Fragment> out = new ArrayList<>();
        for (Matcher m = RE.matcher(text); m.find(); ) {
            Long n = KoreanNumber.parse(m.group(3) != null ? m.group(3) : m.group(1));
            if (n == null) {
                continue;
            }
            long sign = m.group(4).equals("전") ? -1 : 1;
            Components c = new Components();
            switch (m.group(3) != null ? "일" : m.group(2)) {
                case "일" -> c.dayOffset = sign * n;
                case "주", "주일" -> c.dayOffset = sign * n * 7;
                case "달", "개월" -> {
                    // "한 달 뒤"는 다음 달 같은 날 (그 달 1일이 아니라)
                    c.monthOffset = sign * n;
                    c.sameDay = true;
                }
                case "년" -> {
                    // "2028년 후"는 "2028년 이후"일 가능성이 크다. 100년 넘게는 상대 이동으로 보지 않는다
                    if (n > 100) {
                        continue;
                    }
                    c.yearOffset = (int) (sign * n);
                    c.sameDay = true;
                }
                case "시간" -> c.hourOffset = sign * n;
                default -> c.minuteOffset = sign * n;
            }
            if (WITHIN.contains(m.group(4))) {
                c.isEnd = true;
            }
            out.add(Fragment.of(m, c));
        }
        return out;
    }
}
