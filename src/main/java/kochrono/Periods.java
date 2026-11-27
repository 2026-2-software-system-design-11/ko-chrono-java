package kochrono;

import java.time.LocalDateTime;
import java.time.YearMonth;

/** 기간("3일간")의 끝 */
final class Periods {
    private Periods() {
    }

    /** 결과에 끝을 채운다. 기간이 있는데 시작이 없으면 false */
    static boolean apply(LocalResult result, Components c, boolean dateSet, LocalDateTime ref) {
        LocalDateTime d = result.date;
        // "오늘부터 3일간", "3시부터 2시간 동안": 시작일을 포함한 기간의 끝
        if (c.durationDays != null || c.durationMonths != null || c.durationMinutes != null) {
            if (!dateSet && !result.hasTime) {
                return false;
            }
            LocalDateTime end = d;
            if (c.durationMinutes != null) {
                end = end.plusMinutes(c.durationMinutes);
            } else {
                // "1월 31일부터 한 달간"처럼 N달 뒤에 그 날이 없으면 원본처럼 넘치는 만큼 다음 달로 간다
                if (c.durationMonths != null) {
                    end = Calendars.dayCountedFrom(YearMonth.from(end).plusMonths(c.durationMonths), end.getDayOfMonth())
                            .atTime(end.toLocalTime());
                }
                if (c.durationDays != null) {
                    end = end.plusDays(c.durationDays);
                }
                end = end.minusDays(1).toLocalDate().atTime(23, 59);
            }
            result.end = end;
            return true;
        }
        return true;
    }
}
