package kochrono;

import java.time.LocalDateTime;
import java.util.List;

/** 규칙 31: 해석이 갈릴 수 있는 지점과 다른 후보 */
final class Ambiguities {
    private Ambiguities() {
    }

    static void apply(LocalResult result, Components c, boolean dateSet, ResolveOptions opts) {
        LocalDateTime ref = opts.referenceDate();
        LocalDateTime d = result.date;
        // 오전, 오후 없는 1~12시: 반대쪽 12시간이 후보 (날짜가 없으면 규칙 4대로 다음 발생)
        if (c.hour != null && !c.meridiem && !c.midnight && c.hour >= 1 && c.hour <= 12) {
            int used = Resolver.clockHour(c);
            int altHour = used == 12 ? 0 : used < 12 ? used + 12 : used - 12;
            LocalDateTime alt = Calendars.atClock((dateSet ? d : ref).toLocalDate(), altHour, c.minute != null ? c.minute : 0);
            if (!dateSet && c.past) {
                if (alt.isAfter(ref)) {
                    alt = alt.minusDays(1);
                }
            } else if (!dateSet && alt.isBefore(ref)) {
                alt = alt.plusDays(1);
            }
            set(result, Ambiguity.Reason.MERIDIEM, List.of(alt));
            return;
        }
        // "다음 토요일": 규칙 23(오늘 제외 가장 가까운)과 "다음 주 토요일"이 다르면 후보로
        if (c.weekdayDir == Components.WeekdayDir.NEXT && c.weekday != null) {
            LocalDateTime alt = Resolver.startOfWeek(ref, opts.weekStart())
                    .plusDays(7 + Resolver.offsetInWeek(c.weekday, opts.weekStart()));
            alt = alt.withHour(d.getHour()).withMinute(d.getMinute());
            if (!alt.equals(d)) {
                set(result, Ambiguity.Reason.NEXT_WEEKDAY, List.of(alt));
                return;
            }
        }
        if (c.approximate) {
            set(result, Ambiguity.Reason.APPROXIMATE_TIME, List.of());
        }
    }

    private static void set(LocalResult result, Ambiguity.Reason reason, List<LocalDateTime> alternatives) {
        result.ambiguity = reason;
        result.alternatives = alternatives;
    }
}
