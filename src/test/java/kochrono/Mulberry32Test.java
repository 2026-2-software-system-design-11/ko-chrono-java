package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Mulberry32Test {
    @Test void 원본과_같은_수열() {
        // 원본 식을 32비트 정수 연산 그대로 따라 계산한 값
        Mulberry32 rng = new Mulberry32(20261030);
        assertEquals(0.8795690659899265, rng.next());
        assertEquals(0.7244416528847069, rng.next());
        assertEquals(0.36183333303779364, rng.next());
    }
}
