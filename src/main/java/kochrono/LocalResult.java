package kochrono;

import java.time.LocalDateTime;

/**
 * 벽시계 시각으로 계산한 결과.
 */
final class LocalResult {
    LocalDateTime date;
    LocalDateTime end;
    String text = "";
    int index;
    boolean hasTime;

    /** 그룹의 마지막 조각이 끝나는 위치. 원본의 _end */
    int endIndex;

    /** 담은 시각이 모두 원본의 Date로 나타낼 수 있는 범위인지 */
    boolean valid() {
        boolean ok = Calendars.representable(date) && (end == null || Calendars.representable(end));
        return ok;
    }
}
