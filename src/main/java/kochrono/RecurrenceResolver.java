package kochrono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 반복 일정 (규칙 27): date는 기준 시각 이후 첫 발생.
 * 시각이 있으면 지난 시각은 건너뛰고, 날짜만 있으면 오늘도 포함한다.
 * "매달 31일"은 31일이 없는 달을 건너뛴다 (RRULE과 같은 동작).
 * 주기 말만 있으면("매주", "2개월마다") 오늘부터. 요일과 날짜는 RRULE에 넣지 않고 첫 발생일(DTSTART)을 따른다.
 */
final class RecurrenceResolver {
    private RecurrenceResolver() {
    }

    static LocalResult resolve(List<Fragment> group, Components c, ResolveOptions opts) {
        RecurSpec r = c.recur;
        LocalDateTime ref = opts.referenceDate();
        boolean hasTime = c.hour != null;
        LocalDateTime today = ref.toLocalDate().atStartOfDay();
        Occurrence at = new Occurrence(c, opts, hasTime, today);
        LocalDateTime d = first(r, at, ref, today);
        if (d == null) {
            return null;
        }
        RecurSpec rule = r;
        boolean timed = hasTime || r.freq() == Frequency.HOURLY || r.freq() == Frequency.MINUTELY;
        LocalResult result = new LocalResult();
        result.date = d;
        result.index = group.get(0).index();
        result.hasTime = timed;
        result.recur = rule;
        result.rrule = Rrule.of(rule, !timed);
        Fragment last = group.get(group.size() - 1);
        result.endIndex = last.index() + last.text().length();
        return result;
    }

    /** 그날의 발생 시각과, 그 시각이 기준 이후인지 */
    private record Occurrence(Components c, ResolveOptions opts, boolean hasTime, LocalDateTime today) {
        LocalDateTime at(LocalDateTime day) {
            return hasTime ? Calendars.atClock(day.toLocalDate(), Resolver.clockHour(c), c.minute != null ? c.minute : 0)
                    : Calendars.atClock(day.toLocalDate(), opts.defaultHour(), 0);
        }

        boolean ok(LocalDateTime x) {
            return hasTime ? !x.isBefore(opts.referenceDate()) : !x.isBefore(today);
        }
    }

    private static LocalDateTime first(RecurSpec r, Occurrence o, LocalDateTime ref, LocalDateTime today) {
        if (r.freq() == Frequency.DAILY || (r.byWeekday().isEmpty() && r.byMonthDay() == null && r.byMonth() == null)) {
            LocalDateTime d = o.at(today);
            return o.ok(d) ? d : d.plusDays(1);
        }
        if (r.freq() == Frequency.WEEKLY) {
            for (int i = 0; i < 14; i++) {
                LocalDateTime day = today.plusDays(i);
                if (r.byWeekday().contains(Calendars.weekdayNumber(day.toLocalDate())) && o.ok(o.at(day))) {
                    return o.at(day);
                }
            }
            return null;
        }
        return null;
    }
}
