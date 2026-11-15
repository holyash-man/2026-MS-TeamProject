# 식단 관리 앱 (2026 모바일소프트웨어 학기말 프로젝트)

Jetpack Compose 식단 관리 앱. 2인 1조, 마감 2026-12-15. 과제 요구사항 요약은 README에 정리한다.

## 구조와 담당

```
app/src/main/java/kr/ac/dongguk/diet/
  data/        A: Room(local), 식약처 음식 데이터 로딩(food), Repository 구현(repository)
  domain/      A: 모델(model), 영양 계산·집계 로직(calc)
  ui/          B: theme, navigation, input/list/analysis 화면
app/src/main/assets/   A: 식약처 음식 100개 JSON
app/src/test/          로컬 단위 테스트 (12개 이상, 주로 domain/calc, ViewModel)
app/src/androidTest/   Compose UI 테스트 (2개 이상)
```

- `data/repository/Repositories.kt` 의 인터페이스가 A↔B 계약이다. 바꿀 때는 팀원과 먼저 합의한다.
- 영양 값은 식약처 DB와 같이 **100g 기준**으로 저장하고, 섭취량 환산(`값 × g / 100`)은 domain/calc 에서만 한다.
- DI는 Hilt 없이 `AppContainer`(수동 컨테이너, `DietApplication.container`)로 한다.
- 음식 100개는 읽기 전용이라 Room 에 넣지 않는다. `LocalFoodRepository` 가 assets/foods.json 을 한 번 파싱해 캐시한다. Room 은 식사 기록용.
- foods.json 은 `tools/food-data/build_foods.py` 로만 만든다(직접 수정 금지). 자세한 규칙은 `tools/food-data/README.md`.

## 명령

- 단위 테스트: `./gradlew testDebugUnitTest`
- UI 테스트(에뮬레이터 필요): `./gradlew connectedDebugAndroidTest`
- 빌드: `./gradlew assembleDebug`

## AI 사용 로그

- 팀원별 프롬프트를 `docs/prompt/<이름>.md` 에 남긴다(채점 20점). 자동 기록 설정은 `tools/ai-log/README.md`.
- 작업 단위가 끝나면 해당 항목의 '단계/설명' 칸을 채운다.

## Git

- 기능별 브랜치(`feature/...`)에서 작업하고 PR로 main에 합친다.
- 둘이 같이 건드리는 파일(build.gradle.kts, libs.versions.toml, DietNavHost.kt)은 수정 전에 공유한다.
