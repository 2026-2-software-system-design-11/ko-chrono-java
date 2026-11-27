package kochrono;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 기간 표현. 날짜를 계산하지 않고 길이만 뽑는다. "A부터 N일간"으로 쓰일 때 resolve가 끝을 만든다 */
final class DurationParser {
    // "3일간", "일주일 동안", "2주간", "한 달간", "2시간 동안", "90분간", "이틀간"
    private static final Pattern RE = JsRegex.compile(
            "(?:(\\d+|[일이삼사오육칠팔구십한두세네다섯여섯일곱여덟아홉열]+)\\s*(일|주|주일|달|개월|시간|분)"
                    + "|(보름|열흘|이틀|사흘|나흘|닷새|엿새|이레|여드레|아흐레))\\s*(간|동안)");
    // "3박 4일"은 4일
    private static final Pattern NIGHTS = JsRegex.compile("(\\d+)\\s*박\\s*(\\d+)\\s*일");

    private DurationParser() {
    }

    static List<Fragment> parse(String text) {
        List<Fragment> out = new ArrayList<>();
        for (Matcher m = NIGHTS.matcher(text); m.find(); ) {
            long days = KoreanNumber.digits(m.group(2));
            if (days == 0) {
                continue;
            }
            Components c = new Components();
            c.durationDays = days;
            out.add(Fragment.of(m, c));
        }
        for (Matcher m = RE.matcher(text); m.find(); ) {
            Long n = KoreanNumber.parse(m.group(3) != null ? m.group(3) : m.group(1));
            // "0일간"은 기간이 아니다
            if (n == null || n == 0) {
                continue;
            }
            Components c = new Components();
            switch (m.group(3) != null ? "일" : m.group(2)) {
                case "일" -> c.durationDays = n;
                case "주", "주일" -> c.durationDays = n * 7;
                case "달", "개월" -> c.durationMonths = n;
                case "시간" -> c.durationMinutes = n * 60;
                default -> c.durationMinutes = n;
            }
            out.add(Fragment.of(m, c));
        }
        return out;
    }
}
