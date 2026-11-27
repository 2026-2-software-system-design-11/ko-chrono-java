# ko-chrono-java

"다음주 화요일 오후 3시" 같은 한국어 문장에서 날짜와 시각을 찾아 값으로 바꾸는 Java 라이브러리입니다.

[ko-chrono](https://github.com/jwn7/ko-chrono)(TypeScript, MIT)를 Java로 옮긴 것입니다. 옮긴 기준은 원본 커밋 [`bdc0f2f`](https://github.com/jwn7/ko-chrono/tree/bdc0f2f6e32863b5ea6d929074240e5d94f1799b)입니다. 원본의 `src/`와 `tests/`를 옮겼고, 원본 시험을 모두 Java 시험으로 옮겨 같은 결과가 나오는지 확인합니다.

- 지원하는 표현과 결과: [SPEC.md](SPEC.md) (원본 명세를 옮긴 것)
- 옮기며 바꾼 것과 그 이유: [docs/porting.md](docs/porting.md)

## 요구 사항

- Java 21
- 런타임 의존성 없음 (시험에만 JUnit 5)

## 빌드

```sh
./gradlew build   # 컴파일과 시험
./gradlew jar     # build/libs/ko-chrono-java.jar
```

## 사용법

```java
import java.time.ZoneId;
import java.time.ZonedDateTime;
import kochrono.KoChrono;
import kochrono.ParseOptions;
import kochrono.ParseResult;
import kochrono.ParsedEvent;

ParseOptions options = ParseOptions.defaults()
        .withReferenceDate(ZonedDateTime.of(2026, 10, 30, 14, 0, 0, 0, ZoneId.of("Asia/Seoul")));

// 첫 번째 표현
ParseResult r = KoChrono.parse("그럼 다음주 수요일 저녁 7시에 강남에서 볼까?", options);
r.date();   // 2026-11-04T19:00+09:00[Asia/Seoul]
r.text();   // "다음주 수요일 저녁 7시"
r.index();  // 3

// 날짜만
KoChrono.parseDate("내일 오후 3시", options);  // 2026-10-31T15:00+09:00[Asia/Seoul]

// 모든 표현
KoChrono.parseAll("내일 10시 아니면 모레 2시", options);  // 결과 2개

// 마감과 구간: date()는 시작, end()는 끝
ParseResult due = KoChrono.parse("이번달 말까지 제출", options);
due.end();  // 2026-10-31T23:59+09:00[Asia/Seoul]

// 반복 일정: date()는 첫 발생, recurrence().rrule()은 RFC 5545 RRULE
KoChrono.parse("매주 월수금 7시", options).recurrence().rrule();  // "FREQ=WEEKLY;BYDAY=MO,WE,FR"

// 할 일 한 줄을 제목과 시점으로
ParsedEvent e = KoChrono.parseEvent("다음주 금요일까지 보고서 제출", options);
e.title();  // "보고서 제출"

// iCalendar(.ics)
String ics = KoChrono.toICS(KoChrono.parseEvent("매주 월요일 오전 9시 팀 회의", options));
```

기준 시각을 주지 않으면 지금을 씁니다. 서버(UTC)에서 한국 사용자의 문장을 읽을 때는 `withTimezone("Asia/Seoul")`을 넘깁니다. 그러면 "오후 3시"를 서울의 오후 3시로 읽고, 결과는 그 시간대의 시각으로 나옵니다.

결과에서 값이 없는 칸(`end()`, `recurrence()`, `ambiguity()`, `tense()`)은 null입니다. 해석이 갈릴 수 있으면 `ambiguity()`에 이유와 다른 후보가 들어갑니다("3시"는 15시, 후보 3시).

## 라이선스

MIT. 원본 ko-chrono의 저작권 표시를 [LICENSE](LICENSE)에 그대로 둡니다.
