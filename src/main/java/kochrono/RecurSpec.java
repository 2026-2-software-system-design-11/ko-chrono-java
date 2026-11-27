package kochrono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 파서가 뽑은 반복 규칙. 공개 타입 {@link Recurrence}와 같은 값을 벽시계 시각으로 담는다.
 * 요일은 원본처럼 0=일 ... 6=토. 값이 없는 칸은 null, 요일이 없으면 빈 목록.
 */
record RecurSpec(Frequency freq, int interval, List<Integer> byWeekday, Integer byWeekdayNth, Integer byMonthDay,
                 Integer byMonth, LocalDateTime until, Integer count) {
    RecurSpec {
        byWeekday = List.copyOf(byWeekday);
    }

    static RecurSpec of(Frequency freq, int interval) {
        return new RecurSpec(freq, interval, List.of(), null, null, null, null, null);
    }

    RecurSpec withWeekdays(List<Integer> days) {
        return new RecurSpec(freq, interval, days, byWeekdayNth, byMonthDay, byMonth, until, count);
    }

    RecurSpec withNth(Integer nth) {
        return new RecurSpec(freq, interval, byWeekday, nth, byMonthDay, byMonth, until, count);
    }

    RecurSpec withMonthDay(Integer monthDay) {
        return new RecurSpec(freq, interval, byWeekday, byWeekdayNth, monthDay, byMonth, until, count);
    }

    RecurSpec withMonth(Integer month) {
        return new RecurSpec(freq, interval, byWeekday, byWeekdayNth, byMonthDay, month, until, count);
    }

    RecurSpec withUntil(LocalDateTime u) {
        return new RecurSpec(freq, interval, byWeekday, byWeekdayNth, byMonthDay, byMonth, u, count);
    }

    RecurSpec withCount(Integer n) {
        return new RecurSpec(freq, interval, byWeekday, byWeekdayNth, byMonthDay, byMonth, until, n);
    }
}
