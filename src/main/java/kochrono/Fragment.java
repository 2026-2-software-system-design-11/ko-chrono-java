package kochrono;

import java.util.regex.Matcher;

/** 파서가 문장에서 찾은 조각 하나. 조각은 날짜를 계산하지 않고 아는 정보(components)만 담는다 */
record Fragment(String text, int index, Components components) {
    /** 정규식이 맞은 부분 전체를 조각으로 */
    static Fragment of(Matcher m, Components components) {
        return new Fragment(m.group(), m.start(), components);
    }
}
