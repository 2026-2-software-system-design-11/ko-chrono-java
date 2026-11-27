package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/** 원본 tests/lunar.test.ts. KASI에서 옮긴 음력 표가 그럴듯한지 스스로 검증한다 */
@DisplayName("음력 표 검증")
class LunarTest {
    @Test @DisplayName("모든 달은 29일이나 30일 (길이를 모르는 마지막 달 제외)") void c1() {
        for (int y : LunarCalendar.YEARS) {
            for (Integer len : LunarCalendar.monthLengths(y)) {
                if (len != null) {
                    assertTrue(List.of(29, 30).contains(len), y + "년 " + len);
                }
            }
        }
    }

    // 관공서 공휴일 목록과 교차 확인한 날짜
    private static final Map<Integer, String[]> HOLIDAYS = new TreeMap<>(Map.of(
            2025, new String[] {"2025-01-29", "2025-05-05", "2025-10-06"},
            2026, new String[] {"2026-02-17", "2026-05-24", "2026-09-25"},
            2027, new String[] {"2027-02-07", "2027-05-13", "2027-09-15"},
            2028, new String[] {"2028-01-27", "2028-05-02", "2028-10-03"}));

    private static String iso(LocalDate d) {
        return d == null ? null : d.toString();
    }

    @TestFactory Stream<DynamicTest> holidays() {
        return HOLIDAYS.entrySet().stream().map(e -> DynamicTest.dynamicTest(e.getKey() + "년 설날, 부처님 오신 날, 추석", () -> {
            int y = e.getKey();
            assertEquals(Arrays.asList(e.getValue()), Arrays.asList(
                    iso(LunarCalendar.holiday(y, Components.LunarHoliday.SEOLLAL)),
                    iso(LunarCalendar.holiday(y, Components.LunarHoliday.BUDDHA)),
                    iso(LunarCalendar.holiday(y, Components.LunarHoliday.CHUSEOK))));
        }));
    }
}
