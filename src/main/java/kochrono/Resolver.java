package kochrono;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/** 묶인 조각을 기준 시각에 대입해 날짜로 만든다. 모든 날짜 계산은 여기와 RecurrenceResolver에서만 한다 */
final class Resolver {
    /** 연도, 달, 주를 말하지 않아 "가까운 쪽"으로 고른 날짜의 주기. 과거형에서 한 주기 되돌릴 때 쓴다 */
    enum Bare { WEEK, DAY, YEAR }

    private Resolver() {
    }

    /**
     * 원본은 계산이 깨지면 Invalid Date가 되어 null을 낸다. Java의 날짜 타입은 깨진 값을 담지 못하고 예외를 던지므로
     * 그 예외와 Date의 범위를 넘는 값을 같은 뜻(null)으로 본다.
     */
    static LocalResult resolve(List<Fragment> group, ResolveOptions opts) {
        try {
            LocalResult r = resolveDate(group, opts);
            return r != null && r.valid() ? r : null;
        } catch (DateTimeException | ArithmeticException e) {
            return null;
        }
    }

    private static LocalResult resolveDate(List<Fragment> group, ResolveOptions opts) {
        Components c = Grouping.merge(group);
        LocalDateTime ref = opts.referenceDate();
        // "내일 이 시간"은 기준 시각의 시와 분. "다음 주 이 시간"은 7일 뒤 (그 주 월요일이 아니라)
        if (c.sameTime && c.hour == null) {
            c.hour = ref.getHour();
            c.minute = ref.getMinute();
            c.meridiem = true;
            if (c.weekOffset != null && c.weekday == null) {
                c.sameDay = true;
            }
        }
        if (c.recur != null) {
            return RecurrenceResolver.resolve(group, c, opts);
        }
        DateState s = new DateState(ref.withSecond(0).withNano(0));
        boolean hasTime = c.hour != null || c.hourOffset != null || c.minuteOffset != null;
        if (!resolveDay(c, opts, s)) {
            return null;
        }
        LocalDateTime d = resolveTime(c, opts, s);
        if (d == null) {
            return null;
        }
        LocalResult result = new LocalResult();
        result.date = d;
        result.index = group.get(0).index();
        result.hasTime = hasTime;
        Ambiguities.apply(result, c, s.dateSet, opts);
        result.past = c.past;
        Fragment last = group.get(group.size() - 1);
        result.endIndex = last.index() + last.text().length();
        return Periods.apply(result, c, s.dateSet, ref) ? result : null;
    }

    /** 날짜 계산 중의 상태 */
    static final class DateState {
        LocalDateTime d;
        boolean dateSet;
        Bare bare;

        DateState(LocalDateTime d) {
            this.d = d;
        }
    }

    /** 날짜 부분. 없는 날짜면 false */
    private static boolean resolveDay(Components c, ResolveOptions opts, DateState s) {
        LocalDateTime ref = opts.referenceDate();
        boolean isLunar = c.lunar != null || c.lunarDate != null;
        if (isLunar && !Days.lunar(c, opts, s)) {
            return false;
        }
        boolean hasYear = c.year != null || c.yearOffset != null;
        if (!isLunar && (hasYear || c.month != null || c.day != null) && !Days.absolute(c, opts, s)) {
            return false;
        }
        if (c.monthOffset != null && !Days.relativeMonth(c, opts, s)) {
            return false;
        }
        if (!Days.week(c, opts, s)) {
            return false;
        }
        // 일 단위 상대 이동
        if (c.dayOffset != null) {
            if (!s.dateSet) {
                s.d = opts.local(ref.toLocalDate().atStartOfDay());
            }
            s.d = opts.local(s.d.plusDays(c.dayOffset));
            s.dateSet = true;
        }
        return true;
    }

    /** 시각 부분. 결과가 없으면 null */
    private static LocalDateTime resolveTime(Components c, ResolveOptions opts, DateState s) {
        LocalDateTime ref = opts.referenceDate();
        int minute = c.minute != null ? c.minute : 0;
        if (c.hour != null && !s.dateSet) {
            // 시간만 있고 날짜가 없으면 오늘, 이미 지났으면 내일
            LocalDateTime d = opts.local(Calendars.atClock(ref.toLocalDate(), clockHour(c), minute));
            // 과거형(규칙 37)이면 반대로: 아직 안 왔으면 어제
            if (c.past) {
                return d.isAfter(ref) ? opts.local(d.minusDays(1)) : d;
            }
            return d.isBefore(ref) ? opts.local(d.plusDays(1)) : d;
        }
        if (c.hour != null) {
            LocalDateTime d = opts.local(Calendars.atClock(s.d.toLocalDate(), clockHour(c), minute));
            // "내일 자정"은 내일이 끝나는 시점
            if (c.midnight) {
                d = opts.local(d.plusDays(1));
            }
            // 과거형(규칙 37): "금요일 5시에 냈어"(금요일 14시)처럼 오늘로 잡았는데 시각이 아직이면 한 주기 앞으로
            if (c.past && s.bare != null && d.isAfter(ref)) {
                return stepBack(d, s.bare, opts);
            }
            return d;
        }
        if (c.hourOffset != null || c.minuteOffset != null) {
            LocalDateTime d = ref.withSecond(0).withNano(0);
            if (c.hourOffset != null && c.hourOffset != 0) {
                d = opts.local(d.plusHours(c.hourOffset));
            }
            if (c.minuteOffset != null && c.minuteOffset != 0) {
                d = opts.local(d.plusMinutes(c.minuteOffset));
            }
            return d;
        }
        if (s.dateSet) {
            return opts.local(Calendars.atClock(s.d.toLocalDate(), opts.defaultHour(), 0));
        }
        return null;
    }

    /** 규칙 20: 오전, 오후 없는 1~6시는 오후 ("3시에 보자"는 15시). 7~11시는 그대로 */
    static int clockHour(Components c) {
        int h = c.hour;
        return !c.meridiem && h >= 1 && h <= 6 ? h + 12 : h;
    }

    /** 과거형(규칙 37): 같은 요일, 일, 월일의 바로 앞 것. 그 달이나 해에 없는 날이면 null */
    private static LocalDateTime stepBack(LocalDateTime d, Bare unit, ResolveOptions opts) {
        if (unit == Bare.WEEK) {
            return opts.local(d.minusDays(7));
        }
        // 3월 31일의 한 달 전(2월 31일)처럼 없는 날짜는 추측하지 않는다.
        // 원본은 굴린 Date의 날이 바뀌었는지로 판단하므로 같은 순서로 본다
        YearMonth month = YearMonth.from(d).minusMonths(unit == Bare.DAY ? 1 : 12);
        LocalDateTime r = opts.local(Calendars.dayCountedFrom(month, d.getDayOfMonth()).atTime(d.toLocalTime()));
        return r.getDayOfMonth() == d.getDayOfMonth() ? r : null;
    }

    static LocalDateTime startOfWeek(LocalDateTime d, ResolveOptions opts) {
        LocalDateTime start = opts.local(d.toLocalDate().atStartOfDay());
        int dow = Calendars.weekdayNumber(start.toLocalDate());
        int diff = opts.weekStart() == WeekStart.MONDAY ? (dow + 6) % 7 : dow;
        return opts.local(start.minusDays(diff));
    }

    /** 주의 시작 요일에서 그 요일까지 며칠인지 */
    static int offsetInWeek(int weekday, WeekStart weekStart) {
        return weekStart == WeekStart.MONDAY ? (weekday + 6) % 7 : weekday;
    }
}
