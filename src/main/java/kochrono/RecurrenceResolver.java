package kochrono;

import java.time.LocalDateTime;
import java.time.YearMonth;
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
        // "매주 14일": 매주와 며칠은 같이 올 수 없다. 추측하지 않는다
        if (r.freq() == Frequency.WEEKLY && c.day != null) {
            return null;
        }
        LocalDateTime ref = opts.referenceDate();
        boolean hasTime = c.hour != null;
        LocalDateTime today = ref.toLocalDate().atStartOfDay();
        Occurrence at = new Occurrence(c, opts, hasTime, today);
        LocalDateTime d = first(r, at, ref, today);
        if (d == null) {
            return null;
        }
        RecurSpec rule = r;
        // 규칙 34: "10주 동안 매주 월요일"은 오늘(또는 "A부터"의 A)부터 그 기간의 마지막 날 23:59까지
        if (c.durationDays != null || c.durationMonths != null) {
            // N달 뒤 같은 날의 전날까지. 그 달에 없는 날이면 말일로 맞춘다 (11/30에서 3달이면 2/28의 전날)
            YearMonth month = Calendars.yearMonth(today.getYear(),
                    today.getMonthValue() - 1 + (c.durationMonths != null ? c.durationMonths : 0));
            LocalDateTime until = Calendars.sameDayIn(month, today.getDayOfMonth())
                    .plusDays((c.durationDays != null ? c.durationDays : 0) - 1).atTime(23, 59);
            if (until.isBefore(d)) {
                return null;
            }
            rule = rule.withUntil(until);
        }
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
        if (r.freq() == Frequency.MINUTELY || r.freq() == Frequency.HOURLY) {
            return ref.withSecond(0).withNano(0).plusMinutes((long) r.interval() * (r.freq() == Frequency.HOURLY ? 60 : 1));
        }
        if (r.freq() == Frequency.DAILY || (r.byWeekday().isEmpty() && r.byMonthDay() == null && r.byMonth() == null)) {
            LocalDateTime d = o.at(today);
            return o.ok(d) ? d : d.plusDays(1);
        }
        if (r.freq() == Frequency.WEEKLY) {
            for (int i = 0; i < 14; i++) {
                LocalDateTime day = Calendars.constructed(today).plusDays(i);
                if (r.byWeekday().contains(Calendars.weekdayNumber(day.toLocalDate())) && o.ok(o.at(day))) {
                    return o.at(day);
                }
            }
            return null;
        }
        if (r.freq() == Frequency.MONTHLY) {
            for (int k = 0; k < 24; k++) {
                YearMonth month = Calendars.yearMonth(today.getYear(), today.getMonthValue() - 1 + k);
                Integer dom = r.byWeekdayNth() != null
                        ? Calendars.nthWeekday(month, r.byWeekday().get(0), r.byWeekdayNth())
                        : r.byMonthDay() == -1 ? Integer.valueOf(month.lengthOfMonth()) : r.byMonthDay();
                if (dom == null || !month.isValidDay(dom)) {
                    continue;
                }
                LocalDateTime x = o.at(month.atDay(dom).atStartOfDay());
                if (o.ok(x)) {
                    return x;
                }
            }
            return null;
        }
        // YEARLY: 2월 29일은 다음 윤년까지
        for (int k = 0; k < 9; k++) {
            YearMonth month = Calendars.yearMonth(today.getYear() + k, r.byMonth() - 1);
            if (!month.isValidDay(r.byMonthDay())) {
                continue;
            }
            LocalDateTime x = o.at(month.atDay(r.byMonthDay()).atStartOfDay());
            if (o.ok(x)) {
                return x;
            }
        }
        return null;
    }
}
