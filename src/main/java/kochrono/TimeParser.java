package kochrono;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 시각 표현 */
final class TimeParser {
    private static final String HOUR_WORD = "\\d{1,2}|한|두|세|네|다섯|여섯|일곱|여덟|아홉|열|열한|열두";
    private static final String PERIOD = "오전|오후|새벽|아침|저녁|밤|낮";

    // "24시 편의점", "24시 영업": 하루 종일 연다는 뜻이지 시각이 아니다
    private static final Pattern OPEN_ALL_DAY = JsRegex.compile(
            "^\\s*(?:편의점|영업|운영|무인|사우나|찜질방|약국|병원|동물병원|매장|카페|식당|해장국|김밥|헬스|콜센터|상담)");
    // "오후 3시", "3시 반", "오전 10시 30분", "새벽 2시", "저녁 7시", "낮 12시", "15시", "7시30", "3시 정각"
    private static final Pattern CLOCK = JsRegex.compile("(" + PERIOD + "|정오)?\\s*(" + HOUR_WORD
            + ")\\s*시(?!간)\\s*(?:(반)|(\\d{1,2})\\s*분|(\\d{2})(?![\\d가-힣])|정각)?");

    private TimeParser() {
    }

    static int applyMeridiem(String prefix, int hour) {
        if (hour == 24) {
            return 0;
        }
        if (prefix == null) {
            return hour;
        }
        return switch (prefix) {
            // 밤 12시는 자정, 밤 1~5시는 새벽
            case "밤" -> hour == 12 ? 0 : hour <= 5 ? hour : hour < 12 ? hour + 12 : hour;
            case "오후", "저녁", "낮" -> hour < 12 ? hour + 12 : hour;
            case "오전", "새벽", "아침" -> hour == 12 ? 0 : hour;
            case "정오" -> 12;
            default -> hour;
        };
    }

    static List<Fragment> parse(String text) {
        List<Fragment> out = new ArrayList<>();
        Taken taken = new Taken();
        clock(text, out, taken);
        return out;
    }

    private static Components time(int hour, int minute, boolean meridiem) {
        Components c = new Components();
        c.hour = hour;
        c.minute = minute;
        c.meridiem = meridiem;
        return c;
    }

    private static void clock(String text, List<Fragment> out, Taken taken) {
        for (Matcher m = CLOCK.matcher(text); m.find(); ) {
            Long raw = KoreanNumber.parse(m.group(2));
            if (raw == null || raw > 24) {
                continue;
            }
            if (raw == 24 && m.group(1) == null && OPEN_ALL_DAY.matcher(text.substring(m.end())).find()) {
                continue;
            }
            if (!taken.take(m.start(), m.end())) {
                continue;
            }
            int hour = applyMeridiem(m.group(1), raw.intValue());
            int minute = 0;
            if (m.group(3) != null) {
                minute = 30;
            } else if (m.group(4) != null || m.group(5) != null) {
                minute = Integer.parseInt(m.group(4) != null ? m.group(4) : m.group(5));
            }
            if (minute > 59) {
                continue;
            }
            Components c = time(hour, minute, m.group(1) != null);
            if (raw == 24 || ("밤".equals(m.group(1)) && raw == 12)) {
                c.midnight = true;
            }
            out.add(Fragment.of(m, c));
        }
    }
}
