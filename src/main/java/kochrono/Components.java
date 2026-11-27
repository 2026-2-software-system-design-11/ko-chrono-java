package kochrono;

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

    /** "1년 뒤", "내년 이맘때": 연, 월, 주 이동 때 기준일의 날짜를 유지 */
    boolean sameDay;

    /** "저녁", "아침"처럼 관례로 정한 시각 */
    boolean approximate;

    /** "내일 이 시간": 기준 시각의 시와 분을 쓴다 */
    boolean sameTime;

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

    /** 이 조각이 "~까지" 종료점인지 */
    boolean isEnd;

    enum MonthPart { START, MIDDLE, END }

    enum LunarHoliday { SEOLLAL, CHUSEOK, BUDDHA }
}
