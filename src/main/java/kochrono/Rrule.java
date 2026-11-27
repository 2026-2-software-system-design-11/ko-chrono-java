package kochrono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** 반복 규칙을 RFC 5545 RRULE 문자열로 쓴다 */
final class Rrule {
    private static final String[] CODE = {"SU", "MO", "TU", "WE", "TH", "FR", "SA"};

    private Rrule() {
    }

    /**
     * 요일은 월요일부터 쓴다. UNTIL은 벽시계 시각 그대로 쓴다(DTSTART가 TZID나 floating time일 때의 형식).
     * dateOnly면 DTSTART가 DATE이므로 UNTIL도 날짜만 쓴다.
     */
    static String of(RecurSpec r, boolean dateOnly) {
        List<String> parts = new ArrayList<>();
        parts.add("FREQ=" + r.freq());
        if (r.interval() > 1) {
            parts.add("INTERVAL=" + r.interval());
        }
        if (!r.byWeekday().isEmpty()) {
            String nth = r.byWeekdayNth() != null && r.byWeekdayNth() != 0 ? String.valueOf(r.byWeekdayNth()) : "";
            List<String> days = r.byWeekday().stream()
                    .sorted(Comparator.comparingInt(d -> (d + 6) % 7))
                    .map(d -> nth + CODE[d])
                    .toList();
            parts.add("BYDAY=" + String.join(",", days));
        }
        return String.join(";", parts);
    }

    /** {@code String(n).padStart(2, '0')} */
    static String pad(int n) {
        String s = String.valueOf(n);
        return s.length() >= 2 ? s : "0" + s;
    }
}
