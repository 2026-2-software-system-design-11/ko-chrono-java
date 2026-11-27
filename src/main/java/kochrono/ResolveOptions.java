package kochrono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * resolve가 쓰는 옵션. 기준 시각은 계산할 시간대의 벽시계 시각이다.
 * localZone은 원본이 로컬 Date로 계산하는 시간대다. timezone 옵션을 준 경우는 null이다.
 */
record ResolveOptions(LocalDateTime referenceDate, WeekStart weekStart, int defaultHour, ZoneId localZone) {
    ResolveOptions(LocalDateTime referenceDate, WeekStart weekStart, int defaultHour) {
        this(referenceDate, weekStart, defaultHour, null);
    }

    ResolveOptions withReferenceDate(LocalDateTime ref) {
        return new ResolveOptions(ref, weekStart, defaultHour, localZone);
    }

    /**
     * 원본이 로컬 Date를 만들거나 고친 값. 서머타임이 시작되어 없는 벽시계 시각은 Date처럼 그 자리에서
     * 뒤로 밀린다(로스앤젤레스 3월 둘째 일요일 02:30은 03:30). 밀린 값으로 다음 비교와 계산을 한다.
     */
    LocalDateTime local(LocalDateTime wall) {
        return localZone == null ? wall : ZonedDateTime.of(wall, localZone).toLocalDateTime();
    }
}
