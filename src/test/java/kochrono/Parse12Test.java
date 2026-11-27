package kochrono;

import static kochrono.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.*;

/** 원본 tests/parse.test.ts (주기만 있는 반복 (규칙 27, 이슈 #1)). 기준: 2026-10-30 (금) 14:00 */
class Parse12Test {
    @Nested @DisplayName("주기만 있는 반복 (규칙 27, 이슈 #1)")
    class G35 {
        Map<String, Object> rec(String s) {
            ParseResult r = p(s);
            return r == null ? null : obj("date", r.date(), "hasTime", r.hasTime(), "rrule", rrule(r), "text", r.text());
        }

        // 요일, 날짜가 없으면 오늘(시각이 지났으면 내일)부터. 요일, 날짜는 RRULE에 넣지 않고 DTSTART(date)를 따른다
        @Test @DisplayName("매주") void c300() {
            same(obj("date", d(2026, 10, 30), "hasTime", false, "rrule", "FREQ=WEEKLY", "text", "매주"), rec("매주"));
        }
        @Test @DisplayName("격주") void c301() {
            matches(obj("date", d(2026, 10, 30), "rrule", "FREQ=WEEKLY;INTERVAL=2"), rec("격주"));
        }
        @Test @DisplayName("매월") void c302() {
            matches(obj("date", d(2026, 10, 30), "rrule", "FREQ=MONTHLY"), rec("매월"));
        }
        @Test @DisplayName("매달") void c303() {
            matches(obj("date", d(2026, 10, 30), "rrule", "FREQ=MONTHLY"), rec("매달"));
        }
        @Test @DisplayName("매년") void c304() {
            matches(obj("date", d(2026, 10, 30), "rrule", "FREQ=YEARLY"), rec("매년"));
        }
        @Test @DisplayName("해마다") void c305() { matches(obj("rrule", "FREQ=YEARLY"), rec("해마다")); }
        @Test @DisplayName("2개월마다") void c306() {
            matches(obj("date", d(2026, 10, 30), "rrule", "FREQ=MONTHLY;INTERVAL=2"), rec("2개월마다"));
        }
        @Test @DisplayName("두 달마다") void c307() { matches(obj("rrule", "FREQ=MONTHLY;INTERVAL=2"), rec("두 달마다")); }
        @Test @DisplayName("한 달마다") void c308() { matches(obj("rrule", "FREQ=MONTHLY"), rec("한 달마다")); }
        @Test @DisplayName("3년마다") void c309() { matches(obj("rrule", "FREQ=YEARLY;INTERVAL=3"), rec("3년마다")); }
        @Test @DisplayName("일주일마다") void c310() {
            matches(obj("date", d(2026, 10, 30), "rrule", "FREQ=WEEKLY"), rec("일주일마다"));
        }
        @Test @DisplayName("2주마다") void c311() { matches(obj("rrule", "FREQ=WEEKLY;INTERVAL=2"), rec("2주마다")); }
        @Test @DisplayName("일주일마다 정기회의를 엽시다") void c312() {
            matches(obj("date", d(2026, 10, 30), "rrule", "FREQ=WEEKLY", "text", "일주일마다"), rec("일주일마다 정기회의를 엽시다"));
        }
        @Test @DisplayName("격주로 만나자") void c313() {
            matches(obj("rrule", "FREQ=WEEKLY;INTERVAL=2", "text", "격주"), rec("격주로 만나자"));
        }
        // "N일마다"는 N일 간격 ("3일"이라는 날짜가 아님)
        @Test @DisplayName("3일마다") void c314() {
            matches(obj("date", d(2026, 10, 30), "rrule", "FREQ=DAILY;INTERVAL=3", "text", "3일마다"), rec("3일마다"));
        }
        @Test @DisplayName("14일마다 정산") void c315() { matches(obj("rrule", "FREQ=DAILY;INTERVAL=14"), rec("14일마다 정산")); }
        @Test @DisplayName("이틀마다") void c316() { matches(obj("rrule", "FREQ=DAILY;INTERVAL=2"), rec("이틀마다")); }
        // 시각이 있으면 지난 시각은 건너뛴다
        @Test @DisplayName("매주 오후 3시") void c317() {
            matches(obj("date", d(2026, 10, 30, 15), "hasTime", true, "rrule", "FREQ=WEEKLY"), rec("매주 오후 3시"));
        }
        @Test @DisplayName("매달 오전 9시") void c318() {
            matches(obj("date", d(2026, 10, 31, 9), "rrule", "FREQ=MONTHLY"), rec("매달 오전 9시"));
        }
        // "N개월마다", "일주일마다" 뒤에 날짜, 요일이 오면 그 날짜, 요일로 반복
        @Test @DisplayName("2개월마다 1일") void c319() {
            matches(obj("date", d(2026, 11, 1), "rrule", "FREQ=MONTHLY;INTERVAL=2;BYMONTHDAY=1"), rec("2개월마다 1일"));
        }
        @Test @DisplayName("달마다 14일") void c320() {
            matches(obj("date", d(2026, 11, 14), "rrule", "FREQ=MONTHLY;BYMONTHDAY=14"), rec("달마다 14일"));
        }
        @Test @DisplayName("한달마다 14일") void c321() { matches(obj("rrule", "FREQ=MONTHLY;BYMONTHDAY=14"), rec("한달마다 14일")); }
        @Test @DisplayName("매달 14일마다") void c322() {
            matches(obj("date", d(2026, 11, 14), "rrule", "FREQ=MONTHLY;BYMONTHDAY=14"), rec("매달 14일마다"));
        }
        @Test @DisplayName("일주일마다 월요일") void c323() {
            matches(obj("date", d(2026, 11, 2), "rrule", "FREQ=WEEKLY;BYDAY=MO"), rec("일주일마다 월요일"));
        }
        @Test @DisplayName("2년마다 3월 1일") void c324() {
            matches(obj("date", d(2027, 3, 1), "rrule", "FREQ=YEARLY;INTERVAL=2;BYMONTH=3;BYMONTHDAY=1"), rec("2년마다 3월 1일"));
        }
        // 매주 + 며칠은 앞뒤가 안 맞는다. 하루짜리 날짜로 바꿔 내지 않고 null
        @Test @DisplayName("매주 14일 공과금 납부") void c325() { assertNull(p("매주 14일 공과금 납부")); }
        @Test @DisplayName("매주 14일") void c326() { assertNull(p("매주 14일")); }
        @Test @DisplayName("일주일마다 3일") void c327() { assertNull(p("일주일마다 3일")); }
        // "매달다"는 반복이 아님
        @Test @DisplayName("줄에 매달려 있어") void c328() { assertNull(p("줄에 매달려 있어")); }
        @Test @DisplayName("간판을 매달았다") void c329() { assertNull(p("간판을 매달았다")); }
    }
}
