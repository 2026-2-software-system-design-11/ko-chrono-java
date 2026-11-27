package kochrono;

import java.util.List;

/** 문장에서 한 종류의 표현을 찾아 조각으로 돌려준다. 파서는 날짜를 계산하지 않는다 */
@FunctionalInterface
interface Parser {
    List<Fragment> parse(String text);
}
