package kochrono;

import static kochrono.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.*;

/** 원본 tests/parse.test.ts (과거형 문장 (규칙 37)). 기준: 2026-10-30 (금) 14:00 */
class Parse17Test {
    @Nested @DisplayName("과거형 문장 (규칙 37)")
    class G44 {
        Map<String, Object> past(String s) {
            ParseResult r = p(s);
            return r == null ? null : obj("date", r.date(), "tense", r.tense());
        }

        Map<String, Object> on(int y, int m, int day, int... hm) {
            return obj("date", d(y, m, day, hm), "tense", Tense.PAST);
        }

        Map<String, Object> future(int y, int m, int day, int... hm) {
            return obj("date", d(y, m, day, hm));
        }
        // 시각만: 아직 안 온 시각이면 어제 (규칙 4의 반대 방향)
        @Test @DisplayName("새벽 3시에 깨서 못 잤어") void c429() { assertEquals(on(2026, 10, 30, 3), past("새벽 3시에 깨서 못 잤어")); }
        @Test @DisplayName("3시에 회의했어") void c430() {
            assertEquals(on(2026, 10, 29, 15), past("3시에 회의했어"));
            assertEquals(amb("meridiem", d(2026, 10, 30, 3)), p("3시에 회의했어").ambiguity());
        }
        @Test @DisplayName("1시에 점심 먹었어") void c431() { assertEquals(on(2026, 10, 30, 13), past("1시에 점심 먹었어")); }
        @Test @DisplayName("9시에 출근했어") void c432() { assertEquals(on(2026, 10, 30, 9), past("9시에 출근했어")); }
        @Test @DisplayName("저녁 7시에 만났어") void c433() { assertEquals(on(2026, 10, 29, 19), past("저녁 7시에 만났어")); }
        @Test @DisplayName("아침에 커피 마셨어") void c434() { assertEquals(on(2026, 10, 30, 8), past("아침에 커피 마셨어")); }
        @Test @DisplayName("3시에 일어났어") void c435() { assertEquals(on(2026, 10, 29, 15), past("3시에 일어났어")); }
        @Test @DisplayName("2시에 도착했습니다") void c436() { assertEquals(on(2026, 10, 30, 14), past("2시에 도착했습니다")); }
        @Test @DisplayName("5시에 끝났는데 ㅠㅠ") void c437() { assertEquals(on(2026, 10, 29, 17), past("5시에 끝났는데 ㅠㅠ")); }
        @Test @DisplayName("3시에 혼자 갔어") void c438() { assertEquals(on(2026, 10, 29, 15), past("3시에 혼자 갔어")); }
        @Test @DisplayName("3시에 만났어?") void c439() { assertEquals(on(2026, 10, 29, 15), past("3시에 만났어?")); }
        // 연도 없는 월일: 아직 안 온 날이면 작년 (규칙 5, 13의 반대 방향)
        @Test @DisplayName("12월 1일에 이사했었어") void c440() { assertEquals(on(2025, 12, 1), past("12월 1일에 이사했었어")); }
        @Test @DisplayName("11월 5일에 갔어") void c441() { assertEquals(on(2025, 11, 5), past("11월 5일에 갔어")); }
        @Test @DisplayName("10월 3일에 갔어") void c442() { assertEquals(on(2026, 10, 3), past("10월 3일에 갔어")); }
        @Test @DisplayName("10월 30일에 갔어 (오늘)") void c443() { assertEquals(on(2026, 10, 30), past("10월 30일에 갔어")); }
        @Test @DisplayName("크리스마스에 만났어") void c444() { assertEquals(on(2025, 12, 25), past("크리스마스에 만났어")); }
        @Test @DisplayName("12월에 다녀왔어") void c445() { assertEquals(on(2025, 12, 1), past("12월에 다녀왔어")); }
        @Test @DisplayName("추석에 고향 갔어") void c446() { assertEquals(on(2026, 9, 25), past("추석에 고향 갔어")); }
        @Test @DisplayName("2월 29일에 갔어 (작년에 없는 날)") void c447() { assertNull(past("2월 29일에 갔어")); }
        // 요일, 일 단독: 오늘 포함 가장 가까운 과거 (규칙 3, 35의 반대 방향)
        @Test @DisplayName("월요일에 만났어") void c448() { assertEquals(on(2026, 10, 26), past("월요일에 만났어")); }
        @Test @DisplayName("금요일에 제출했어 (오늘)") void c449() { assertEquals(on(2026, 10, 30), past("금요일에 제출했어")); }
        @Test @DisplayName("금요일 5시에 제출했어 (오늘 5시는 아직)") void c450() { assertEquals(on(2026, 10, 23, 17), past("금요일 5시에 제출했어")); }
        @Test @DisplayName("토요일 저녁에 봤어") void c451() { assertEquals(on(2026, 10, 24, 19), past("토요일 저녁에 봤어")); }
        @Test @DisplayName("주말에 놀았어") void c452() { assertEquals(on(2026, 10, 24), past("주말에 놀았어")); }
        @Test @DisplayName("5일에 갔어") void c453() { assertEquals(on(2026, 10, 5), past("5일에 갔어")); }
        @Test @DisplayName("30일에 갔어 (오늘)") void c454() { assertEquals(on(2026, 10, 30), past("30일에 갔어")); }
        @Test @DisplayName("31일에 갔어 (9월엔 31일이 없음)") void c455() { assertNull(past("31일에 갔어")); }
        // 날짜가 정해진 표현은 그대로, 표시만 붙는다
        @Test @DisplayName("어제 3시에 만났어") void c456() { assertEquals(on(2026, 10, 29, 15), past("어제 3시에 만났어")); }
        @Test @DisplayName("오늘 아침에 늦잠 잤어") void c457() { assertEquals(on(2026, 10, 30, 8), past("오늘 아침에 늦잠 잤어")); }
        @Test @DisplayName("지난주 금요일에 봤어") void c458() { assertEquals(on(2026, 10, 23), past("지난주 금요일에 봤어")); }
        @Test @DisplayName("2025년 5월 1일에 입사했어") void c459() { assertEquals(on(2025, 5, 1), past("2025년 5월 1일에 입사했어")); }
        // 과거형처럼 보여도 미래 일정: 바꾸지 않는다 (규칙 10)
        @Test @DisplayName("3시에 보자") void c460() { assertEquals(future(2026, 10, 30, 15), past("3시에 보자")); }
        @Test @DisplayName("3시에 만나기로 했어") void c461() { assertEquals(future(2026, 10, 30, 15), past("3시에 만나기로 했어")); }
        @Test @DisplayName("3시에 온다고 했어") void c462() { assertEquals(future(2026, 10, 30, 15), past("3시에 온다고 했어")); }
        @Test @DisplayName("3시에 온댔어") void c463() { assertEquals(future(2026, 10, 30, 15), past("3시에 온댔어")); }
        @Test @DisplayName("3시에 갔다 올게") void c464() { assertEquals(future(2026, 10, 30, 15), past("3시에 갔다 올게")); }
        @Test @DisplayName("3시에 끝났으면 좋겠다") void c465() { assertEquals(future(2026, 10, 30, 15), past("3시에 끝났으면 좋겠다")); }
        @Test @DisplayName("3시로 예약했어") void c466() { assertEquals(future(2026, 10, 30, 15), past("3시로 예약했어")); }
        @Test @DisplayName("월요일로 잡았어") void c467() { assertEquals(future(2026, 11, 2), past("월요일로 잡았어")); }
        @Test @DisplayName("3시에 미팅 잡혔어") void c468() { assertEquals(future(2026, 10, 30, 15), past("3시에 미팅 잡혔어")); }
        @Test @DisplayName("3시에 자려고 했는데") void c469() { assertEquals(future(2026, 10, 30, 15), past("3시에 자려고 했는데")); }
        @Test @DisplayName("금요일에 냈어야 했는데") void c470() { assertEquals(future(2026, 10, 30), past("금요일에 냈어야 했는데")); }
        @Test @DisplayName("3시 회의 깜빡했어") void c471() { assertEquals(future(2026, 10, 30, 15), past("3시 회의 깜빡했어")); }
        @Test @DisplayName("3시에 회의 있어") void c472() { assertEquals(future(2026, 10, 30, 15), past("3시에 회의 있어")); }
        @Test @DisplayName("3시에 하면 재밌어") void c473() { assertEquals(future(2026, 10, 30, 15), past("3시에 하면 재밌어")); }
        @Test @DisplayName("3시에 봐 저번엔 늦었잖아") void c474() { assertEquals(future(2026, 10, 30, 15), past("3시에 봐 저번엔 늦었잖아")); }
        @Test @DisplayName("3시에 만나자. 어제는 미안했어") void c475() { assertEquals(future(2026, 10, 30, 15), past("3시에 만나자. 어제는 미안했어")); }
        @Test @DisplayName("내일 3시라고 했잖아") void c476() { assertEquals(future(2026, 10, 31, 15), past("내일 3시라고 했잖아")); }
        @Test @DisplayName("3시까지 보내 달라고 했어") void c477() {
            ParseResult r = p("3시까지 보내 달라고 했어");
            assertEquals(Arrays.asList(d(2026, 10, 30, 15), null), Arrays.asList(r.end(), r.tense()));
        }
        @Test @DisplayName("매주 월요일에 운동했어 (반복은 그대로)") void c478() {
            ParseResult r = p("매주 월요일에 운동했어");
            assertEquals(Arrays.asList(d(2026, 11, 2), "FREQ=WEEKLY;BYDAY=MO", null), Arrays.asList(r.date(), r.recurrence().rrule(), r.tense()));
        }
        @Test @DisplayName("앞 문장만 과거 (3시에 일어났어 5시에 보자)") void c479() {
            List<ParseResult> rs = parseAll("3시에 일어났어 5시에 보자", REF);
            assertEquals(Arrays.asList(Arrays.asList(d(2026, 10, 29, 15), Tense.PAST), Arrays.asList(d(2026, 10, 30, 17), null)), rs.stream().map(r -> Arrays.asList(r.date(), r.tense())).toList());
        }
        @Test @DisplayName("시간대 옵션에서도 (Asia/Seoul)") void c480() {
            ParseResult r = parse("새벽 3시에 깨서 못 잤어", utc(2026, 10, 30, 5), "Asia/Seoul");
            assertEquals(Arrays.asList(utc(2026, 10, 29, 18).toInstant(), Tense.PAST), Arrays.asList(r.date().toInstant(), r.tense()));
        }
    }
}
