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
    }

    private static void set(LocalResult result, Ambiguity.Reason reason, List<LocalDateTime> alternatives) {
        result.ambiguity = reason;
        result.alternatives = alternatives;
    }
}
