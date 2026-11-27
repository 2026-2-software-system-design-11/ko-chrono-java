package kochrono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;

/** resolve가 여러 곳에서 쓰는 날짜 계산 */
final class Calendars {
    /** 원본의 Date가 담을 수 있는 시점의 한도(초). 1970년에서 앞뒤로 1억 일. 넘으면 원본은 Invalid Date가 된다 */
    private static final long MAX_SECONDS = 8_640_000_000_000L;

    private Calendars() {
    }

    /** 요일 번호. 0=일 ... 6=토 (원본의 요일 번호 규약) */
    static int weekdayNumber(LocalDate d) {
        return d.getDayOfWeek().getValue() % 7;
    }

    /** 그날 0시에서 hour시간 minute분 뒤. "0시 10분 전"(-1시 -10분)은 전날 23:50이 된다 */
    static LocalDateTime atClock(LocalDate day, long hour, long minute) {
        return day.atStartOfDay().plusHours(hour).plusMinutes(minute);
    }

    /**
     * 그 달 1일부터 세어 day번째 날. 그 달에 없는 날은 넘치는 만큼 다음 달로 간다(2월 30일은 3월 2일).
     * 원본이 없는 날짜를 이렇게 다루는 자리(지났는지 비교, 끝에 일만 있는 구간, N달간)에서만 쓴다.
     */
    static LocalDate dayCountedFrom(YearMonth month, long day) {
        return month.atDay(1).plusDays(day - 1);
    }

    /** 그 달의 day일. 그 달에 없으면 말일 (2월 29일에서 1년 뒤는 2월 28일) */
    static LocalDate sameDayIn(YearMonth month, int day) {
        return month.atDay(Math.min(day, month.lengthOfMonth()));
    }

    /** 그 달의 N번째 요일이 며칠인지. nth가 -1이면 마지막. 없으면 null ("다섯째 주 토요일") */
    static Integer nthWeekday(YearMonth month, int weekday, int nth) {
        int last = month.lengthOfMonth();
        int day = 1 + ((weekday - weekdayNumber(month.atDay(1)) + 7) % 7);
        if (nth > 0) {
            day += (nth - 1) * 7;
        } else {
            while (day + 7 <= last) {
                day += 7;
            }
        }
        return day > last ? null : day;
    }

    /** 원본의 Date로 나타낼 수 있는 시점인지 */
    static boolean representable(LocalDateTime d) {
        return Math.abs(d.toEpochSecond(ZoneOffset.UTC)) <= MAX_SECONDS;
    }
}
