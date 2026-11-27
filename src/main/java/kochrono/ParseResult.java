package kochrono;

import java.time.ZonedDateTime;

/**
 * 문장에서 찾은 날짜 표현 하나. 값이 없는 칸은 null이다.
 *
 * @param date       해석된 시각 (구간이면 시작, 반복이면 기준 시각 이후 첫 발생)
 * @param end        구간의 끝. "~까지", "A부터 B까지"인 경우만
 * @param text       입력에서 날짜로 인식된 부분
 * @param index      입력 안의 위치 (UTF-16 단위, String.indexOf와 같은 기준)
 * @param hasTime    시간이 명시되었는지
 * @param recurrence 반복 일정이면 그 규칙
 * @param ambiguity  해석이 갈릴 수 있으면 그 이유와 다른 후보. 사용자에게 되묻거나 다른 도구에 넘길지 판단할 때 쓴다
 * @param tense      뒤따르는 말이 과거형이면 PAST ("3시에 만났어"). 이때 연도나 날짜 없는 표현은 가까운 과거로 해석한다
 */
public record ParseResult(ZonedDateTime date, ZonedDateTime end, String text, int index, boolean hasTime,
                          Recurrence recurrence, Ambiguity ambiguity, Tense tense) {
}
