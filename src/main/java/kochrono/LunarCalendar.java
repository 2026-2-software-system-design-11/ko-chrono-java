package kochrono;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 음력을 양력으로 바꾸는 표. 런타임 의존성 없이 음력을 다루려면 표가 필요하다.
 *
 * <p>출처: 한국천문연구원 천문우주지식정보 "월별 양음력 대조" (astro.kasi.re.kr/life/post/calendarData).
 * 원본 ko-chrono가 2025~2028년 페이지에서 음력 초하루가 되는 양력 날짜를 옮긴 표를 그대로 가져왔다.
 * 표에 없는 날짜는 추측하지 않고 null이다 (규칙 10). 한국 음력은 한국 표준시 기준이라 중국과 하루 다를 수 있다.
 */
final class LunarCalendar {
    /** 그 음력 달 초하루의 양력 날짜. 연속된 달 순서대로 */
    private record MonthStart(int month, boolean leap, LocalDate solar) {
    }

    private static final Map<Integer, List<MonthStart>> MONTH_STARTS = new TreeMap<>(Map.of(
            2025, months(new int[][] {
                {1, 0, 2025, 1, 29}, {2, 0, 2025, 2, 28}, {3, 0, 2025, 3, 29}, {4, 0, 2025, 4, 28},
                {5, 0, 2025, 5, 27}, {6, 0, 2025, 6, 25}, {6, 1, 2025, 7, 25}, {7, 0, 2025, 8, 23},
                {8, 0, 2025, 9, 22}, {9, 0, 2025, 10, 21}, {10, 0, 2025, 11, 20}, {11, 0, 2025, 12, 20},
                {12, 0, 2026, 1, 19}}),
            2026, months(new int[][] {
                {1, 0, 2026, 2, 17}, {2, 0, 2026, 3, 19}, {3, 0, 2026, 4, 17}, {4, 0, 2026, 5, 17},
                {5, 0, 2026, 6, 15}, {6, 0, 2026, 7, 14}, {7, 0, 2026, 8, 13}, {8, 0, 2026, 9, 11},
                {9, 0, 2026, 10, 11}, {10, 0, 2026, 11, 9}, {11, 0, 2026, 12, 9}, {12, 0, 2027, 1, 8}}),
            2027, months(new int[][] {
                {1, 0, 2027, 2, 7}, {2, 0, 2027, 3, 8}, {3, 0, 2027, 4, 7}, {4, 0, 2027, 5, 6},
                {5, 0, 2027, 6, 5}, {6, 0, 2027, 7, 4}, {7, 0, 2027, 8, 2}, {8, 0, 2027, 9, 1},
                {9, 0, 2027, 9, 30}, {10, 0, 2027, 10, 29}, {11, 0, 2027, 11, 28}, {12, 0, 2027, 12, 28}}),
            2028, months(new int[][] {
                {1, 0, 2028, 1, 27}, {2, 0, 2028, 2, 25}, {3, 0, 2028, 3, 26}, {4, 0, 2028, 4, 25},
                {5, 0, 2028, 5, 24}, {5, 1, 2028, 6, 23}, {6, 0, 2028, 7, 22}, {7, 0, 2028, 8, 20},
                {8, 0, 2028, 9, 19}, {9, 0, 2028, 10, 18}, {10, 0, 2028, 11, 16}, {11, 0, 2028, 12, 16},
                {12, 0, 2029, 1, 15}})));

    /** 표가 있는 음력 해 */
    static final List<Integer> YEARS = List.copyOf(MONTH_STARTS.keySet());

    private LunarCalendar() {
    }

    private static List<MonthStart> months(int[][] rows) {
        List<MonthStart> out = new ArrayList<>();
        for (int[] r : rows) {
            out.add(new MonthStart(r[0], r[1] == 1, LocalDate.of(r[2], r[3], r[4])));
        }
        return List.copyOf(out);
    }

    /** 그 음력 달의 다음 달 초하루 (모르면 null) */
    private static LocalDate nextStart(int year, int index) {
        List<MonthStart> months = MONTH_STARTS.get(year);
        if (index + 1 < months.size()) {
            return months.get(index + 1).solar();
        }
        List<MonthStart> next = MONTH_STARTS.get(year + 1);
        return next != null ? next.get(0).solar() : null;
    }

    /**
     * 음력을 양력으로. 그 달에 없는 날(29일인 달의 30일)이나 표에 없는 날짜는 null.
     * 마지막 달처럼 길이를 모르면 29일까지만 받는다.
     */
    static LocalDate toSolar(long year, int month, int day, boolean leap) {
        List<MonthStart> months = year == (int) year ? MONTH_STARTS.get((int) year) : null;
        if (months == null || day < 1 || day > 30) {
            return null;
        }
        for (int i = 0; i < months.size(); i++) {
            MonthStart s = months.get(i);
            if (s.month() == month && s.leap() == leap) {
                LocalDate next = nextStart((int) year, i);
                long length = next != null ? ChronoUnit.DAYS.between(s.solar(), next) : 29;
                return day > length ? null : s.solar().plusDays(day - 1);
            }
        }
        return null;
    }

    /** 설날(1/1), 부처님 오신 날(4/8), 추석(8/15)의 양력 날짜 */
    static LocalDate holiday(long year, Components.LunarHoliday which) {
        return switch (which) {
            case SEOLLAL -> toSolar(year, 1, 1, false);
            case BUDDHA -> toSolar(year, 4, 8, false);
            case CHUSEOK -> toSolar(year, 8, 15, false);
        };
    }

    /** 각 음력 달의 길이 (검증용). 모르면 null */
    static List<Integer> monthLengths(int year) {
        List<MonthStart> months = MONTH_STARTS.get(year);
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < months.size(); i++) {
            LocalDate next = nextStart(year, i);
            out.add(next != null ? (int) ChronoUnit.DAYS.between(months.get(i).solar(), next) : null);
        }
        return out;
    }
}
