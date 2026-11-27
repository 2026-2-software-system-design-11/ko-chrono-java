package kochrono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 공휴일과 기념일 */
final class HolidayParser {
    /** 양력 고정 공휴일과 기념일의 월, 일. 여러 단어는 띄어쓰기를 생략해도 된다. 원본의 순서를 지킨다 */
    private static final Map<String, int[]> FIXED = new LinkedHashMap<>();

    static {
        FIXED.put("크리스마스 이브", new int[] {12, 24});
        FIXED.put("크리스마스", new int[] {12, 25});
        FIXED.put("성탄절", new int[] {12, 25});
        FIXED.put("신정", new int[] {1, 1});
        FIXED.put("새해 첫날", new int[] {1, 1});
        FIXED.put("삼일절", new int[] {3, 1});
        FIXED.put("근로자의 날", new int[] {5, 1});
        FIXED.put("노동절", new int[] {5, 1});
        FIXED.put("어린이날", new int[] {5, 5});
        FIXED.put("어버이날", new int[] {5, 8});
        FIXED.put("스승의 날", new int[] {5, 15});
        FIXED.put("현충일", new int[] {6, 6});
        FIXED.put("제헌절", new int[] {7, 17});
        FIXED.put("광복절", new int[] {8, 15});
        FIXED.put("개천절", new int[] {10, 3});
        FIXED.put("한글날", new int[] {10, 9});
        FIXED.put("할로윈", new int[] {10, 31});
        FIXED.put("핼러윈", new int[] {10, 31});
        FIXED.put("빼빼로데이", new int[] {11, 11});
        FIXED.put("발렌타인데이", new int[] {2, 14});
        FIXED.put("밸런타인데이", new int[] {2, 14});
        FIXED.put("화이트데이", new int[] {3, 14});
    }

    private static final Map<String, Long> SHIFT = Map.of("전날", -1L, "다음날", 1L, "이튿날", 1L, "당일", 0L);
    private static final String MODIFIER = "(?:\\s*(전날|다음\\s*날|이튿날|당일))?";

    // "크리스마스", "광복절 전날", "스승의날"
    private static final Pattern FIXED_RE = JsRegex.compile(
            "(" + String.join("|", FIXED.keySet().stream().map(k -> k.replace(" ", "\\s*")).toList()) + ")" + MODIFIER);

    private HolidayParser() {
    }

    private static int[] fixed(String name) {
        String key = JsRegex.removeSpaces(name);
        for (Map.Entry<String, int[]> e : FIXED.entrySet()) {
            if (e.getKey().replace(" ", "").equals(key)) {
                return e.getValue();
            }
        }
        throw new IllegalStateException(name);
    }

    private static long shiftOf(String modifier) {
        return modifier == null ? 0 : SHIFT.get(JsRegex.removeSpaces(modifier));
    }

    static List<Fragment> parse(String text) {
        List<Fragment> out = new ArrayList<>();
        for (Matcher m = FIXED_RE.matcher(text); m.find(); ) {
            int[] md = fixed(m.group(1));
            Components c = new Components();
            c.month = md[0];
            c.day = md[1];
            long shift = shiftOf(m.group(2));
            if (shift != 0) {
                c.dayOffset = shift;
            }
            out.add(Fragment.of(m, c));
        }
        return out;
    }
}
