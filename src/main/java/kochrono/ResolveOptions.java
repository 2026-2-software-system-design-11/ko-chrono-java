package kochrono;

import java.time.LocalDateTime;

/** resolve가 쓰는 옵션. 기준 시각은 계산할 시간대의 벽시계 시각이다 */
record ResolveOptions(LocalDateTime referenceDate, WeekStart weekStart, int defaultHour) {
    ResolveOptions withReferenceDate(LocalDateTime ref) {
        return new ResolveOptions(ref, weekStart, defaultHour);
    }
}
