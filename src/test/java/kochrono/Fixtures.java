package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 원본 시험의 도우미를 옮긴 것. 원본은 실행 환경의 로컬 시간대로 Date를 만들므로 여기서는 기본 시간대를 쓴다.
 * 기준 시각은 원본과 같은 2026-10-30 (금) 14:00.
 */
final class Fixtures {
    static final ZoneId ZONE = ZoneId.systemDefault();
    static final ZonedDateTime REF = d(2026, 10, 30, 14, 0);

    private Fixtures() {
    }

    /** 로컬 시간대의 y년 m월 day일 (원본의 d, 달은 1부터) */
    static ZonedDateTime d(int y, int m, int day) {
        return d(y, m, day, 0, 0);
    }

    static ZonedDateTime d(int y, int m, int day, int h) {
        return d(y, m, day, h, 0);
    }

    static ZonedDateTime d(int y, int m, int day, int h, int min) {
        return ZonedDateTime.of(y, m, day, h, min, 0, 0, ZONE);
    }

    /** 시와 분을 생략할 수 있는 d (원본의 기본값 h = 0, min = 0) */
    static ZonedDateTime d(int y, int m, int day, int[] hm) {
        return d(y, m, day, hm.length > 0 ? hm[0] : 0, hm.length > 1 ? hm[1] : 0);
    }

    /** UTC 시각 (원본의 new Date(Date.UTC(...)), 달은 1부터) */
    static ZonedDateTime utc(int y, int m, int day) {
        return utc(y, m, day, 0, 0);
    }

    static ZonedDateTime utc(int y, int m, int day, int h) {
        return utc(y, m, day, h, 0);
    }

    static ZonedDateTime utc(int y, int m, int day, int h, int min) {
        return ZonedDateTime.of(y, m, day, h, min, 0, 0, ZoneOffset.UTC);
    }

    static ParseOptions at(ZonedDateTime ref) {
        return ParseOptions.defaults().withReferenceDate(ref);
    }

    static ParseResult p(String s) {
        return KoChrono.parse(s, at(REF));
    }

    static ParseResult parse(String s, ZonedDateTime ref) {
        return KoChrono.parse(s, at(ref));
    }

    static ParseResult parse(String s, ZonedDateTime ref, String timezone) {
        return KoChrono.parse(s, at(ref).withTimezone(timezone));
    }

    static List<ParseResult> parseAll(String s, ZonedDateTime ref) {
        return KoChrono.parseAll(s, at(ref));
    }

    /** 원본의 r.recurrence?.rrule */
    static String rrule(ParseResult r) {
        return r.recurrence() != null ? r.recurrence().rrule() : null;
    }

    /** Date 비교처럼 실제 시점끼리 비교하려고 */
    static List<Instant> instants(List<ZonedDateTime> list) {
        return list.stream().map(ZonedDateTime::toInstant).toList();
    }

    /** 원본의 { reason, alternatives } */
    static Ambiguity amb(String code, ZonedDateTime... alternatives) {
        for (Ambiguity.Reason r : Ambiguity.Reason.values()) {
            if (r.code().equals(code)) {
                return new Ambiguity(r, List.of(alternatives));
            }
        }
        throw new IllegalArgumentException(code);
    }

    /** 원본의 객체 리터럴. 값이 null인 칸은 undefined처럼 빠진다 */
    static Map<String, Object> obj(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            if (kv[i + 1] != null) {
                m.put((String) kv[i], kv[i + 1]);
            }
        }
        return m;
    }

    /** toEqual: undefined 칸을 빼고 같은지 */
    static void same(Map<String, Object> expected, Map<String, Object> actual) {
        assertEquals(expected, actual);
    }

    /** toMatchObject: 기대한 칸만 같은지 */
    static void matches(Map<String, Object> expected, Map<String, Object> actual) {
        Map<String, Object> part = new LinkedHashMap<>();
        expected.keySet().forEach(k -> part.put(k, actual.get(k)));
        assertEquals(expected, part);
    }
}
