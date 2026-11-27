package kochrono;

import static java.time.DayOfWeek.*;
import static kochrono.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.*;

/** 원본 tests/parse.test.ts (반복 일정의 시작, 끝 (규칙 34)). 기준: 2026-10-30 (금) 14:00 */
class Parse13Test {
    @Nested @DisplayName("반복 일정의 시작, 끝 (규칙 34)")
    class G36 {
        Map<String, Object> rec(String s) {
            ParseResult r = p(s);
            return r == null ? obj()
                    : obj("date", r.date(), "end", r.end(), "rrule", rrule(r), "text", r.text());
        }

        // 끝: "~까지" 마감이 앞이나 뒤에 붙으면 UNTIL
        @Test @DisplayName("다음달까지 매주 월요일 9시") void c330() {
            same(obj("date", d(2026, 11, 2, 9), "end", null, "rrule", "FREQ=WEEKLY;BYDAY=MO;UNTIL=20261130T235900", "text", "다음달까지 매주 월요일 9시"), rec("다음달까지 매주 월요일 9시"));
        }
        @Test @DisplayName("매주 월요일 9시 12월 20일까지") void c331() {
            matches(obj("date", d(2026, 11, 2, 9), "rrule", "FREQ=WEEKLY;BYDAY=MO;UNTIL=20261220T235900", "text", "매주 월요일 9시 12월 20일까지"), rec("매주 월요일 9시 12월 20일까지"));
        }
        @Test @DisplayName("12월 말까지 매일 아침 8시") void c332() {
            matches(obj("date", d(2026, 10, 31, 8), "rrule", "FREQ=DAILY;UNTIL=20261231T235900"), rec("12월 말까지 매일 아침 8시"));
        }
        // 시각이 없으면 UNTIL도 날짜만 (DTSTART가 DATE일 때 RFC 5545가 요구하는 형식)
        @Test @DisplayName("이번달까지 매일") void c333() { matches(obj("rrule", "FREQ=DAILY;UNTIL=20261031"), rec("이번달까지 매일")); }
        // 시작: "A부터"가 앞에 붙으면 A 이후 첫 발생
        @Test @DisplayName("다다음주부터 매주 월요일") void c334() {
            matches(obj("date", d(2026, 11, 9), "rrule", "FREQ=WEEKLY;BYDAY=MO", "text", "다다음주부터 매주 월요일"), rec("다다음주부터 매주 월요일"));
        }
        @Test @DisplayName("11월 1일부터 12월 31일까지 매주 토요일") void c335() {
            matches(obj("date", d(2026, 11, 7), "rrule", "FREQ=WEEKLY;BYDAY=SA;UNTIL=20261231", "text", "11월 1일부터 12월 31일까지 매주 토요일"), rec("11월 1일부터 12월 31일까지 매주 토요일"));
        }
        // 기간: 시작일(없으면 오늘)부터 그 기간의 마지막 날까지
        @Test @DisplayName("10주 동안 매주 월요일") void c336() {
            matches(obj("date", d(2026, 11, 2), "rrule", "FREQ=WEEKLY;BYDAY=MO;UNTIL=20270107"), rec("10주 동안 매주 월요일"));
        }
        @Test @DisplayName("3달 동안 매주 화요일 저녁 7시") void c337() {
            matches(obj("date", d(2026, 11, 3, 19), "rrule", "FREQ=WEEKLY;BYDAY=TU;UNTIL=20270129T235900"), rec("3달 동안 매주 화요일 저녁 7시"));
        }
        // 횟수: "총 N번", "N회 반복"처럼 전체 횟수인 게 분명할 때만
        @Test @DisplayName("매주 월요일 10번 반복") void c338() {
            matches(obj("rrule", "FREQ=WEEKLY;BYDAY=MO;COUNT=10", "text", "매주 월요일 10번 반복"), rec("매주 월요일 10번 반복"));
        }
        @Test @DisplayName("매주 수요일 오후 2시, 총 8회") void c339() {
            matches(obj("date", d(2026, 11, 4, 14), "rrule", "FREQ=WEEKLY;BYDAY=WE;COUNT=8", "text", "매주 수요일 오후 2시, 총 8회"), rec("매주 수요일 오후 2시, 총 8회"));
        }
        @Test @DisplayName("격주 금요일 열 번") void c340() {
            matches(obj("rrule", "FREQ=WEEKLY;INTERVAL=2;BYDAY=FR"), rec("격주 금요일 열 번"));
        }
        @Test @DisplayName("매일 3번 약 먹기 (하루 횟수, COUNT 아님)") void c341() { matches(obj("rrule", "FREQ=DAILY", "text", "매일"), rec("매일 3번 약 먹기")); }
        // 시간 범위: end는 첫 회차의 끝
        @Test @DisplayName("매주 화목 오후 3~5시") void c342() {
            matches(obj("date", d(2026, 11, 3, 15), "end", d(2026, 11, 3, 17), "rrule", "FREQ=WEEKLY;BYDAY=TU,TH"), rec("매주 화목 오후 3~5시"));
        }
        @Test @DisplayName("다음달까지 매주 화목 3시부터 5시까지") void c343() {
            matches(obj("date", d(2026, 11, 3, 15), "end", d(2026, 11, 3, 17), "rrule", "FREQ=WEEKLY;BYDAY=TU,TH;UNTIL=20261130T235900"), rec("다음달까지 매주 화목 3시부터 5시까지"));
        }
        @Test @DisplayName("다음주부터 10주 동안 매주 월요일") void c344() {
            matches(obj("date", d(2026, 11, 2), "rrule", "FREQ=WEEKLY;BYDAY=MO;UNTIL=20270110"), rec("다음주부터 10주 동안 매주 월요일"));
        }
        @Test @DisplayName("매달 15일 오전 10시 내년 3월 말까지") void c345() {
            matches(obj("date", d(2026, 11, 15, 10), "rrule", "FREQ=MONTHLY;BYMONTHDAY=15;UNTIL=20270331T235900"), rec("매달 15일 오전 10시 내년 3월 말까지"));
        }
        // 시각만 있는 마감은 그날의 끝이라 반복의 끝으로 보지 않는다
        @Test @DisplayName("5시까지 매일 보고") void c346() {
            assertEquals(Arrays.asList(null, "FREQ=DAILY"), parseAll("5시까지 매일 보고", REF).stream().map(Fixtures::rrule).toList());
        }
        // 마감이 첫 발생보다 앞이면 묶지 않는다
        @Test @DisplayName("어제까지 매주 월요일 (지난 마감은 별개)") void c347() {
            List<ParseResult> rs = parseAll("어제까지 매주 월요일", REF);
            assertEquals(Arrays.asList(null, "FREQ=WEEKLY;BYDAY=MO"), rs.stream().map(Fixtures::rrule).toList());
        }
        @Test @DisplayName("구조화된 값") void c348() {
            assertEquals(new Recurrence(Frequency.WEEKLY, 1, List.of(MONDAY), null, null, null, d(2026, 11, 30, 23, 59),
                    null, "FREQ=WEEKLY;BYDAY=MO;UNTIL=20261130T235900"), p("다음달까지 매주 월요일 9시").recurrence());
        }
        @Test @DisplayName("timezone을 주면 until도 실제 시점으로") void c349() {
            ParseResult r = parse("다음달까지 매일 9시", utc(2026, 10, 30, 5), "Asia/Seoul");
            assertEquals(utc(2026, 11, 30, 14, 59).toInstant(), r.recurrence().until().toInstant());
            assertEquals("FREQ=DAILY;UNTIL=20261130T235900", r.recurrence().rrule());
        }
    }
}
