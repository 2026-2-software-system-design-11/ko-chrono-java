package kochrono;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;

/** 묶인 조각을 기준 시각에 대입해 날짜로 만든다. 모든 날짜 계산은 여기와 RecurrenceResolver에서만 한다 */
final class Resolver {
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
        Fragment last = group.get(group.size() - 1);
        result.endIndex = last.index() + last.text().length();
        return result;
    }

    /** 날짜 계산 중의 상태 */
    static final class DateState {
        LocalDateTime d;
        boolean dateSet;

        DateState(LocalDateTime d) {
            this.d = d;
        }
    }

    /** 날짜 부분. 없는 날짜면 false */
    private static boolean resolveDay(Components c, ResolveOptions opts, DateState s) {
        return true;
    }

    /** 시각 부분. 결과가 없으면 null */
    private static LocalDateTime resolveTime(Components c, ResolveOptions opts, DateState s) {
        LocalDateTime ref = opts.referenceDate();
        int minute = c.minute != null ? c.minute : 0;
        if (c.hour != null && !s.dateSet) {
            // 시간만 있고 날짜가 없으면 오늘, 이미 지났으면 내일
            LocalDateTime d = Calendars.atClock(ref.toLocalDate(), clockHour(c), minute);
            return d.isBefore(ref) ? d.plusDays(1) : d;
        }
        return null;
    }

    /** 규칙 20: 오전, 오후 없는 1~6시는 오후 ("3시에 보자"는 15시). 7~11시는 그대로 */
    static int clockHour(Components c) {
        int h = c.hour;
        return !c.meridiem && h >= 1 && h <= 6 ? h + 12 : h;
    }
}
