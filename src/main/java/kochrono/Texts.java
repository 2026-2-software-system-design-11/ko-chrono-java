package kochrono;

/** 원본의 문자열 자르기를 같은 뜻으로 */
final class Texts {
    private Texts() {
    }

    /** {@code s.slice(from, to)}: 범위를 넘거나 거꾸로면 빈 문자열 */
    static String slice(String s, int from, int to) {
        int a = Math.max(0, Math.min(from, s.length()));
        int b = Math.max(0, Math.min(to, s.length()));
        return a < b ? s.substring(a, b) : "";
    }

    /** {@code s.slice(from)} */
    static String slice(String s, int from) {
        return slice(s, from, s.length());
    }
}
