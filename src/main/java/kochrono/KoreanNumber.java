package kochrono;

import java.util.Map;
import java.util.regex.Pattern;

/** "3", "세", "열", "십이", "열두" 같은 수 표현을 숫자로 바꾼다 */
final class KoreanNumber {
    /**
     * 숫자열이 이보다 크면 여기서 멈춘다. 어느 단위(분, 시간, 일)로 더해도 Date의 한도를 넘는 크기라
     * 결과는 같고(날짜를 만들 수 없음), long 곱셈이 넘치지 않는다.
     */
    static final long CAP = 1_000_000_000_000_000L;

    private static final Map<String, Long> NATIVE = Map.ofEntries(
            Map.entry("한", 1L), Map.entry("하나", 1L), Map.entry("두", 2L), Map.entry("둘", 2L),
            Map.entry("세", 3L), Map.entry("셋", 3L), Map.entry("네", 4L), Map.entry("넷", 4L),
            Map.entry("다섯", 5L), Map.entry("여섯", 6L), Map.entry("일곱", 7L), Map.entry("여덟", 8L),
            Map.entry("아홉", 9L), Map.entry("열", 10L), Map.entry("열흘", 10L), Map.entry("보름", 15L),
            Map.entry("스무", 20L), Map.entry("스물", 20L), Map.entry("이틀", 2L), Map.entry("사흘", 3L),
            Map.entry("나흘", 4L), Map.entry("닷새", 5L), Map.entry("엿새", 6L), Map.entry("이레", 7L),
            Map.entry("여드레", 8L), Map.entry("아흐레", 9L));

    private static final Pattern DIGITS = Pattern.compile("\\d+");

    private KoreanNumber() {
    }

    /** 읽지 못하면 null. 고유어와 한자어는 1~99만 다룬다 */
    static Long parse(String s) {
        if (DIGITS.matcher(s).matches()) {
            return digits(s);
        }
        if (NATIVE.containsKey(s)) {
            return NATIVE.get(s);
        }
        // 고유어 합성: 열두, 열다섯, 스무
        for (String tens : new String[] {"스물", "스무", "열"}) {
            if (s.startsWith(tens) && s.length() > tens.length()) {
                Long rest = NATIVE.get(s.substring(tens.length()));
                if (rest != null) {
                    return NATIVE.get(tens) + rest;
                }
            }
        }
        return null;
    }

    /** 숫자열. 아주 긴 숫자열은 CAP에서 멈춘다 */
    static long digits(String s) {
        long n = 0;
        for (int i = 0; i < s.length(); i++) {
            n = n * 10 + (s.charAt(i) - '0');
            if (n >= CAP) {
                return CAP;
            }
        }
        return n;
    }
}
