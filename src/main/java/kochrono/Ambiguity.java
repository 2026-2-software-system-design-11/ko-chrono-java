package kochrono;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 해석이 갈릴 수 있다는 표시 (규칙 31). 기본 해석은 결과의 date(마감이면 end)이고, 다른 후보가 alternatives다.
 *
 * @param reason       갈리는 이유
 * @param alternatives 다른 후보. 관례 시각이면 비어 있다
 */
public record Ambiguity(Reason reason, List<ZonedDateTime> alternatives) {
    public Ambiguity {
        Objects.requireNonNull(reason);
        alternatives = List.copyOf(alternatives);
    }

    /** 갈리는 이유. code는 원본의 문자열 값이다 */
    public enum Reason {
        /** 오전, 오후 없는 시각 ("3시"는 15시, 후보 3시) */
        MERIDIEM("meridiem"),
        /** "다음 토요일"이 이번 주의 다음 토요일인지 다음 주 토요일인지 */
        NEXT_WEEKDAY("next-weekday"),
        /** "저녁", "아침"처럼 관례로 정한 시각 (후보 없음) */
        APPROXIMATE_TIME("approximate-time");

        private final String code;

        Reason(String code) {
            this.code = code;
        }

        public String code() {
            return code;
        }
    }
}
