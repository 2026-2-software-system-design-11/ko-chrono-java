package kochrono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    // "5시 10분 전"은 4시 50분
    private static final Pattern BEFORE = JsRegex.compile("(" + PERIOD + ")?\\s*(" + HOUR_WORD
            + ")\\s*시\\s*(\\d{1,2})\\s*분\\s*전(?=[\\s,에까]|$)");
    // "오전 중으로", "오후 중에"는 그 시간대의 끝(12시, 18시)까지
    private static final Pattern HALF_DAY = JsRegex.compile("(오전|오후)\\s*중(?!순)(?:으로|에는|에)?");
    // "3pm", "9 AM", "12pm"
    private static final Pattern AMPM = JsRegex.compile("(?<![\\d:])(\\d{1,2})(?::(\\d{2}))?\\s*([ap])\\.?m\\.?(?![a-z])",
            Pattern.CASE_INSENSITIVE);
    // "3~5시", "오후 3~5시"는 "오후 3"과 "5시" 두 조각 (KoChrono가 "~"로 구간을 합친다)
    private static final Pattern SHORT_RANGE = JsRegex.compile("(" + PERIOD + ")?\\s*(" + HOUR_WORD + ")\\s*~\\s*(("
            + HOUR_WORD + ")\\s*시)");
    // "15:30", "3:00", "오후 3:30"
    private static final Pattern COLON = JsRegex.compile("(?:(" + PERIOD + ")\\s*)?(?<!\\d)(\\d{1,2}):(\\d{2})(?!\\d)");
    // 단독 시간 단어. "낮"은 "낮다"와 겹쳐서 뒤에 공백이나 조사가 올 때만
    private static final Pattern WORD = JsRegex.compile("(정오|자정|점심|아침|저녁|밤(?!새|낮)|퇴근|낮(?=[\\s에,]|$))(?!\\s*\\d)");
    // "내일 오후", "오전에"는 9시, 15시 (규칙 25). 뒤에 조사, 공백, 끝만 허용하고
    // "오전반", "오후 반차", "오전오후"처럼 다른 낱말이 붙으면 잡지 않는다
    private static final Pattern HALF_WORD = JsRegex.compile(
            "(?<![가-힣])(오전|오후)(?=(?:에는|에|엔|은|는|도|쯤|께)?(?:[\\s,.!?~]|$))(?!\\s*(?:반|오전|오후|\\d))");
    private static final Map<String, Integer> WORD_HOUR = Map.of("정오", 12, "자정", 0, "점심", 12, "아침", 8, "저녁", 19,
            "밤", 21, "퇴근", 18, "낮", 12, "오전", 9, "오후", 15);
    // "내일 이 시간", "다음 주 이 시간"은 기준 시각의 시와 분. "같이 시간 내자"는 잡지 않는다
    private static final Pattern SAME_TIME = JsRegex.compile("(?<![가-힣])이\\s*시간(?!대|\\s*(?:뒤|후|전|동안|이내|안에))");

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
        shortRange(text, out, taken);
        before(text, out, taken);
        halfDay(text, out, taken);
        ampm(text, out, taken);
        clock(text, out, taken);
        colon(text, out, taken);
        sameTime(text, out, taken);
        halfWord(text, out, taken);
        word(text, out, taken);
        return out;
    }

    private static Components time(int hour, int minute, boolean meridiem) {
        Components c = new Components();
        c.hour = hour;
        c.minute = minute;
        c.meridiem = meridiem;
        return c;
    }

    private static void shortRange(String text, List<Fragment> out, Taken taken) {
        for (Matcher m = SHORT_RANGE.matcher(text); m.find(); ) {
            Long a = KoreanNumber.parse(m.group(2));
            Long b = KoreanNumber.parse(m.group(4));
            if (a == null || b == null || a > 24 || b > 24 || !taken.take(m.start(), m.end())) {
                continue;
            }
            String whole = m.group();
            String startText = JsRegex.trimEnd(whole.substring(0, whole.indexOf('~')));
            out.add(new Fragment(startText, m.start(),
                    time(applyMeridiem(m.group(1), a.intValue()), 0, m.group(1) != null)));
            out.add(new Fragment(m.group(3), m.start(3), time(b == 24 ? 0 : b.intValue(), 0, false)));
        }
    }

    private static void before(String text, List<Fragment> out, Taken taken) {
        for (Matcher m = BEFORE.matcher(text); m.find(); ) {
            Long raw = KoreanNumber.parse(m.group(2));
            int minutes = Integer.parseInt(m.group(3));
            if (raw == null || raw > 24 || minutes > 59 || !taken.take(m.start(), m.end())) {
                continue;
            }
            int total = applyMeridiem(m.group(1), raw.intValue()) * 60 - minutes;
            // 원본처럼 내림 나눗셈과 부호가 남는 나머지 ("0시 10분 전"은 -1시 -10분, resolve가 전날로 굴린다)
            out.add(new Fragment(m.group(), m.start(), time(Math.floorDiv(total, 60), total % 60, m.group(1) != null)));
        }
    }

    private static void halfDay(String text, List<Fragment> out, Taken taken) {
        for (Matcher m = HALF_DAY.matcher(text); m.find(); ) {
            if (taken.take(m.start(), m.end())) {
                Components c = time(m.group(1).equals("오전") ? 12 : 18, 0, true);
                c.isEnd = true;
                out.add(Fragment.of(m, c));
            }
        }
    }

    private static void ampm(String text, List<Fragment> out, Taken taken) {
        for (Matcher m = AMPM.matcher(text); m.find(); ) {
            int raw = Integer.parseInt(m.group(1));
            if (raw < 1 || raw > 12 || !taken.take(m.start(), m.end())) {
                continue;
            }
            int hour = applyMeridiem(m.group(3).equalsIgnoreCase("p") ? "오후" : "오전", raw);
            int minute = m.group(2) != null ? Integer.parseInt(m.group(2)) : 0;
            out.add(Fragment.of(m, time(hour, minute, true)));
        }
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

    private static void colon(String text, List<Fragment> out, Taken taken) {
        for (Matcher m = COLON.matcher(text); m.find(); ) {
            int raw = Integer.parseInt(m.group(2));
            int minute = Integer.parseInt(m.group(3));
            if (raw > 23 || minute > 59 || !taken.take(m.start(), m.end())) {
                continue;
            }
            out.add(Fragment.of(m, time(applyMeridiem(m.group(1), raw), minute, true)));
        }
    }

    private static void sameTime(String text, List<Fragment> out, Taken taken) {
        for (Matcher m = SAME_TIME.matcher(text); m.find(); ) {
            if (taken.take(m.start(), m.end())) {
                Components c = new Components();
                c.sameTime = true;
                out.add(Fragment.of(m, c));
            }
        }
    }

    private static void halfWord(String text, List<Fragment> out, Taken taken) {
        for (Matcher m = HALF_WORD.matcher(text); m.find(); ) {
            if (taken.take(m.start(), m.end())) {
                Components c = time(WORD_HOUR.get(m.group(1)), 0, true);
                c.approximate = true;
                out.add(Fragment.of(m, c));
            }
        }
    }

    private static void word(String text, List<Fragment> out, Taken taken) {
        for (Matcher m = WORD.matcher(text); m.find(); ) {
            if (!taken.take(m.start(), m.end())) {
                continue;
            }
            Components c = time(WORD_HOUR.get(m.group(1)), 0, true);
            if (m.group(1).equals("자정")) {
                c.midnight = true;
            } else if (!m.group(1).equals("정오")) {
                // 정오와 자정은 정확한 시각, 나머지(아침, 저녁, 밤, 점심, 낮, 퇴근)는 관례
                c.approximate = true;
            }
            out.add(Fragment.of(m, c));
        }
    }
}
