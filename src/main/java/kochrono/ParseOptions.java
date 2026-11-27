package kochrono;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * parse 계열 함수의 옵션. 값이 null이면 기본값을 쓴다. {@link #defaults()}에서 시작해 with 메서드로 바꾼다.
 *
 * @param referenceDate 기준 시각. 기본값은 지금
 * @param weekStart     주의 시작 요일. 기본값은 MONDAY
 * @param defaultHour   시간이 명시되지 않았을 때 넣을 시. 기본값은 0 (00:00)
 * @param timezone      "오후 3시"를 어느 시간대의 3시로 볼지. 기본값은 referenceDate의 시간대.
 *                      서버(UTC)에서 한국 사용자 문장을 처리할 때는 Asia/Seoul을 넘긴다
 */
public record ParseOptions(ZonedDateTime referenceDate, WeekStart weekStart, Integer defaultHour, ZoneId timezone) {
    private static final ParseOptions DEFAULTS = new ParseOptions(null, null, null, null);

    public static ParseOptions defaults() {
        return DEFAULTS;
    }

    public ParseOptions withReferenceDate(ZonedDateTime referenceDate) {
        return new ParseOptions(referenceDate, weekStart, defaultHour, timezone);
    }

    public ParseOptions withWeekStart(WeekStart weekStart) {
        return new ParseOptions(referenceDate, weekStart, defaultHour, timezone);
    }

    public ParseOptions withDefaultHour(int defaultHour) {
        return new ParseOptions(referenceDate, weekStart, defaultHour, timezone);
    }

    public ParseOptions withTimezone(ZoneId timezone) {
        return new ParseOptions(referenceDate, weekStart, defaultHour, timezone);
    }

    /**
     * IANA 이름('Asia/Seoul', 'UTC', 'America/New_York')으로 시간대를 정한다.
     *
     * @throws java.time.DateTimeException 없는 이름이면
     */
    public ParseOptions withTimezone(String timezone) {
        return withTimezone(ZoneId.of(timezone));
    }
}
