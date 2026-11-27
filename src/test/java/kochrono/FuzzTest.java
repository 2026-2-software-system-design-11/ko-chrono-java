package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 원본 tests/fuzz.test.ts: 무작위 문장으로 "절대 죽지 않기"를 확인한다. 기대값이 아니라 항상 지켜야 하는 성질(불변식)만 본다.
 * 원본과 같은 난수 생성기(Mulberry32)와 시드를 써서 같은 3000개 문장을 만든다.
 */
@DisplayName("무작위 문장 불변식")
class FuzzTest {
    private static final List<String> PIECES = List.of(
            // 날짜, 시간 조각
            "내일", "모레", "어제", "오늘", "다음주", "지난주", "이번 주", "담주", "월요일", "금욜", "월", "수", "주말", "평일",
            "오전", "오후", "새벽", "저녁", "밤", "정오", "자정", "3시", "12시", "24시", "25시", "0시", "세 시", "열두시", "반",
            "15:30", "99:99", "3pm", "11월", "13월", "31일", "0일", "2월 30일", "11/5", "26.11.5", "2027-02-29", "2028년",
            "3일", "이틀", "2주", "한 달", "1년", "30분", "한 시간 반", "뒤", "후", "전", "이내", "까지", "부터", "~", "간", "동안",
            "매주", "매달", "격주", "매일", "마다", "첫째 주", "마지막 주", "이맘때", "이 시간", "크리스마스", "설날", "추석", "설 연휴",
            "전날", "다음날", "음력", "윤", "금주", "차주", "전주", "익월", "금일", "익일", "말", "초", "중순", "연말", "내년", "작년", "쯤", "경",
            // 조사, 잡음
            "에", "은", "는", "도", "이나", "아니면", ",", ".", "?", "!", " ", "  ", "(", ")", "(화)", "/", "-", ":",
            "회의", "치과", "보고서", "설명", "설거지", "내가 낼게", "3명", "1월생", "오일", "ㅋㅋ", "😀", "abc", "123456", "");

    private static String sentence(Mulberry32 next) {
        int n = 1 + (int) Math.floor(next.next() * 8);
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < n; i++) {
            s.append(PIECES.get((int) Math.floor(next.next() * PIECES.size()))).append(next.next() < 0.6 ? " " : "");
        }
        return s.toString();
    }

    private static final List<String> INPUTS = inputs();

    private static List<String> inputs() {
        Mulberry32 next = new Mulberry32(20261030);
        List<String> out = new ArrayList<>();
        for (int i = 0; i < 3000; i++) {
            out.add(sentence(next));
        }
        return out;
    }

    /** 원본의 valid: Date로 나타낼 수 있는 시점 */
    private static boolean valid(ZonedDateTime d) {
        return d != null && Math.abs(d.toInstant().getEpochSecond()) <= 8_640_000_000_000L;
    }

    @Test @DisplayName("3000개 문장에서 에러 없이, 결과는 항상 일관된다") void c1() {
        List<String> bad = new ArrayList<>();
        for (String text : INPUTS) {
            try {
                for (ParseResult r : Fixtures.parseAll(text, Fixtures.REF)) {
                    if (!valid(r.date())) {
                        bad.add("Invalid date: " + text);
                    }
                    if (r.end() != null && !valid(r.end())) {
                        bad.add("Invalid end: " + text);
                    }
                    if (r.ambiguity() != null && !r.ambiguity().alternatives().stream().allMatch(FuzzTest::valid)) {
                        bad.add("Invalid alternative: " + text);
                    }
                    if (r.end() != null && r.end().isBefore(r.date())) {
                        bad.add("end < date: " + text + " " + r.date() + " ~ " + r.end());
                    }
                    if (!Texts.slice(text, r.index(), r.index() + r.text().length()).equals(r.text())) {
                        bad.add("text/index 불일치: " + text + " index=" + r.index() + " text=" + r.text());
                    }
                    if (!r.text().equals(JsRegex.trim(r.text())) || r.text().isEmpty()) {
                        bad.add("text 공백/빈값: " + text);
                    }
                }
                ParsedEvent e = KoChrono.parseEvent(text, Fixtures.at(Fixtures.REF));
                if (e.date() != null && !valid(e.date())) {
                    bad.add("parseEvent invalid: " + text);
                }
            } catch (RuntimeException err) {
                bad.add("throw: " + text + " " + err);
            }
        }
        assertEquals(List.of(), bad.subList(0, Math.min(20, bad.size())));
    }

    @Test @DisplayName("timezone 옵션에서도 에러가 없다") void c2() {
        for (String text : INPUTS.subList(0, 500)) {
            for (ParseResult r : KoChrono.parseAll(text, Fixtures.at(Fixtures.REF).withTimezone("America/New_York"))) {
                assertTrue(valid(r.date()), text);
            }
        }
    }
}
