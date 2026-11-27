package kochrono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 공휴일, 기념일, 명절. 음력 명절의 양력 날짜는 resolve가 음력 표에서 찾는다 */
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
    // "설날", "구정", "설 연휴", "추석 전날", "한가위", "부처님 오신 날".
    // "설"만은 "설명", "설거지"와 겹쳐서 뒤에 연휴, 전날 등이 올 때만
    private static final Pattern LUNAR_RE = JsRegex.compile(
            "(설날|구정|설(?=\\s*(?:연휴|전날|다음\\s*날|당일))|추석|한가위|부처님\\s*오신\\s*날|석가\\s*탄신일|석탄일|초파일)"
                    + "(?:\\s*(연휴))?(?:\\s*(전날|다음\\s*날|이튿날|당일))?");
    // "음력 3월 3일", "음력 윤5월 1일" (음력이라고 말한 경우만)
    private static final Pattern LUNAR_DATE_RE = JsRegex.compile("음력\\s*(윤\\s*)?(\\d{1,2})\\s*월\\s*(\\d{1,2})\\s*일");

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
        for (Matcher m = LUNAR_DATE_RE.matcher(text); m.find(); ) {
            int month = Integer.parseInt(m.group(2));
            int day = Integer.parseInt(m.group(3));
            if (month >= 1 && month <= 12 && day >= 1 && day <= 30) {
                Components c = new Components();
                c.lunarDate = new Components.LunarDate(month, day, m.group(1) != null);
                out.add(Fragment.of(m, c));
            }
        }
        for (Matcher m = LUNAR_RE.matcher(text); m.find(); ) {
            String name = JsRegex.removeSpaces(m.group(1));
            Components c = new Components();
            c.lunar = switch (name) {
                case "설날", "구정", "설" -> Components.LunarHoliday.SEOLLAL;
                case "추석", "한가위" -> Components.LunarHoliday.CHUSEOK;
                default -> Components.LunarHoliday.BUDDHA;
            };
            // "설 연휴", "추석 연휴"는 전날부터 다음날까지 3일 (부처님 오신 날은 연휴가 없다)
            if (m.group(2) != null && c.lunar != Components.LunarHoliday.BUDDHA) {
                c.dayOffset = -1L;
                c.durationDays = 3L;
            } else {
                long shift = shiftOf(m.group(3));
                if (shift != 0) {
                    c.dayOffset = shift;
                }
            }
            out.add(Fragment.of(m, c));
        }
        return out;
    }
}
