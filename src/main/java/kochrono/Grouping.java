package kochrono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/** 인접한 조각을 하나의 표현으로 묶고, 묶인 정보의 종류를 판단한다 */
final class Grouping {
    // 조각 사이에 와도 같은 표현으로 묶는 것: "내일 오후", "내일은 오후", "토요일쯤 저녁", "모레도 2시".
    // "이나", "아니면", "랑"은 다른 선택지일 수 있어 묶지 않는다
    private static final Pattern JOINER = JsRegex.compile("^(?:\\s|,|에|의|엔|은|는|도|쯤|경|께|부터)*$");

    private Grouping() {
    }

    /**
     * 인접한 조각을 하나로 합친다. 예: "다음주 화요일"과 "오후 3시"는 하나의 결과.
     * 사이에 공백, 쉼표, 조사(에, 의, 엔, 은, 는, 도), 어림말(쯤, 경, 께)만 있고 정보가 겹치지 않을 때 묶는다.
     */
    static List<List<Fragment>> group(List<Fragment> fragments, String text) {
        List<Fragment> sorted = new ArrayList<>(fragments);
        sorted.sort(Comparator.comparingInt(Fragment::index));
        List<List<Fragment>> groups = new ArrayList<>();
        for (Fragment f : sorted) {
            if (!groups.isEmpty()) {
                List<Fragment> g = groups.get(groups.size() - 1);
                Fragment last = g.get(g.size() - 1);
                String between = Texts.slice(text, last.index() + last.text().length(), f.index());
                boolean conflict = hasConflict(merge(g), f.components());
                if (JOINER.matcher(between).find() && !conflict) {
                    g.add(f);
                    continue;
                }
            }
            List<Fragment> g = new ArrayList<>();
            g.add(f);
            groups.add(g);
        }
        return groups;
    }

    static Components merge(List<Fragment> group) {
        return Components.merge(group.stream().map(Fragment::components).toList());
    }

    /** 원본의 DATE_KEYS(연, 월, 일, 요일, 각종 이동, 음력) 중 하나라도 있는지 */
    static boolean hasDate(Components c) {
        return c.day != null || c.weekday != null || hasDateBesidesDayAndWeekday(c);
    }

    private static boolean hasDateBesidesDayAndWeekday(Components c) {
        return c.year != null || c.yearOffset != null || c.month != null || c.dayOffset != null || c.weekOffset != null
                || c.monthOffset != null || c.monthPart != null || c.lunar != null || c.lunarDate != null;
    }

    private static boolean hasRelative(Components c) {
        return c.hourOffset != null || c.minuteOffset != null;
    }

    /** 날짜 정보 없이 시각만 있는 조각인지 ("5시까지", "17:00") */
    static boolean isTimeOnly(Components c) {
        return c.hour != null && c.recur == null && !hasRelative(c) && !hasDate(c);
    }

    /** 날짜 정보가 요일뿐인 조각인지 ("금요일까지") */
    static boolean isWeekdayOnly(Components c) {
        return c.weekday != null && c.day == null && !hasDateBesidesDayAndWeekday(c);
    }

    /** 날짜 정보가 "N일"뿐인 조각인지 ("3일까지") */
    static boolean isDayOnly(Components c) {
        return c.day != null && c.weekday == null && !hasDateBesidesDayAndWeekday(c);
    }

    /**
     * 시 범위의 끝을 시작에 맞춘다 (규칙 16).
     * 끝을 시작과 같은 날에 놓고, 시작보다 이르면 오전, 오후를 이어받거나(+12시간) 다음날로 넘긴다.
     * "오후 3~5시"는 17시, "오후 11시부터 1시까지"는 다음날 1시, "오후 3시부터 오전 1시까지"는 다음날 1시
     */
    static LocalDateTime alignEnd(LocalDateTime start, Components end) {
        int hour = end.hour;
        LocalDateTime r = Calendars.atClock(start.toLocalDate(), hour, end.minute != null ? end.minute : 0);
        if (!r.isAfter(start)) {
            if (!end.meridiem && hour < 12 && hour + 12 > start.getHour()) {
                r = Calendars.atClock(r.toLocalDate(), hour + 12, r.getMinute());
            } else {
                r = r.plusDays(1);
            }
        }
        return r;
    }

    private static boolean hasDayKey(Components c) {
        return c.day != null || c.dayOffset != null || c.weekday != null || c.monthPart != null;
    }

    /** "11월 5일 목요일": 날짜 뒤의 요일은 덧붙이는 말이라 충돌이 아니다 */
    private static boolean weekdayNote(Components x, Components y) {
        return x.day != null && y.weekday != null && !y.weekend && y.weekOffset == null && y.day == null
                && y.dayOffset == null && y.monthPart == null;
    }

    /** 같은 종류의 정보가 두 번 나오면 다른 날짜 표현으로 본다 (예: "3일 5일") */
    static boolean hasConflict(Components a, Components b) {
        boolean dayConflict = hasDayKey(a) && hasDayKey(b) && !weekdayNote(a, b) && !weekdayNote(b, a);
        boolean bothHour = a.hour != null && b.hour != null;
        // 규칙 15: "내일"과 "30분 후"는 서로 다른 표현
        boolean dateVsRel = (hasDate(a) && hasRelative(b)) || (hasRelative(a) && hasDate(b));
        // 규칙 34: "다음주부터 매주 월요일"의 날짜는 반복의 시작, 끝이라 따로 해석한 뒤 KoChrono가 묶는다
        boolean recurVsDate = (a.recur != null && hasDate(b)) || (b.recur != null && hasDate(a));
        return dayConflict || bothHour || dateVsRel || recurVsDate;
    }
}
