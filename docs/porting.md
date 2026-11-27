# 옮긴 기록

원본 [ko-chrono](https://github.com/jwn7/ko-chrono) 커밋 `bdc0f2f6e32863b5ea6d929074240e5d94f1799b`의 `src/`와 `tests/`를 Java로 옮기며 고른 것과 그 이유를 적는다. 동작은 원본과 같게 하는 것이 목표이고, 원본 시험이 명세다. 원본 시험의 사례는 빼거나 기대값을 바꾸지 않았다.

## 시험 사례 수 대조

| 원본 시험 | 원본 사례 | Java 시험 | Java 사례 | 비고 |
|---|---|---|---|---|
| `tests/parse.test.ts` | 480 (expect 553) | `Parse*Test` | 480 (단언 553) | |
| `tests/real-world.test.ts` (`cases/messenger.ts`, `cases/stress.ts`) | 294 (112 + 182) | `RealWorldTest` | 294 | 예문은 `MessengerCases`, `StressCases` |
| `tests/event.test.ts` | 13 | `EventTest` | 13 | |
| `tests/ics.test.ts` | 12 | `IcsTest` | 12 | |
| `tests/lunar.test.ts` | 5 (1 + 연도별 4) | `LunarTest` | 5 | |
| `tests/fuzz.test.ts` | 2 | `FuzzTest` | 2 | 같은 난수 생성기와 시드로 같은 3000개 문장 |
| `tests/docs.test.ts` | 4 (문서별) | `DocsTest` | 4 + 1 | 2건 `@Disabled`, `docs/porting.md` 1건 추가 |

원본 사례 수는 `it(` 호출을 세고, 반복문으로 만드는 사례(real-world, lunar, docs)는 만들어지는 수를 셌다. 위 표 밖의 시험(`*ParserTest`, `ResolverTest` 등)은 부품마다 더한 단위 시험이다.

### `@Disabled`

| 시험 | 이유 |
|---|---|
| `DocsTest` README.en.md | 원본의 영어 README는 옮기는 범위(`src/`, `tests/`) 밖이라 이 저장소에 없다 |
| `DocsTest` bench/RESULTS.md | 원본의 `bench/` 폴더는 옮기지 않아 이 저장소에 없다 |

원본의 `SPEC.md`(동작 명세)는 [SPEC.md](../SPEC.md)로 옮겼고 그 문서 시험은 살아 있다.

## 공개 API

| 원본 | Java | 이유 |
|---|---|---|
| `parse`, `parseAll`, `parseDate`, `parseEvent`, `toICS` 함수 | `KoChrono`의 같은 이름 정적 메서드 | Java에는 모듈 함수가 없다. 옵션 없는 겹지정(overload)도 둔다 |
| `Date` | `ZonedDateTime` | 원본의 `Date`는 실제 시점이다. 시점과 그 시점의 벽시계 시각을 함께 담는 타입이 쓰기 편하다 |
| 옵션 객체 `{ referenceDate, weekStart, defaultHour, timezone }` | `ParseOptions` record와 `with` 메서드 | 값이 없는 칸은 null이고 기본값을 쓴다 |
| `weekStart: 'monday' \| 'sunday'` | `WeekStart.MONDAY`, `WeekStart.SUNDAY` | 열거형 |
| `timezone: string` | `ZoneId` (`withTimezone(String)`도 있음) | |
| 없는 시간대의 `RangeError` | `DateTimeException` (`ZoneId.of`가 던짐) | Java에서 같은 뜻의 예외 |
| 결과의 선택 칸(`end?`, `recurrence?`, `ambiguity?`, `tense?`) | null | |
| `tense: 'past'` | `Tense.PAST` | |
| `ambiguity.reason` 문자열 | `Ambiguity.Reason` 열거형, `code()`가 원본 문자열 | |
| `recurrence.byWeekday` 숫자(0=일) | `List<DayOfWeek>`, 없으면 빈 목록 | Java의 요일 타입. 내부 계산은 원본처럼 0=일 번호를 쓴다 |
| `Frequency` 문자열 | `Frequency` 열거형 | 이름이 RRULE의 FREQ 값과 같다 |
| `ICSOptions` | `IcsOptions` record | `now`는 `Instant` |

`timezone`을 주지 않으면 원본은 실행 환경의 로컬 시간대로 계산한다. Java에서는 `referenceDate`가 시간대를 가지고 있으므로 그 시간대로 계산하고, `referenceDate`도 없으면 `ZonedDateTime.now()`(실행 환경의 시간대)를 쓴다. 원본과 같은 문장에 같은 결과를 낸다.

## 날짜 계산 (JavaScript `Date`에서 `java.time`으로)

원본의 `resolve.ts`는 `Date`의 로컬 필드로 계산한다. Java에서는 벽시계 시각(`LocalDateTime`, `LocalDate`, `YearMonth`)으로 계산하고, 마지막에 시간대를 붙여 `ZonedDateTime`으로 만든다.

원본은 `Date`의 두 성질에 기대는 곳이 있다. 달을 0부터 세는 것과, 그 달에 없는 날을 넘치는 만큼 다음 달로 굴리는 것이다. Java에서는 달을 1부터 세는 `YearMonth`로 쓰고, 굴림에 기대는 규칙은 그 자리에서 뜻대로 썼다.

| 원본 규칙 | 원본 구현 | Java |
|---|---|---|
| 그 달에 없는 날은 버린다 ("11월 31일", 규칙 35) | `day > lastDayOfMonth` 검사 후 `new Date` | `YearMonth.lengthOfMonth()`로 같은 검사 |
| "한 달 뒤"는 다음 달 같은 날, 없으면 말일 (규칙 24) | `Math.min(day, lastDay)` | `Calendars.sameDayIn` |
| 과거형의 한 주기 앞이 없는 날이면 null (규칙 37) | `setMonth(-1)` 뒤 날짜가 바뀌었는지 비교 | `YearMonth.isValidDay`로 바로 검사 |
| 일 단독이 지났으면 다음 달 | 달 번호 더하고 12로 나눔 | `YearMonth.plusMonths(1)` |
| 매달, 매년 반복의 그 달 날짜 | `lastDayOfMonth`, `new Date(y, m0, d)` | `YearMonth.atDay`, `isValidDay` |
| 시각을 넣을 때 "0시 10분 전"(-1시 -10분)은 전날 23:50 | `setHours(-1, -10)`의 굴림 | `Calendars.atClock`: 0시에서 시와 분을 더한다 |

아래 네 곳은 원본이 없는 날짜를 넘치는 만큼 다음 달로 보낸 결과를 그대로 쓴다. 결과를 같게 하려고 `Calendars.dayCountedFrom`(그 달 1일부터 센 날)으로 같은 날짜를 만든다.

- 연도 없는 월일이 지났는지 볼 때: 평년의 "2월 29일"은 3월 1일로 비교한다. 2027년 3월 1일 오후에 "2월 29일"은 null이고, 2027년 10월에는 2028-02-29가 된다.
- 끝에 일만 있는 구간: "11월 1일부터 31일까지"의 끝은 12월 1일 23:59다.
- "N달간": "1월 31일부터 한 달간"의 끝은 3월 2일 23:59다(2월 31일이 3월 3일로 넘어간 뒤 하루 전).
- 범위를 검사하지 않는 자리의 0일과 13월: "11월 0일"은 10월 31일, "13월 말"은 다음 해 1월 말이다.

그 밖의 차이:

- 원본의 `new Date(y, ...)`는 0\~99년을 1900년대로 읽는다. 이 연도가 들어오는 곳은 네 자리 연도 "0050-01-01"뿐이라 `AbsoluteDateParser.expandYear`에서 1950으로 바꿨다.
- 원본은 계산이 범위를 넘으면 Invalid Date가 되어 결과가 null이다. Java의 날짜 타입은 깨진 값을 담지 못하고 예외를 던지므로, 그 예외와 원본 `Date`의 범위(1970년에서 앞뒤 1억 일)를 넘는 값을 null로 본다. 원본은 시작이 정상이고 끝만 깨진 경우("오늘부터 99999999999일간") 끝이 Invalid Date인 결과를 내지만, Java는 그 결과를 내지 않는다.
- 숫자열이 아주 길면(10의 15제곱 이상) 그 값에서 멈춘다(`KoreanNumber.CAP`). 어느 단위로 더해도 날짜 범위를 넘는 크기라 결과(null)가 같고, `long` 곱셈이 넘치지 않는다.
- 원본의 구간 합치기는 끝 요일이 시작보다 앞이면 B의 `Date`를 그 자리에서 7일 뒤로 고친다. 합치지 못해도 B에 그 값이 남으므로 `RangeMerger`도 B에 같은 값을 쓴다.
- 원본의 비교는 실제 시점끼리, Java의 계산 중 비교는 벽시계 시각끼리 한다. 서머타임이 끝나며 같은 벽시계 시각이 두 번 오는 한 시간 안에서만 다를 수 있다.

## 시간대 (`Intl.DateTimeFormat`에서 `ZoneId`로)

- 실제 시점에서 벽시계로: `ZonedDateTime.withZoneSameInstant`.
- `timezone` 옵션이 있을 때 벽시계에서 실제 시점으로: 원본 `fromWallClock`의 계산(벽시계를 UTC로 읽고 그 시점의 오프셋을 두 번 맞춤)을 `ZoneRules.getOffset`으로 그대로 옮겼다. 서머타임이 시작되어 없는 시각(뉴욕 3월 8일 2:30)은 원본처럼 1:30 EST가 된다. `ZonedDateTime.of`는 3:30 EDT로 옮기므로 쓰지 않았다.
- `timezone`이 없을 때: 원본은 로컬 `Date` 생성자를 쓰고, 없는 시각은 서머타임 전의 오프셋으로 읽는다. `ZonedDateTime.of`가 같은 결과를 낸다.

## 정규식

| 원본 | Java | 이유 |
|---|---|---|
| `\s` | JavaScript가 잡는 공백 글자를 모두 적은 글자 묶음 (`JsRegex.SPACE`) | Java의 `\s`는 ASCII 공백만 잡는다. `UNICODE_CHARACTER_CLASS`는 `\d`까지 유니코드 숫자로 넓혀서 쓰지 않았다 |
| `$` (m 깃발 없음) | `\z` | Java의 `$`는 마지막 줄바꿈 앞에서도 맞는다 |
| `trim`, `trimStart`, `trimEnd` | `JsRegex.trim` 등 | Java의 `String.trim`은 U+0020 이하만, `strip`은 NBSP를 자르지 않는다 |
| `(?<!\d\s*박\s*)(?<!(?<![가-힣])주\s*)` (`absoluteDate.ts`) | `AbsoluteDateParser.afterNightsOrWeek` | 길이가 정해지지 않은 lookbehind는 Java에서 컴파일되지 않는다. 맞은 자리 앞을 거꾸로 보며 공백을 건너뛰고 "숫자+박" 또는 "(한글 아닌 글자 뒤의) 주"인지 본다. 공백은 이어져 있으므로 같은 뜻이다. 걸리면 정규식 엔진처럼 다음 글자부터 다시 찾는다 |
| `'gi'` 깃발 (`3pm`) | `CASE_INSENSITIVE` | ASCII만 대소문자를 무시하는 점이 같다 |
| 정규식 안의 가운뎃점 글자 | 유니코드 이스케이프 `\u00B7` | 같은 글자를 이스케이프로 적었다 |
| `split(/\s+/)` | `Pattern.split(s, -1)` | 끝의 빈 문자열을 원본처럼 남긴다 |

원본 정규식은 `u` 깃발이 없어 UTF-16 단위로, Java는 코드 포인트 단위로 맞춘다. 원본 패턴의 글자는 모두 BMP 안의 글자이고 부정 묶음(`[^가-힣]`)은 둘레를 보는 자리에서만 쓰여, 이모지가 섞인 문장도 결과가 같다. `index`는 원본처럼 UTF-16 위치다.

## 내부 구조

원본 파일과 Java 클래스는 아래처럼 맞는다.

| 원본 | Java |
|---|---|
| `types.ts` | `Components`, `Fragment`, `RecurSpec`, 공개 record들 |
| `util/number.ts` | `KoreanNumber` |
| `util/timezone.ts` | `WallClock` |
| `util/lunarHolidays.ts` | `LunarCalendar` |
| `parsers/*.ts` | `*Parser` |
| `parsers/recurrence.ts`의 `toRRule` | `Rrule` |
| `resolve.ts` | `Grouping`, `Resolver`, `Days`, `Periods`, `Ambiguities`, `RecurrenceResolver`, `Calendars` |
| `index.ts` | `KoChrono`, `Pipeline`, `RangeMerger`, `RecurrenceBinder` |
| `tense.ts` | `PastTense` |
| `event.ts` | `EventTitle` |
| `ics.ts` | `Ics` |

- 원본의 `Components`는 값이 없으면 속성이 없다(undefined). Java에서는 숫자 칸을 null로, 참으로만 쓰는 표시는 `boolean`으로 두었다. 합치기(`Object.assign`)는 뒤 조각의 null이 아닌 값이 앞 값을 덮는 것으로 옮겼다.
- 원본 `resolve`는 한 함수 안에서 단계별로 계산한다. Java에서는 단계를 `Days`(날짜), `Resolver`(시각), `Periods`(끝), `Ambiguities`(모호성)로 나눴다. 순서와 조건은 원본과 같다.

## 시험

- 원본 시험은 실행 환경의 로컬 시간대로 `Date`를 만든다. Java 시험은 `ZoneId.systemDefault()`로 같은 일을 한다. 원본 CI처럼 `TZ`를 Asia/Seoul, UTC, America/Los_Angeles로 바꿔도 통과하는 것을 확인했다.
- 원본이 실제 시점(`Date.UTC`)을 비교하는 시험은 `Instant`끼리 비교한다.
- 원본의 `toEqual`은 값이 undefined인 칸을 무시하고, `toMatchObject`는 기대한 칸만 본다. 시험 도우미 `same`, `matches`가 같은 일을 한다.
- 시험 이름의 가운뎃점은 쉼표로 바꿨다. 입력 문장과 기대값은 바꾸지 않았다.
