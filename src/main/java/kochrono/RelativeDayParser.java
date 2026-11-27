package kochrono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 오늘, 내일, 모레, 글피, 어제, 그저께 */
final class RelativeDayParser {
    private static final Map<String, Long> WORDS = Map.ofEntries(
            Map.entry("그저께", -2L), Map.entry("금일", 0L), Map.entry("명일", 1L), Map.entry("익일", 1L),
            Map.entry("작일", -1L), Map.entry("내일모레", 2L), Map.entry("낼모레", 2L), Map.entry("낼", 1L),
            Map.entry("그제", -2L), Map.entry("어제", -1L), Map.entry("오늘", 0L), Map.entry("내일", 1L),
            Map.entry("모레", 2L), Map.entry("글피", 3L));

    // "금일", "익일 배송", "작일" (업무용 한자어). "내일모레"는 "내일"과 "모레"가 아니라 모레.
    // "낼"은 "내가 낼게"(내다)와 겹쳐서 뒤에 시각이나 약속 표현이 올 때만
    private static final Pattern RE = JsRegex.compile(
            "(그저께|그제|어제|오늘|내일모레|낼모레|내일|모레|글피|(?<![가-힣])(?:금일|명일|익일|작일)"
                    + "|(?<![가-힣])낼(?=\\s*(?:오전|오후|아침|점심|저녁|밤|새벽|낮|\\d|봐|보자|만나|까지)))");

    private RelativeDayParser() {
    }

    static List<Fragment> parse(String text) {
        List<Fragment> out = new ArrayList<>();
        for (Matcher m = RE.matcher(text); m.find(); ) {
            Components c = new Components();
            c.dayOffset = WORDS.get(m.group(1));
            out.add(Fragment.of(m, c));
        }
        return out;
    }
}
