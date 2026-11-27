package kochrono;

import static kochrono.ParserFixtures.one;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class RecurrenceParserTest {
    private static String rule(String s) {
        return Rrule.of(one(RecurrenceParser.parse(s)).components().recur, false);
    }

    @Test void 요일_반복() {
        assertEquals("FREQ=WEEKLY;BYDAY=MO,WE,FR", rule("매주 월수금"));
        assertEquals("FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR", rule("매주 월~금"));
        assertEquals("FREQ=WEEKLY;INTERVAL=2;BYDAY=FR", rule("격주 금요일"));
        assertEquals("FREQ=WEEKLY;BYDAY=SA,SU", rule("주말마다"));
        assertEquals("FREQ=WEEKLY;BYDAY=TU,TH", rule("화요일이랑 목요일마다"));
        assertEquals(List.of(5, 6, 0), RecurrenceParser.weekdaysOf("금~일"));
    }

    @Test void 달과_해_반복() {
        assertEquals("FREQ=MONTHLY;BYMONTHDAY=15", rule("매달 15일"));
        assertEquals("FREQ=MONTHLY;BYMONTHDAY=-1", rule("매월 말일"));
        assertEquals("FREQ=MONTHLY;BYDAY=1MO", rule("매달 첫째 주 월요일"));
        assertEquals("FREQ=YEARLY;INTERVAL=2;BYMONTH=3;BYMONTHDAY=1", rule("2년마다 3월 1일"));
    }

    @Test void 일_시간_분_반복과_주기_말만() {
        assertEquals("FREQ=DAILY", rule("매일"));
        assertEquals("FREQ=DAILY;INTERVAL=3", rule("3일마다"));
        assertEquals("FREQ=HOURLY;INTERVAL=2", rule("2시간마다"));
        assertEquals("FREQ=MINUTELY;INTERVAL=30", rule("30분마다"));
        assertEquals("FREQ=MONTHLY;INTERVAL=2", rule("2개월마다"));
        assertEquals("FREQ=WEEKLY;INTERVAL=2", rule("격주로"));
        assertTrue(RecurrenceParser.parse("줄에 매달려").isEmpty());
    }

    @Test void 매주와_며칠은_같이_실어_보낸다() {
        Components c = one(RecurrenceParser.parse("매주 14일")).components();
        assertEquals(14, c.day);
        assertEquals(Frequency.WEEKLY, c.recur.freq());
    }
}
