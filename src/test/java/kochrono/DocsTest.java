package kochrono;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 원본 tests/docs.test.ts: GitHub 마크다운에서 한 문단에 물결표가 둘 있으면 그 사이가 취소선이 된다.
 * 날짜 범위를 다루는 프로젝트라 문서에 물결표가 많다. 코드 밖의 물결표는 역슬래시를 붙여 쓴다.
 */
@DisplayName("문서 렌더링")
class DocsTest {
    private static final Pattern FENCE = Pattern.compile("^[>\\s]*```");
    private static final Pattern TILDE = Pattern.compile("(^|[^\\\\])~");

    static List<String> unescapedTildes(String md) {
        List<String> found = new ArrayList<>();
        boolean inCode = false;
        for (String line : md.split("\n", -1)) {
            if (FENCE.matcher(line).find()) {
                inCode = !inCode;
                continue;
            }
            if (inCode) {
                continue;
            }
            String[] parts = line.split("`", -1);
            StringBuilder outside = new StringBuilder();
            for (int i = 0; i < parts.length; i += 2) {
                outside.append(parts[i]);
            }
            if (TILDE.matcher(outside).find()) {
                found.add(line.trim());
            }
        }
        return found;
    }

    private static void check(String file) throws IOException {
        assertEquals(List.of(), unescapedTildes(Files.readString(Path.of(file))));
    }

    @Test @DisplayName("README.md: 코드 밖 물결표는 \\~ 로 (취소선 방지)") void readme() throws IOException {
        check("README.md");
    }

    @Test @Disabled("원본의 영어 README는 옮기지 않아 이 저장소에 없다")
    @DisplayName("README.en.md: 코드 밖 물결표는 \\~ 로 (취소선 방지)") void readmeEn() throws IOException {
        check("README.en.md");
    }

    @Test @DisplayName("SPEC.md: 코드 밖 물결표는 \\~ 로 (취소선 방지)") void spec() throws IOException {
        check("SPEC.md");
    }

    @Test @Disabled("원본의 bench 폴더는 옮기지 않아 이 저장소에 없다")
    @DisplayName("bench/RESULTS.md: 코드 밖 물결표는 \\~ 로 (취소선 방지)") void benchResults() throws IOException {
        check("bench/RESULTS.md");
    }

    /** 원본에 없는 사례. 이 저장소에만 있는 문서도 같은 규칙으로 본다 */
    @Test @DisplayName("docs/porting.md: 코드 밖 물결표는 \\~ 로 (취소선 방지)") void porting() throws IOException {
        check("docs/porting.md");
    }
}
