package kochrono;

import static kochrono.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/** 원본 tests/parse.test.ts의 지난 달 → 내년 (규칙 13)부터 고유어 일수 / 년 / 이맘때까지. 기준: 2026-10-30 (금) 14:00 */
class Parse02Test {
    @Nested @DisplayName("지난 달 → 내년 (규칙 13)")
    class G05 {
        @Test @DisplayName("2월 말") void c33() { assertEquals(d(2027, 2, 28), p("2월 말").date()); }
        @Test @DisplayName("3월") void c34() { assertEquals(d(2027, 3, 1), p("3월에 이사").date()); }
        @Test @DisplayName("9월 말") void c35() { assertEquals(d(2027, 9, 30), p("9월 말").date()); }
        // 이번 달은 아직 안 끝났으므로 올해
        @Test @DisplayName("10월") void c36() { assertEquals(d(2026, 10, 1), p("10월").date()); }
        @Test @DisplayName("10월 초") void c37() { assertEquals(d(2026, 10, 1), p("10월 초").date()); }
        @Test @DisplayName("12월") void c38() { assertEquals(d(2026, 12, 1), p("12월").date()); }
    }

    @Nested @DisplayName("지난/다음 X요일")
    class G06 {
        // "지난 금요일" = 오늘을 제외한 가장 가까운 과거의 금요일
        @Test @DisplayName("지난 금요일 (오늘이 금요일)") void c39() { assertEquals(d(2026, 10, 23), p("지난 금요일").date()); }
        @Test @DisplayName("지난 금요일 (토요일 기준 → 어제)") void c40() {
            assertEquals(d(2026, 10, 30), parse("지난 금요일", d(2026, 10, 31, 10)).date());
        }
        @Test @DisplayName("저번 수욜") void c41() { assertEquals(d(2026, 10, 28), p("저번 수욜").date()); }
        // "다음 금요일" = 오늘을 제외한 가장 가까운 미래의 금요일
        @Test @DisplayName("다음 금요일 (오늘이 금요일 → 일주일 뒤)") void c42() { assertEquals(d(2026, 11, 6), p("다음 금요일").date()); }
        @Test @DisplayName("다음 월요일") void c43() { assertEquals(d(2026, 11, 2), p("다음 월요일").date()); }
    }

    @Nested @DisplayName("괄호 요일 / 요일 범위")
    class G07 {
        @Test @DisplayName("11/3(화) 14:00") void c44() {
            ParseResult r = p("11/3(화) 14:00 킥오프");
            assertEquals(d(2026, 11, 3, 14), r.date());
            assertEquals("11/3(화) 14:00", r.text());
        }
        @Test @DisplayName("11/3(수) (요일 불일치 → null)") void c45() { assertNull(p("11/3(수)")); }
        @Test @DisplayName("월~금") void c46() {
            ParseResult r = p("월~금 출근");
            assertEquals(d(2026, 11, 2), r.date());
            assertEquals(d(2026, 11, 6, 23, 59), r.end());
        }
        @Test @DisplayName("화~목요일") void c47() { assertEquals(d(2026, 11, 5, 23, 59), p("화~목요일").end()); }
    }

    @Nested @DisplayName("시각 단어 추가")
    class G08 {
        @Test @DisplayName("오전 중으로") void c48() {
            ParseResult r = p("오전 중으로 보내드릴게요");
            assertEquals(d(2026, 10, 31, 12), r.end());
            assertEquals("오전 중으로", r.text());
        }
        @Test @DisplayName("내일 오후 중에") void c49() { assertEquals(d(2026, 10, 31, 18), p("내일 오후 중에").end()); }
        @Test @DisplayName("퇴근 전까지") void c50() { assertEquals(d(2026, 10, 30, 18), p("퇴근 전까지").end()); }
        @Test @DisplayName("퇴근 후에 보자") void c51() { assertEquals(d(2026, 10, 30, 18), p("퇴근 후에 보자").date()); }
        @Test @DisplayName("토요일 낮에") void c52() { assertEquals(d(2026, 10, 31, 12), p("토요일 낮에").date()); }
        @Test @DisplayName("온도가 낮다 (잡지 않음)") void c53() { assertNull(p("온도가 낮다")); }
        @Test @DisplayName("5시 10분 전") void c54() { assertEquals(d(2026, 10, 30, 16, 50), p("5시 10분 전에 모이자").date()); }
        @Test @DisplayName("30분 내로") void c55() { assertEquals(d(2026, 10, 30, 14, 30), p("30분 내로").end()); }
    }

    @Nested @DisplayName("고유어 일수 / 년 / 이맘때")
    class G09 {
        @Test @DisplayName("이틀 뒤") void c56() { assertEquals(d(2026, 11, 1), p("이틀 뒤").date()); }
        @Test @DisplayName("사흘 전") void c57() { assertEquals(d(2026, 10, 27), p("사흘 전").date()); }
        @Test @DisplayName("닷새 후") void c58() { assertEquals(d(2026, 11, 4), p("닷새 후").date()); }
        @Test @DisplayName("1년 뒤") void c59() { assertEquals(d(2027, 10, 30), p("1년 뒤").date()); }
        @Test @DisplayName("2년 전") void c60() { assertEquals(d(2024, 10, 30), p("2년 전").date()); }
        @Test @DisplayName("1년 뒤 (2월 29일 기준 → 2월 28일)") void c61() {
            assertEquals(d(2029, 2, 28), parse("1년 뒤", d(2028, 2, 29, 10)).date());
        }
        @Test @DisplayName("내년 이맘때") void c62() { assertEquals(d(2027, 10, 30), p("내년 이맘때").date()); }
        @Test @DisplayName("다음달 이맘때") void c63() { assertEquals(d(2026, 11, 30), p("다음달 이맘때").date()); }
        @Test @DisplayName("다음주 이맘때") void c64() { assertEquals(d(2026, 11, 6), p("다음주 이맘때").date()); }
        @Test @DisplayName("연말 / 연초 / 월초 / 월말") void c65() {
            assertEquals(d(2026, 12, 31), p("연말").date());
            assertEquals(d(2027, 1, 1), p("연초").date());
            assertEquals(d(2026, 10, 1), p("월초").date());
            assertEquals(d(2026, 10, 31), p("월말").date());
        }
        @Test @DisplayName("5일장 (잡지 않음)") void c66() { assertNull(p("5일장")); }
        @Test @DisplayName("3일째 (잡지 않음)") void c67() { assertNull(p("3일째 연속")); }
    }
}
