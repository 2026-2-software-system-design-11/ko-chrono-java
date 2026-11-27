package kochrono;

import static kochrono.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.*;

/** 원본 tests/parse.test.ts의 공휴일, 기념일, 명절 (규칙 29)부터 업무용 한자어 (규칙 33)까지. 기준: 2026-10-30 (금) 14:00 */
class Parse14Test {
    @Nested @DisplayName("공휴일, 기념일, 명절 (규칙 29)")
    class G37 {
        @Test @DisplayName("크리스마스") void c350() { assertEquals(d(2026, 12, 25), p("크리스마스에 뭐 해?").date()); }
        @Test @DisplayName("크리스마스 이브 저녁 7시") void c351() { assertEquals(d(2026, 12, 24, 19), p("크리스마스 이브 저녁 7시").date()); }
        @Test @DisplayName("빼빼로데이") void c352() { assertEquals(d(2026, 11, 11), p("빼빼로데이").date()); }
        @Test @DisplayName("한글날 (지났으면 내년)") void c353() { assertEquals(d(2027, 10, 9), p("한글날").date()); }
        @Test @DisplayName("광복절 전날") void c354() { assertEquals(d(2027, 8, 14), p("광복절 전날").date()); }
        @Test @DisplayName("크리스마스까지") void c355() { assertEquals(d(2026, 12, 25, 23, 59), p("크리스마스까지").end()); }
        // 음력 명절: 한국천문연구원 자료가 있는 2025~2028년만
        @Test @DisplayName("설날") void c356() { assertEquals(d(2027, 2, 7), p("설날").date()); }
        @Test @DisplayName("구정") void c357() { assertEquals(d(2027, 2, 7), p("구정 때 내려가").date()); }
        @Test @DisplayName("설 연휴") void c358() {
            ParseResult r = p("설 연휴에 여행 가자");
            assertEquals(Arrays.asList(d(2027, 2, 6), d(2027, 2, 8, 23, 59)), Arrays.asList(r.date(), r.end()));
            assertEquals("설 연휴", r.text());
        }
        @Test @DisplayName("추석 (올해 것은 지났으므로 내년)") void c359() { assertEquals(d(2027, 9, 15), p("추석").date()); }
        @Test @DisplayName("추석 전날") void c360() { assertEquals(d(2027, 9, 14), p("추석 전날").date()); }
        @Test @DisplayName("한가위 다음날") void c361() { assertEquals(d(2027, 9, 16), p("한가위 다음날").date()); }
        @Test @DisplayName("부처님 오신 날") void c362() { assertEquals(d(2027, 5, 13), p("부처님 오신 날").date()); }
        @Test @DisplayName("2028년 설날") void c363() { assertEquals(d(2028, 1, 27), p("2028년 설날").date()); }
        @Test @DisplayName("내년 추석") void c364() { assertEquals(d(2027, 9, 15), p("내년 추석").date()); }
        @Test @DisplayName("올해 추석 (지났어도 올해)") void c365() { assertEquals(d(2026, 9, 25), p("올해 추석").date()); }
        @Test @DisplayName("2030년 설날 (공식 자료 없음 → null)") void c366() { assertNull(p("2030년 설날")); }
        @Test @DisplayName("설명해줘 (잡지 않음)") void c367() { assertNull(p("설명해줘")); }
        @Test @DisplayName("설거지 (잡지 않음)") void c368() { assertNull(p("설거지 내가 할게")); }
    }

    @Nested @DisplayName("퍼즈 테스트가 찾은 버그")
    class G38 {
        @Test @DisplayName("어제까지 (지난 마감은 그 시점)") void c369() {
            ParseResult r = p("어제까지 냈어야 했는데");
            assertEquals(d(2026, 10, 29, 23, 59), r.date());
            assertEquals(d(2026, 10, 29, 23, 59), r.end());
        }
        @Test @DisplayName("0일간 (기간 아님)") void c370() { assertNull(p("오늘부터 0일간").end()); }
        @Test @DisplayName("추석 ~ 11월 (거꾸로 된 구간은 합치지 않음)") void c371() { assertEquals(2, parseAll("추석 ~ 11월", REF).size()); }
        @Test @DisplayName("2028년후 (2028년 뒤로 읽지 않음)") void c372() { assertEquals(2028, p("2028년후").date().getYear()); }
        @Test @DisplayName("3년 후") void c373() { assertEquals(d(2029, 10, 30), p("3년 후").date()); }
        @Test @DisplayName("302028년 뒤 (Invalid Date 대신 null)") void c374() { assertNull(p("302028년 뒤")); }
    }

    @Nested @DisplayName("업무용 한자어 (규칙 33)")
    class G39 {
        // 주
        // 한자어를 무시하고 요일만 잡으면 틀리게 나오는 문장으로 검증한다
        @Test @DisplayName("차주 금요일 (요일만 잡으면 오늘)") void c375() { assertEquals(d(2026, 11, 6), p("차주 금요일").date()); }
        @Test @DisplayName("금주 월요일 (이미 지난 이번 주 월요일)") void c376() { assertEquals(d(2026, 10, 26), p("금주 월요일").date()); }
        @Test @DisplayName("전주 수요일") void c377() { assertEquals(d(2026, 10, 21), p("전주 수요일").date()); }
        @Test @DisplayName("익주 금요일") void c378() { assertEquals(d(2026, 11, 6), p("익주 금요일").date()); }
        @Test @DisplayName("차주 중으로 회신") void c379() {
            ParseResult r = p("차주 중으로 회신 부탁드립니다");
            assertEquals(d(2026, 11, 8, 23, 59), r.end());
            assertEquals("차주 중으로", r.text());
        }
        @Test @DisplayName("금주 내로 제출") void c380() { assertEquals(d(2026, 11, 1, 23, 59), p("금주 내로 제출").end()); }
        @Test @DisplayName("차주에 회의") void c381() { assertEquals(d(2026, 11, 2), p("차주에 회의 일정 공유드립니다").date()); }
        // 일, 월, 년
        @Test @DisplayName("금일 오전 10시 (지났어도 오늘)") void c382() { assertEquals(d(2026, 10, 30, 10), p("금일 오전 10시").date()); }
        @Test @DisplayName("명일 오후 3시") void c383() { assertEquals(d(2026, 10, 31, 15), p("명일 오후 3시").date()); }
        @Test @DisplayName("익일 배송") void c384() { assertEquals(d(2026, 10, 31), p("익일 배송").date()); }
        @Test @DisplayName("작일") void c385() { assertEquals(d(2026, 10, 29), p("작일 보내드린 자료").date()); }
        @Test @DisplayName("익월 초") void c386() { assertEquals(d(2026, 11, 1), p("익월 초").date()); }
        @Test @DisplayName("금월 말까지") void c387() { assertEquals(d(2026, 10, 31, 23, 59), p("금월 말까지").end()); }
        @Test @DisplayName("전월") void c388() { assertEquals(d(2026, 9, 1), p("전월 실적").date()); }
        @Test @DisplayName("익년 12월") void c389() { assertEquals(d(2027, 12, 1), p("익년 12월").date()); }
        // 다른 뜻 (잡지 않음)
        @Test @DisplayName("금주 결심했어 (禁酒)") void c390() { assertNull(p("금주 결심했어")); }
        @Test @DisplayName("금주에 성공했어 (禁酒)") void c391() { assertNull(p("금주에 성공했어")); }
        @Test @DisplayName("금주 중이야 (禁酒)") void c392() { assertNull(p("금주 중이야")); }
        @Test @DisplayName("전주 여행 (도시)") void c393() { assertNull(p("전주 여행 가자")); }
        @Test @DisplayName("전주에 갔다 (도시일 수도)") void c394() { assertNull(p("전주에 갔다")); }
        @Test @DisplayName("차주에게 연락 (車主)") void c395() { assertNull(p("차주에게 연락했어요")); }
        @Test @DisplayName("전월세 계약") void c396() { assertNull(p("전월세 계약")); }
    }
}
