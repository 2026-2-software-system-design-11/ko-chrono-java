package kochrono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

/** resolve의 날짜 단계들. 각 단계는 없는 날짜면 false를 돌려준다 */
final class Days {
    private Days() {
    }

    /**
     * 0. 음력 명절과 음력 날짜 (규칙 29, 32): 연도를 말했으면 그 음력 해, 아니면 (연휴까지) 지나지 않은 가장 가까운 것.
     * 음력 12월은 양력으로 다음 해 1월이라 작년 음력부터 본다. 표에 없는 날짜는 null
     */
    static boolean lunar(Components c, LocalDateTime ref, Resolver.DateState s) {
        Long said = c.year != null ? Long.valueOf(c.year)
                : c.yearOffset != null ? Long.valueOf(ref.getYear() + c.yearOffset) : null;
        long tail = (c.dayOffset != null ? c.dayOffset : 0)
                + (c.durationDays != null && c.durationDays != 0 ? c.durationDays - 1 : 0);
        long y0 = ref.getYear();
        // 과거형(규칙 37)이면 거꾸로: 이미 시작된 가장 가까운 것
        long[] years = said != null ? new long[] {said}
                : c.past ? new long[] {y0 + 1, y0, y0 - 1, y0 - 2} : new long[] {y0 - 1, y0, y0 + 1, y0 + 2};
        for (long y : years) {
            LocalDate solar = c.lunar != null ? LunarCalendar.holiday(y, c.lunar)
                    : LunarCalendar.toSolar(y, c.lunarDate.month(), c.lunarDate.day(), c.lunarDate.leap());
            // 그 해에 없는 날 ("음력 2월 30일", 윤달)
            if (solar == null) {
                continue;
            }
            LocalDateTime start = solar.atStartOfDay();
            LocalDateTime lastMoment = solar.plusDays(tail).atTime(23, 59);
            if (said != null || (c.past ? !start.isAfter(ref) : !lastMoment.isBefore(ref))) {
                s.d = start;
                s.dateSet = true;
                return true;
            }
        }
        return false;
    }

    /** 1. 절대 날짜 */
    static boolean absolute(Components c, LocalDateTime ref, Resolver.DateState s) {
        boolean hasYear = c.year != null || c.yearOffset != null;
        int y = c.year != null ? c.year : ref.getYear() + (c.yearOffset != null ? c.yearOffset : 0);
        // "작년", "내년"처럼 연도만 있으면 1월 1일. "내년 이맘때", "1년 뒤"는 오늘 날짜 유지.
        // "13월 말"처럼 12를 넘는 월(검사하지 않는 자리)은 원본처럼 다음 해로 넘어간다
        int month = c.month != null ? c.month : hasYear && !c.sameDay ? 1 : ref.getMonthValue();
        YearMonth ym = YearMonth.of(y, 1).plusMonths(month - 1L);
        // "5일까지"처럼 일만 있으면: 오늘이 그날을 넘었으면 다음 달, 아니면 이번 달 (오늘 포함).
        // 그 달에 없는 날("11월에 31일")은 아래에서 버린다
        if (!hasYear && c.month == null && c.day != null && c.monthOffset == null && c.nthWeek == null) {
            if (c.day < ref.getDayOfMonth()) {
                ym = ym.plusMonths(1);
            }
        }
        // 연도 미지정이고 이미 지났으면 내년으로.
        // 월일("3월 1일")은 그 날이, 월("3월", "2월 말")은 그 달이 통째로 지났을 때.
        // 그 해에 없는 날(평년의 2월 29일)은 원본처럼 그 달 1일부터 센 날(3월 1일)로 비교한다
        if (!hasYear && c.month != null) {
            if (Calendars.dayCountedFrom(ym, c.day != null ? c.day : ym.lengthOfMonth()).atTime(23, 59).isBefore(ref)) {
                ym = ym.plusYears(1);
            }
        }
        int day = c.day != null ? c.day : c.month != null || (hasYear && !c.sameDay) ? 1 : ref.getDayOfMonth();
        if (c.monthPart != null) {
            day = partDay(ym, c.monthPart);
        }
        if (c.sameDay) {
            day = Math.min(day, ym.lengthOfMonth());
        }
        // 2월 30일, 11월 31일처럼 없는 날짜는 다음 달로 넘기지 않고 버린다.
        // "11월 0일"(범위를 검사하지 않는 자리)은 원본처럼 앞 달의 마지막 날이 된다
        if (day > ym.lengthOfMonth()) {
            return false;
        }
        s.d = Calendars.dayCountedFrom(ym, day).atStartOfDay();
        s.dateSet = true;
        return true;
    }

    /** 2. 상대 월 */
    static boolean relativeMonth(Components c, LocalDateTime ref, Resolver.DateState s) {
        YearMonth ym = YearMonth.from(ref).plusMonths(c.monthOffset);
        int day = c.monthPart != null ? partDay(ym, c.monthPart) : c.day != null ? c.day : 1;
        if (day > ym.lengthOfMonth()) {
            return false;
        }
        // "다음달 이맘때"는 오늘 날짜 유지
        s.d = (c.sameDay ? Calendars.sameDayIn(ym, ref.getDayOfMonth()) : Calendars.dayCountedFrom(ym, day)).atStartOfDay();
        s.dateSet = true;
        return true;
    }

    /** 3. N째 주 (규칙 22), 4. 주와 요일 */
    static boolean week(Components c, ResolveOptions opts, Resolver.DateState s) {
        LocalDateTime ref = opts.referenceDate();
        if (c.nthWeek != null) {
            // 달은 1, 2단계에서 정해진 달, 없으면 기준 달
            YearMonth month = YearMonth.from(s.dateSet ? s.d : ref);
            int target = c.weekday != null ? c.weekday : opts.weekStart() == WeekStart.MONDAY ? 1 : 0;
            Integer day = Calendars.nthWeekday(month, target, c.nthWeek);
            // "11월 다섯째 주 토요일"처럼 없는 날짜는 null
            if (day == null) {
                return false;
            }
            s.d = month.atDay(day).atStartOfDay();
            s.dateSet = true;
        } else if (c.weekday != null && c.weekOffset == null && c.day != null) {
            // "11월 5일 목요일": 날짜가 우선. 요일이 안 맞으면 어느 쪽이 맞는지 모르므로 null
            return Calendars.weekdayNumber(s.d.toLocalDate()) == c.weekday;
        } else if (c.weekOffset != null || c.weekday != null) {
            s.d = weekDay(c, opts, s);
            s.dateSet = true;
        }
        return true;
    }

    private static LocalDateTime weekDay(Components c, ResolveOptions opts, Resolver.DateState s) {
        LocalDateTime ref = opts.referenceDate();
        LocalDateTime today = ref.toLocalDate().atStartOfDay();
        if (c.weekOffset != null && c.sameDay) {
            // "다음주 이맘때"는 7일 뒤
            return today.plusDays(c.weekOffset * 7L);
        }
        if (c.weekOffset != null) {
            LocalDateTime sow = Resolver.startOfWeek(ref, opts.weekStart()).plusDays(c.weekOffset * 7L);
            return c.weekday != null ? sow.plusDays(Resolver.offsetInWeek(c.weekday, opts.weekStart())) : sow;
        }
        // 요일 단독: 오늘 포함, 가장 가까운 미래의 그 요일
        int dow = Calendars.weekdayNumber(today.toLocalDate());
        // "주말" 단독인데 오늘이 일요일이면 오늘
        int diff = c.weekend && dow == 0 ? 0 : (c.weekday - dow + 7) % 7;
        // "다음 금요일"은 오늘 제외 미래, "지난 금요일"은 오늘 제외 과거
        if (c.weekdayDir == Components.WeekdayDir.NEXT && diff == 0) {
            diff = 7;
        }
        if (c.weekdayDir == Components.WeekdayDir.PAST) {
            diff = diff == 0 ? -7 : diff - 7;
        }
        return today.plusDays(diff);
    }

    static int partDay(YearMonth month, Components.MonthPart part) {
        return switch (part) {
            case START -> 1;
            case MIDDLE -> 15;
            case END -> month.lengthOfMonth();
        };
    }
}
