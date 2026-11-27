package kochrono;

import java.util.regex.Pattern;

/**
 * 원본의 JavaScript 정규식을 같은 뜻의 Java 정규식으로 바꾼다.
 *
 * <ul>
 *   <li>JavaScript의 {@code \s}는 NBSP, 전각 공백 같은 유니코드 공백도 잡는다. Java의 {@code \s}는 ASCII 공백만 잡으므로
 *       JavaScript가 잡는 글자를 모두 적은 글자 묶음으로 바꾼다. UNICODE_CHARACTER_CLASS는 {@code \d}까지 넓혀 쓰지 않는다.</li>
 *   <li>JavaScript의 {@code $}는 입력의 끝에서만 맞는다. Java의 {@code $}는 마지막 줄바꿈 앞에서도 맞으므로 {@code \z}로 바꾼다.</li>
 * </ul>
 * 문자열 끝 공백 자르기도 JavaScript의 trim과 같은 글자를 자른다.
 */
final class JsRegex {
    /** JavaScript {@code \s}가 잡는 글자. 글자 묶음 안에 그대로 넣을 수 있는 형태 */
    static final String SPACE = "\\t\\n\\u000B\\f\\r \\u00A0\\u1680\\u2000-\\u200A\\u2028\\u2029\\u202F\\u205F\\u3000\\uFEFF";
    private static final Pattern SPACES = Pattern.compile("[" + SPACE + "]+");

    private JsRegex() {
    }

    static Pattern compile(String js) {
        return compile(js, 0);
    }

    static Pattern compile(String js, int flags) {
        return Pattern.compile(translate(js), flags);
    }

    /** {@code \s}, {@code \S}, {@code $}를 바꾼다. 글자 묶음 안의 {@code $}는 글자 그대로라 두고 넘어간다 */
    static String translate(String js) {
        StringBuilder out = new StringBuilder();
        boolean inClass = false;
        for (int i = 0; i < js.length(); i++) {
            char c = js.charAt(i);
            if (c == '\\' && i + 1 < js.length()) {
                char next = js.charAt(++i);
                if (next == 's') {
                    out.append(inClass ? SPACE : "[" + SPACE + "]");
                } else if (next == 'S' && !inClass) {
                    out.append("[^").append(SPACE).append(']');
                } else {
                    out.append(c).append(next);
                }
            } else if (c == '[' && !inClass) {
                inClass = true;
                out.append(c);
            } else if (c == ']' && inClass) {
                inClass = false;
                out.append(c);
            } else if (c == '$' && !inClass) {
                out.append("\\z");
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    /** JavaScript {@code \s}에 드는 글자인지 */
    static boolean isSpace(char c) {
        return switch (c) {
            case '\t', '\n', '\u000B', '\f', '\r', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
                 '　', '﻿' -> true;
            default -> c >= ' ' && c <= ' ';
        };
    }

    /** {@code s.trim()} */
    static String trim(String s) {
        return trimEnd(trimStart(s));
    }

    /** {@code s.trimStart()} */
    static String trimStart(String s) {
        int i = 0;
        while (i < s.length() && isSpace(s.charAt(i))) {
            i++;
        }
        return s.substring(i);
    }

    /** {@code s.trimEnd()} */
    static String trimEnd(String s) {
        int i = s.length();
        while (i > 0 && isSpace(s.charAt(i - 1))) {
            i--;
        }
        return s.substring(0, i);
    }

    /** {@code s.replace(/\s/g, '')} */
    static String removeSpaces(String s) {
        return SPACES.matcher(s).replaceAll("");
    }
}
