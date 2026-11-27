package kochrono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** parseEvent의 제목 분리: 날짜 부분과 거기 붙은 조사를 뺀다 (규칙 28) */
final class EventTitle {
    // 날짜 바로 뒤에 붙은 조사: "3시에", "모레까지는", "주말엔"
    private static final Pattern TRAILING_PARTICLE = JsRegex.compile(
            "^(?:에는|에도|에|엔|은|는|도|까지는|까지|부터|쯤|께|경)(?=[\\s,.!?]|$)");
    private static final Pattern SPACES = JsRegex.compile("\\s+");
    private static final Pattern SPACE_BEFORE_MARK = JsRegex.compile("\\s+([,.!?])");
    private static final Pattern EDGES = JsRegex.compile("^[\\s,]+|[\\s,]+$");

    private EventTitle() {
    }

    /** 장소("강남역에서")나 동사("만나기")는 그대로 남긴다 */
    static ParsedEvent toEvent(String text, List<ParseResult> results) {
        String rest = text;
        // 뒤에서부터 지워야 앞쪽 index가 어긋나지 않는다
        List<ParseResult> sorted = new ArrayList<>(results);
        sorted.sort(Comparator.comparingInt(ParseResult::index).reversed());
        for (ParseResult r : sorted) {
            String after = Texts.slice(rest, r.index() + r.text().length());
            Matcher particle = TRAILING_PARTICLE.matcher(after);
            int skip = particle.find() ? particle.group().length() : 0;
            rest = Texts.slice(rest, 0, r.index()) + " " + after.substring(skip);
        }
        String title = SPACES.matcher(rest).replaceAll(" ");
        title = SPACE_BEFORE_MARK.matcher(title).replaceAll("$1");
        title = EDGES.matcher(title).replaceAll("");
        if (results.isEmpty()) {
            return new ParsedEvent(title, null, null, false, null, null, null);
        }
        ParseResult first = results.get(0);
        return new ParsedEvent(title, first.date(), first.end(), first.hasTime(), first.recurrence(), first.ambiguity(),
                first.tense());
    }
}
