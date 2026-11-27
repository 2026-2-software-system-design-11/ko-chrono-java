package kochrono;

import java.time.Instant;
import java.time.ZoneId;

/**
 * toICS의 옵션. 값이 null이면 기본값을 쓴다. {@link #defaults()}에서 시작해 with 메서드로 바꾼다.
 *
 * @param title           일정 제목. 기본값은 parseEvent 결과면 그 title, parse 결과면 문장에서 찾은 부분
 * @param timezone        시각을 이 시간대의 벽시계로 쓴다(DTSTART에 TZID를 붙인다). 서머타임이 있는 곳의 반복 일정은 이것을
 *                        넘겨야 시각이 유지된다. 없으면 UTC로 쓴다. 종일 일정의 날짜도 이 시간대 기준
 * @param durationMinutes 시각은 있는데 끝이 없을 때 일정 길이(분). 기본값은 60
 * @param uid             VEVENT의 UID. 기본값은 무작위
 * @param now             DTSTAMP. 기본값은 지금
 */
public record IcsOptions(String title, ZoneId timezone, Integer durationMinutes, String uid, Instant now) {
    private static final IcsOptions DEFAULTS = new IcsOptions(null, null, null, null, null);

    public static IcsOptions defaults() {
        return DEFAULTS;
    }

    public IcsOptions withTitle(String title) {
        return new IcsOptions(title, timezone, durationMinutes, uid, now);
    }

    public IcsOptions withTimezone(ZoneId timezone) {
        return new IcsOptions(title, timezone, durationMinutes, uid, now);
    }

    /** @throws java.time.DateTimeException 없는 이름이면 */
    public IcsOptions withTimezone(String timezone) {
        return withTimezone(ZoneId.of(timezone));
    }

    public IcsOptions withDurationMinutes(int durationMinutes) {
        return new IcsOptions(title, timezone, durationMinutes, uid, now);
    }

    public IcsOptions withUid(String uid) {
        return new IcsOptions(title, timezone, durationMinutes, uid, now);
    }

    public IcsOptions withNow(Instant now) {
        return new IcsOptions(title, timezone, durationMinutes, uid, now);
    }
}
