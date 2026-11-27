package kochrono;

import static kochrono.ResolveFixtures.OPTS;
import static kochrono.ResolveFixtures.REF;
import static kochrono.ResolveFixtures.at;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class PipelineTest {
    private static List<LocalDateTime> dates(String text) {
        return Pipeline.run(text, OPTS).stream().map(r -> r.date).toList();
    }

    @Test void 모든_파서의_조각을_묶어_계산한다() {
        assertEquals(List.of(at(2026, 11, 4, 19)), dates("그럼 다음주 수요일 저녁 7시에 강남에서 볼까?"));
        assertEquals(List.of(at(2026, 10, 31, 10), at(2026, 11, 1, 14)), dates("내일 10시 아니면 모레 2시"));
        assertEquals(List.of(), dates("안녕하세요"));
    }

    @Test void 글자와_위치는_공백을_뺀_원문() {
        LocalResult r = Pipeline.run("회의는 오후 3시", OPTS).get(0);
        assertEquals("오후 3시", r.text);
        assertEquals(4, r.index);
    }

    @Test void 까지와_내내() {
        LocalResult r = Pipeline.run("이번달 말까지 제출", OPTS).get(0);
        assertEquals("이번달 말까지", r.text);
        assertEquals(List.of(REF, at(2026, 10, 31, 23, 59)), List.of(r.date, r.end));
        assertEquals(at(2026, 11, 1, 23, 59), Pipeline.run("이번 주 내내", OPTS).get(0).end);
    }
}
