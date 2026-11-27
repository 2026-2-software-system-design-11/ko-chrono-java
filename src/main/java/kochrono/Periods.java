package kochrono;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

/** 기간("3일간"), 마감("까지"), 전체("내내")의 끝 */
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
        if (c.isEnd || c.whole) {
            // "~까지", "~이내": 시작은 기준 시각, 끝은 해당 시점(시간 없으면 그날 23:59)
            // "~내내": 시작은 그 기간의 시작, 끝은 기간의 끝
            result.end = result.hasTime ? d : periodEnd(d, c);
            // "이번 주말 내내"는 토요일부터 일요일까지
            if (c.whole && c.weekend && !result.hasTime && result.end.getDayOfWeek() == DayOfWeek.SATURDAY) {
                result.end = result.end.plusDays(1);
            }
            // 규칙 30: "어제까지"처럼 이미 지난 마감은 구간이 아니라 그 시점 (date와 end가 같다)
            if (c.isEnd) {
                result.date = result.end.isBefore(ref) ? result.end : ref;
            }
        }
        return true;
    }

    /** 날짜 표현이 가리키는 기간의 마지막 시각. "이번 주"는 일요일, "이번달"은 말일, "올해"는 12/31, 그 외는 그날 23:59 */
    private static LocalDateTime periodEnd(LocalDateTime d, Components c) {
        LocalDateTime end = d;
        if (c.weekOffset != null && c.weekday == null) {
            end = end.plusDays(6);
        } else if (c.monthOffset != null && c.monthPart == null && c.day == null && !c.sameDay) {
            end = YearMonth.from(end).atEndOfMonth().atStartOfDay();
        } else if ((c.year != null || c.yearOffset != null) && c.month == null && !c.sameDay) {
            end = LocalDate.of(end.getYear(), 12, 31).atStartOfDay();
        }
        return end.toLocalDate().atTime(23, 59);
    }
}
