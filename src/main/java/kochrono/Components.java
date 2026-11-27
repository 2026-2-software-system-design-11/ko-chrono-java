package kochrono;

import java.util.List;

/**
 * 파서들이 추출하는 중간 결과. 날짜와 시간 중 아는 것만 채우고 모르는 것은 null로 둔다.
 * 원본에서 값이 없을 때(undefined)와 같은 뜻이다. 참 거짓 표시는 원본에서 참으로만 쓰므로 boolean으로 둔다.
 */
final class Components {
    Integer year;

    /** 1~12 */
    Integer month;
    Integer day;

    /** 0~23 */
    Integer hour;
    Integer minute;

    /** 기준일로부터 일 단위 이동 (내일 = +1) */
    Long dayOffset;

    /** 기준 주로부터 주 단위 이동 (다음주 = +1) */
    Integer weekOffset;

    /** 0=일 ... 6=토 (원본의 요일 번호 규약) */
    Integer weekday;

    /** "주말" 단독: 오늘이 토, 일이면 오늘, 아니면 다가오는 토요일 */
    boolean weekend;

    /** "지난 금요일"(오늘 제외 과거)과 "다음 금요일"(오늘 제외 미래) */
    WeekdayDir weekdayDir;

    /** "1년 뒤", "내년 이맘때": 연, 월, 주 이동 때 기준일의 날짜를 유지 */
    boolean sameDay;

    /** "저녁", "아침"처럼 관례로 정한 시각 */
    boolean approximate;

    /** "내일 이 시간": 기준 시각의 시와 분을 쓴다 */
    boolean sameTime;

    /** 음력 명절. 양력 날짜는 resolve가 음력 표에서 찾는다 */
    LunarHoliday lunar;

    /** "음력 3월 3일", "음력 윤5월 1일" */
    LunarDate lunarDate;

    /** "매주 월요일", "매달 15일": 반복 규칙 (RRULE 문자열은 resolve가 만든다) */
    RecurSpec recur;

    /** 연 단위 이동 (내년 = +1). 있으면 "지난 월일은 내년" 규칙을 쓰지 않는다 */
    Integer yearOffset;

    /** 월 단위 이동 (다음달 = +1) */
    Long monthOffset;

    /** 월 안의 위치: 초(1일), 중순(15일), 말(마지막 날) */
    MonthPart monthPart;

    /** 오전, 오후 등이 명시됨 (15:30 같은 24시간 표기 포함). 시 범위의 끝을 맞출 때 쓴다 */
    boolean meridiem;

    /** "자정", "24시", "밤 12시": 날짜가 있으면 그 날의 끝(다음날 00:00) */
    boolean midnight;

    /** 시 단위 상대 이동 */
    Long hourOffset;

    /** 분 단위 상대 이동 */
    Long minuteOffset;

    /** "3일간", "2주 동안", "3박 4일": 앞의 날짜부터 이어지는 기간. 날짜 없이 단독이면 결과가 없다 */
    Long durationDays;
    Long durationMonths;
    Long durationMinutes;

    /** "N째 주": 1~5, 마지막은 -1 */
    Integer nthWeek;

    /** "이번 주 내내": 그 기간 전체 */
    boolean whole;

    /** 뒤따르는 말이 과거형 ("3시에 만났어") */
    boolean past;

    /** 이 조각이 "~까지" 종료점인지 */
    boolean isEnd;

    /** 이 조각이 "~부터" 시작점인지 */
    boolean isStart;

    enum WeekdayDir { PAST, NEXT }

    enum MonthPart { START, MIDDLE, END }

    enum LunarHoliday { SEOLLAL, CHUSEOK, BUDDHA }

    record LunarDate(int month, int day, boolean leap) {
    }

    /** 원본의 {@code Object.assign({}, ...list)}: 뒤에 오는 조각의 값이 앞의 값을 덮는다 */
    static Components merge(List<Components> list) {
        Components c = new Components();
        for (Components x : list) {
            c.year = pick(x.year, c.year);
            c.month = pick(x.month, c.month);
            c.day = pick(x.day, c.day);
            c.hour = pick(x.hour, c.hour);
            c.minute = pick(x.minute, c.minute);
            c.dayOffset = pick(x.dayOffset, c.dayOffset);
            c.weekOffset = pick(x.weekOffset, c.weekOffset);
            c.weekday = pick(x.weekday, c.weekday);
            c.weekdayDir = pick(x.weekdayDir, c.weekdayDir);
            c.lunar = pick(x.lunar, c.lunar);
            c.lunarDate = pick(x.lunarDate, c.lunarDate);
            c.recur = pick(x.recur, c.recur);
            c.yearOffset = pick(x.yearOffset, c.yearOffset);
            c.monthOffset = pick(x.monthOffset, c.monthOffset);
            c.monthPart = pick(x.monthPart, c.monthPart);
            c.hourOffset = pick(x.hourOffset, c.hourOffset);
            c.minuteOffset = pick(x.minuteOffset, c.minuteOffset);
            c.durationDays = pick(x.durationDays, c.durationDays);
            c.durationMonths = pick(x.durationMonths, c.durationMonths);
            c.durationMinutes = pick(x.durationMinutes, c.durationMinutes);
            c.nthWeek = pick(x.nthWeek, c.nthWeek);
            c.weekend |= x.weekend;
            c.sameDay |= x.sameDay;
            c.approximate |= x.approximate;
            c.sameTime |= x.sameTime;
            c.meridiem |= x.meridiem;
            c.midnight |= x.midnight;
            c.whole |= x.whole;
            c.past |= x.past;
            c.isEnd |= x.isEnd;
            c.isStart |= x.isStart;
        }
        return c;
    }

    private static <T> T pick(T later, T earlier) {
        return later != null ? later : earlier;
    }
}
