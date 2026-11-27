package kochrono;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 반복 일정. 필드 이름과 값은 RFC 5545 RRULE을 따른다. 값이 없는 칸은 null이다.
 *
 * @param freq         주기
 * @param interval     몇 번째마다 (격주는 2)
 * @param byWeekday    요일들. 없으면 빈 목록
 * @param byWeekdayNth 매달 N번째 요일 ("마지막 주 금요일"은 -1). byWeekday와 함께 쓴다
 * @param byMonthDay   매달 며칠 (말일은 -1)
 * @param byMonth      매년 몇 월
 * @param until        마지막 발생 한도 ("다음달까지 매주 월요일"은 다음달 말일 23:59). count와 함께 오지 않는다
 * @param count        전체 횟수 ("총 8회", "10번 반복")
 * @param rrule        RRULE 문자열. date를 DTSTART로 함께 쓰면 캘린더에 그대로 넣을 수 있다.
 *                     UNTIL은 date와 같은 벽시계 시각(floating time)으로 쓰고, 시각이 없으면 날짜만 쓴다.
 *                     DTSTART를 UTC나 TZID로 쓰는 캘린더 파일에는 toICS가 UNTIL을 UTC로 바꿔 넣는다
 */
public record Recurrence(Frequency freq, int interval, List<DayOfWeek> byWeekday, Integer byWeekdayNth,
                         Integer byMonthDay, Integer byMonth, ZonedDateTime until, Integer count, String rrule) {
    public Recurrence {
        Objects.requireNonNull(freq);
        byWeekday = List.copyOf(byWeekday);
    }
}
