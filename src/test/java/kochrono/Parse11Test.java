package kochrono;

import static java.time.DayOfWeek.*;
import static kochrono.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.*;

/** 원본 tests/parse.test.ts (반복 일정 (규칙 27)). 기준: 2026-10-30 (금) 14:00 */
class Parse11Test {
    @Nested @DisplayName("반복 일정 (규칙 27)")
    class G34 {
        // date = 기준 시각 이후 첫 발생, recurrence.rrule = RFC 5545 RRULE
        // date는 기준 시각 이후 첫 발생, recurrence.rrule은 RFC 5545 RRULE
        Map<String, Object> rec(String s) {
            ParseResult r = p(s);
            return r == null ? obj() : obj("date", r.date(), "rrule", rrule(r), "text", r.text());
        }

        @Test @DisplayName("매일 아침 8시") void c278() {
            matches(obj("date", d(2026, 10, 31, 8), "rrule", "FREQ=DAILY"), rec("매일 아침 8시"));
        }
        @Test @DisplayName("매일 오후 6시") void c279() {
            matches(obj("date", d(2026, 10, 30, 18), "rrule", "FREQ=DAILY"), rec("매일 오후 6시"));
        }
        @Test @DisplayName("매주 월요일 오전 9시 회의") void c280() {
            same(obj("date", d(2026, 11, 2, 9), "rrule", "FREQ=WEEKLY;BYDAY=MO", "text", "매주 월요일 오전 9시"), rec("매주 월요일 오전 9시 회의"));
        }
        @Test @DisplayName("월요일마다") void c281() {
            matches(obj("date", d(2026, 11, 2), "rrule", "FREQ=WEEKLY;BYDAY=MO"), rec("월요일마다"));
        }
        @Test @DisplayName("매주 월수금 7시") void c282() {
            matches(obj("date", d(2026, 11, 2, 7), "rrule", "FREQ=WEEKLY;BYDAY=MO,WE,FR"), rec("매주 월수금 7시"));
        }
        @Test @DisplayName("매주 화, 목 저녁 8시") void c283() {
            matches(obj("date", d(2026, 11, 3, 20), "rrule", "FREQ=WEEKLY;BYDAY=TU,TH"), rec("매주 화, 목 저녁 8시"));
        }
        @Test @DisplayName("매주 월~금 오전 9시") void c284() {
            matches(obj("date", d(2026, 11, 2, 9), "rrule", "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR"), rec("매주 월~금 오전 9시"));
        }
        @Test @DisplayName("매주 금~일") void c285() {
            matches(obj("date", d(2026, 10, 30), "rrule", "FREQ=WEEKLY;BYDAY=FR,SA,SU"), rec("매주 금~일"));
        }
        @Test @DisplayName("매주 일요일 일정 공유 (일정의 \"일\"은 요일 아님)") void c286() {
            matches(obj("rrule", "FREQ=WEEKLY;BYDAY=SU", "text", "매주 일요일"), rec("매주 일요일 일정 공유"));
        }
        @Test @DisplayName("격주 금요일") void c287() {
            matches(obj("date", d(2026, 10, 30), "rrule", "FREQ=WEEKLY;INTERVAL=2;BYDAY=FR"), rec("격주 금요일"));
        }
        @Test @DisplayName("2주마다 수요일") void c288() {
            matches(obj("date", d(2026, 11, 4), "rrule", "FREQ=WEEKLY;INTERVAL=2;BYDAY=WE"), rec("2주마다 수요일"));
        }
        @Test @DisplayName("평일마다 오전 10시") void c289() {
            matches(obj("date", d(2026, 11, 2, 10), "rrule", "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR"), rec("평일마다 오전 10시"));
        }
        @Test @DisplayName("주말마다") void c290() {
            matches(obj("date", d(2026, 10, 31), "rrule", "FREQ=WEEKLY;BYDAY=SA,SU"), rec("주말마다"));
        }
        @Test @DisplayName("매달 15일") void c291() {
            matches(obj("date", d(2026, 11, 15), "rrule", "FREQ=MONTHLY;BYMONTHDAY=15"), rec("매달 15일"));
        }
        @Test @DisplayName("매월 말일") void c292() {
            matches(obj("date", d(2026, 10, 31), "rrule", "FREQ=MONTHLY;BYMONTHDAY=-1"), rec("매월 말일"));
        }
        @Test @DisplayName("매달 마지막 주 금요일") void c293() {
            matches(obj("date", d(2026, 10, 30), "rrule", "FREQ=MONTHLY;BYDAY=-1FR"), rec("매달 마지막 주 금요일"));
        }
        @Test @DisplayName("매달 첫째 주 월요일") void c294() {
            matches(obj("date", d(2026, 11, 2), "rrule", "FREQ=MONTHLY;BYDAY=1MO"), rec("매달 첫째 주 월요일"));
        }
        @Test @DisplayName("매년 3월 1일") void c295() {
            matches(obj("date", d(2027, 3, 1), "rrule", "FREQ=YEARLY;BYMONTH=3;BYMONTHDAY=1"), rec("매년 3월 1일"));
        }
        @Test @DisplayName("2시간마다") void c296() {
            matches(obj("date", d(2026, 10, 30, 16), "rrule", "FREQ=HOURLY;INTERVAL=2"), rec("2시간마다"));
        }
        @Test @DisplayName("매주 회의 (요일 없음 → 오늘부터 매주)") void c297() {
            matches(obj("date", d(2026, 10, 30), "rrule", "FREQ=WEEKLY"), rec("매주 회의"));
        }
        @Test @DisplayName("반복이 아니면 recurrence 없음") void c298() { assertNull(p("다음주 월요일").recurrence()); }
        @Test @DisplayName("구조화된 값") void c299() {
            assertEquals(new Recurrence(Frequency.WEEKLY, 1, List.of(MONDAY, WEDNESDAY, FRIDAY), null, null, null, null,
                    null, "FREQ=WEEKLY;BYDAY=MO,WE,FR"), p("매주 월수금 7시").recurrence());
        }
    }
}
