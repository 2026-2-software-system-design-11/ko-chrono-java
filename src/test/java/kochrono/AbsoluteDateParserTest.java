package kochrono;

import static kochrono.ParserFixtures.one;
import static kochrono.ParserFixtures.texts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class AbsoluteDateParserTest {
    @Test void 연월일과_ISO() {
        Components c = one(AbsoluteDateParser.parse("2027년 1월 2일")).components();
        assertEquals(List.of(2027, 1, 2), List.of(c.year, c.month, c.day));
        Components iso = one(AbsoluteDateParser.parse("2027-01-01T09:00")).components();
        assertEquals(List.of(2027, 1, 1, 9, 0), List.of(iso.year, iso.month, iso.day, iso.hour, iso.minute));
        assertTrue(iso.meridiem);
    }

    @Test void 두_자리_연도() {
        assertEquals(2027, one(AbsoluteDateParser.parse("27.1.2")).components().year);
        assertEquals(1970, one(AbsoluteDateParser.parse("70.5.6")).components().year);
        assertEquals(1950, AbsoluteDateParser.expandYear("0050"));
    }

    @Test void 빗금과_한글_월일() {
        Components c = one(AbsoluteDateParser.parse("11/5")).components();
        assertEquals(List.of(11, 5), List.of(c.month, c.day));
        Components h = one(AbsoluteDateParser.parse("유월 이십오일")).components();
        assertEquals(List.of(6, 25), List.of(h.month, h.day));
        assertTrue(AbsoluteDateParser.parse("오일 교환").isEmpty());
    }

    @Test void 연도_월_일_단독() {
        assertEquals(2027, one(AbsoluteDateParser.parse("2027년에")).components().year);
        assertEquals(3, one(AbsoluteDateParser.parse("3월에 이사")).components().month);
        assertEquals(5, one(AbsoluteDateParser.parse("5일에 보자")).components().day);
        assertTrue(AbsoluteDateParser.parse("1월생").isEmpty());
    }

    @Test void 기간과_횟수는_날짜가_아니다() {
        assertTrue(AbsoluteDateParser.parse("5일간").isEmpty());
        assertTrue(AbsoluteDateParser.parse("3일째").isEmpty());
        assertEquals(List.of(), texts(AbsoluteDateParser.parse("3박 4일")));
        assertEquals(List.of(), texts(AbsoluteDateParser.parse("주 5일 근무")));
        assertEquals(List.of("5일"), texts(AbsoluteDateParser.parse("이번주 5일")));
    }
}
