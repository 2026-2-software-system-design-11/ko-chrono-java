package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class IcsOptionsTest {
    @Test void with로_바꾼다() {
        IcsOptions o = IcsOptions.defaults().withTitle("회의").withTimezone("Asia/Seoul").withDurationMinutes(30)
                .withUid("u").withNow(Instant.EPOCH);
        assertEquals(new IcsOptions("회의", ZoneId.of("Asia/Seoul"), 30, "u", Instant.EPOCH), o);
    }
}
