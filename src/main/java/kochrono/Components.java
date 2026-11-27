package kochrono;

/**
 * 파서들이 추출하는 중간 결과. 날짜와 시간 중 아는 것만 채우고 모르는 것은 null로 둔다.
 * 원본에서 값이 없을 때(undefined)와 같은 뜻이다. 참 거짓 표시는 원본에서 참으로만 쓰므로 boolean으로 둔다.
 */
final class Components {
    /** 1~12 */
    Integer month;

    /** 기준일로부터 일 단위 이동 (내일 = +1) */
    Long dayOffset;

    /** "1년 뒤", "내년 이맘때": 연, 월, 주 이동 때 기준일의 날짜를 유지 */
    boolean sameDay;

    /** 월 단위 이동 (다음달 = +1) */
    Long monthOffset;

    /** 월 안의 위치: 초(1일), 중순(15일), 말(마지막 날) */
    MonthPart monthPart;

    enum MonthPart { START, MIDDLE, END }
}
