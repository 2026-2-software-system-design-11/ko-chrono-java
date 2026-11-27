package kochrono;

import java.time.ZonedDateTime;

/**
 * parseEvent의 결과: 할 일이나 일정 한 줄을 제목과 시점으로 나눈 것. 값이 없는 칸은 null이다.
 *
 * @param title      날짜 부분을 뺀 나머지 ("내일 3시 치과"는 "치과"). 없으면 빈 문자열
 * @param date       첫 번째 날짜 표현의 date. 날짜가 없으면 null
 * @param end        구간의 끝이나 마감
 * @param hasTime    시간이 명시되었는지
 * @param recurrence 반복 일정이면 그 규칙
 * @param ambiguity  해석이 갈릴 수 있으면 그 이유와 다른 후보 ("3시"는 15시, 후보 3시)
 * @param tense      과거형 문장이면 PAST ("어제 3시에 만났어")
 */
public record ParsedEvent(String title, ZonedDateTime date, ZonedDateTime end, boolean hasTime, Recurrence recurrence,
                          Ambiguity ambiguity, Tense tense) {
}
