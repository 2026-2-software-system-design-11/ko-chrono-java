package kochrono;

import static kochrono.ParserFixtures.one;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class AbsoluteDateParserTest {
    @Test void 연월일과_ISO() {
        Components iso = one(AbsoluteDateParser.parse("2027-01-01T09:00")).components();
        assertEquals(List.of(2027, 1, 1, 9, 0), List.of(iso.year, iso.month, iso.day, iso.hour, iso.minute));
        assertTrue(iso.meridiem);
    }

    @Test void 두_자리_연도() {
        assertEquals(2027, one(AbsoluteDateParser.parse("27.1.2")).components().year);
        assertEquals(1970, one(AbsoluteDateParser.parse("70.5.6")).components().year);
        assertEquals(1950, AbsoluteDateParser.expandYear("0050"));
    }
}
