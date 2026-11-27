package kochrono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.zone.ZoneRules;

/**
 * 벽시계 시각과 실제 시점 사이의 변환. 원본의 util/timezone.ts(Intl.DateTimeFormat)를 ZoneId로 옮겼다.
 */
final class WallClock {
    private WallClock() {
    }

    /** 실제 시점을 그 시간대의 벽시계 시각으로. 원본은 그 필드로 Date를 다시 만들므로 0~99년은 1900년대가 된다 */
    static LocalDateTime toWall(ZonedDateTime instant, ZoneId zone) {
        return Calendars.constructed(instant.withZoneSameInstant(zone).toLocalDateTime());
    }

    /**
     * timezone 옵션을 준 경우: 벽시계 시각을 그 시간대의 실제 시점으로. 오프셋은 시점에 따라 달라지므로(서머타임)
     * 원본처럼 한 번 더 맞춘다. 서머타임이 시작되어 없는 시각(예: 02:30)은 원본과 같은 시점이 된다.
     */
    static ZonedDateTime fromWall(LocalDateTime wall, ZoneId zone) {
        ZoneRules rules = zone.getRules();
        // 원본의 Date.UTC도 0~99년을 1900년대로 읽는다
        Instant asUtc = Calendars.constructed(wall).toInstant(ZoneOffset.UTC);
        int first = rules.getOffset(asUtc).getTotalSeconds();
        int second = rules.getOffset(asUtc.minusSeconds(first)).getTotalSeconds();
        return asUtc.minusSeconds(second).atZone(zone);
    }

    /**
     * timezone 옵션이 없는 경우: 원본은 실행 환경의 로컬 Date로 계산한다. 여기서는 기준 시각의 시간대를 쓰고,
     * 없는 시각은 Date처럼 서머타임 전의 오프셋으로 읽는다(ZonedDateTime.of와 같다).
     */
    static ZonedDateTime local(LocalDateTime wall, ZoneId zone) {
        return ZonedDateTime.of(wall, zone);
    }
}
