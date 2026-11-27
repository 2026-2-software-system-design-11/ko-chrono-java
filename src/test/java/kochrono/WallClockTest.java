package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class WallClockTest {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final ZoneId NEW_YORK = ZoneId.of("America/New_York");

    @Test void 벽시계와_실제_시점() {
        ZonedDateTime t = Instant.parse("2026-10-30T05:00:00Z").atZone(ZoneId.of("UTC"));
        assertEquals(LocalDateTime.of(2026, 10, 30, 14, 0), WallClock.toWall(t, SEOUL));
        assertEquals(Instant.parse("2026-10-30T06:00:00Z"), WallClock.fromWall(LocalDateTime.of(2026, 10, 30, 15, 0), SEOUL).toInstant());
    }

    @Test void 서머타임() {
        // 11월 1일에 EDT(-4)에서 EST(-5)로 바뀐다
        assertEquals(Instant.parse("2026-11-02T05:00:00Z"), WallClock.fromWall(LocalDateTime.of(2026, 11, 2, 0, 0), NEW_YORK).toInstant());
        // 겹치는 1시 반은 앞의 오프셋
        assertEquals(Instant.parse("2026-11-01T05:30:00Z"), WallClock.fromWall(LocalDateTime.of(2026, 11, 1, 1, 30), NEW_YORK).toInstant());
        // 없는 2시 반(3월 8일)은 원본의 계산처럼 1시 반 EST
        assertEquals(Instant.parse("2026-03-08T06:30:00Z"), WallClock.fromWall(LocalDateTime.of(2026, 3, 8, 2, 30), NEW_YORK).toInstant());
    }
}
