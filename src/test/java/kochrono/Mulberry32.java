package kochrono;

/** 원본 퍼즈 시험의 mulberry32: 작은 결정적 난수. 원본과 같은 32비트 정수 연산으로 같은 수열을 낸다 */
final class Mulberry32 {
    private int seed;

    Mulberry32(int seed) {
        this.seed = seed;
    }

    /** 0 이상 1 미만 */
    double next() {
        seed = seed + 0x6D2B79F5;
        int t = (seed ^ (seed >>> 15)) * (1 | seed);
        t = (t + (t ^ (t >>> 7)) * (61 | t)) ^ t;
        return Integer.toUnsignedLong(t ^ (t >>> 14)) / 4294967296.0;
    }
}
