package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JsRegexTest {
    @Test void 공백은_NBSP와_전각_공백도_잡는다() {
        assertTrue(JsRegex.compile("내일\\s*3시").matcher("내일\u00A03시").matches());
        assertTrue(JsRegex.compile("내일\\s*3시").matcher("내일\u30003시").matches());
        assertTrue(JsRegex.compile("[\\s,]+").matcher(" ,\u3000").matches());
        assertFalse(JsRegex.compile("\\S").matcher("\u3000").find());
    }

    @Test void 숫자는_ASCII만() {
        assertFalse(JsRegex.compile("\\d").matcher("\uFF13").find());
    }

    @Test void 끝_표시는_마지막_줄바꿈_앞에서_맞지_않는다() {
        assertFalse(JsRegex.compile("봐$").matcher("봐\n").find());
        assertTrue(JsRegex.compile("봐$").matcher("봐").find());
        assertTrue(JsRegex.compile("[$]").matcher("$").find());
    }

    @Test void trim은_JavaScript와_같은_글자를_자른다() {
        assertEquals("3시", JsRegex.trim("\u3000 3시\u00A0\n"));
        assertEquals("3시 ", JsRegex.trimStart("\uFEFF3시 "));
        assertEquals(" 3시", JsRegex.trimEnd(" 3시\u2028"));
        assertEquals("다음날", JsRegex.removeSpaces("다음\u3000날"));
    }

    @Test void slice는_범위를_넘으면_빈_문자열() {
        assertEquals("", Texts.slice("내일", 3, 1));
        assertEquals("일", Texts.slice("내일", 1));
        assertEquals("", Texts.slice("내일", 5));
    }
}
